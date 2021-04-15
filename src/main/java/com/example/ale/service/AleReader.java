package com.example.ale.service;

import com.example.ale.model.AccessPoint;
import com.example.ale.model.Location;
import com.example.ale.model.LocationRecord;
import com.example.ale.model.entity.ClientGeofence;
import com.example.ale.model.entity.ClientLocation;
import com.example.ale.model.entity.GeofenceNtf;
import com.example.ale.repository.ClientGeofenceRepository;
import com.example.ale.repository.GeofenceNtfRepository;
import com.example.ale.repository.ClientLocationRepository;
import com.example.ale.utils.Converter;
import com.example.ale.utils.DateUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.alelimited.AleLimited;

@Slf4j
//@Component
@Service
public class AleReader {

    @Value("${ale.host}")
    private String aleHost;

    @Value("${ale.nbapi.port}")
    private String nbApiPort;

    @Value("${brms.uri}")
    private String brmsUri;

    private final ClientLocationRepository locationRepo;
    private final GeofenceNtfRepository geofenceNtfRepo;
    private final ClientGeofenceRepository clientGeofenceRepo;

    @Autowired
    public AleReader(ClientLocationRepository locationRepo, GeofenceNtfRepository geofenceNtfRepo, ClientGeofenceRepository clientGeofenceRepo) {
        this.locationRepo = locationRepo;
        this.geofenceNtfRepo = geofenceNtfRepo;
        this.clientGeofenceRepo = clientGeofenceRepo;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Void> aleReader(){
        try (ZContext context = new ZContext()) {

            //connect to kill switch socket
            ZMQ.Socket controller = context.createSocket(SocketType.PULL);
            controller.connect("tcp://localhost:5555");

            //  Connect to ale server
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            String url = "tcp://" + aleHost + ":" + nbApiPort;

            subscriber.connect(url);
            String topic = "";
            String[] filterSub = {"location", "geofence_notify"};

            for (String s : filterSub) {
                subscriber.subscribe(s.getBytes(StandardCharsets.UTF_8));
            }
            //subscriber.subscribe(topic.getBytes(ZMQ.CHARSET));

            //  Initialize poll set
            ZMQ.Poller items = context.createPoller(2);

            items.register(subscriber, ZMQ.Poller.POLLIN);
            items.register(controller, ZMQ.Poller.POLLIN);

            //  Process messages from both sockets
            //while (!Thread.currentThread().isInterrupted()) {
            while(true){
                byte[] data;
                items.poll();
                if (items.pollin(0)) {
                    topic = subscriber.recvStr();
                    log.info("xxxxxxxxxxxxxxx topic :{}", topic);
                    data = subscriber.recv(0);
                    log.info("Process ale msg");

                    while(subscriber.hasReceiveMore()){
                        log.info("multi-part zmq message ");
                        byte[] moreBytes = subscriber.recv(0);
                        byteConcat(data, moreBytes);
                    }

                    //parse ale event message
                    AleLimited.nb_event msg = parseAleMsg(data);

                    //handle message (save to db and/or send to rules engine)
                    handleMessages(topic, msg);

                    log.info("process ale update");
                }

                if (items.pollin(1)) {
                    //data = controller.recv(0);
                    log.info("--------------------- kill switch ---------------------");
                    break;
                }
            }
        }

        return null;
    }

    public void killSwitch() throws Exception{

        try (ZContext context = new ZContext()) {
            // Socket for worker control
            ZMQ.Socket controller = context.createSocket(SocketType.PUSH);
            controller.bind("tcp://*:5555");

            //  Send the kill signal to the workers
            controller.send("KILL", 0);
            log.info("kill command sent ?");
            //  Give it some time to deliver
            Thread.sleep(1000);
        }

    }

    private AleLimited.nb_event parseAleMsg(byte[] data){

        try {
            return AleLimited.nb_event.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
           log.error(e.getMessage());
        }

        return null;
    }

    private void handleLocationMsg(AleLimited.nb_event msg){
        AleLimited.location locMsg = msg.getLocation();

        log.info("xxxxxxxxxxxxxx location msg :{}", locMsg);

        //ClientLocation clientLocation = new ClientLocation();

        String macAddress = Converter.transformMacAddress(Converter.bytesToHex(locMsg.getStaEthMac().toByteArray()));

        //TODO create service with optional response
        ClientLocation clientLocation = locationRepo.findByMacAddress(macAddress);
        List<Location> locationHistory = clientLocation != null? clientLocation.getLocationHistory() : new ArrayList<>();

        if(clientLocation == null){
            clientLocation = new ClientLocation();

            clientLocation.setMacAddress(macAddress);
            clientLocation.setHashedMacAddress(Converter.bytesToHex(locMsg.getHashedStaEthMac().toByteArray()));
        }
        Location location = new Location();

        location.setTimestamp(DateUtils.dateToString(Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime()));

        location.setUntransformedAddress(Converter.bytesToHex(locMsg.getStaEthMac().toByteArray()));

        location.setXLocation(String.valueOf(locMsg.getStaLocationX()));
        location.setYLocation(String.valueOf(locMsg.getStaLocationY()));
        location.setErrorLevel(String.valueOf(locMsg.getErrorLevel()));
        location.setIsAssociated(String.valueOf(locMsg.getAssociated()));
        location.setCampusId(Converter.bytesToHex(locMsg.getCampusId().toByteArray()));
        location.setBuildingId(Converter.bytesToHex(locMsg.getBuildingId().toByteArray()));
        location.setFloorId(Converter.bytesToHex(locMsg.getFloorId().toByteArray()));
        location.setHashedMacAddress(Converter.bytesToHex(locMsg.getHashedStaEthMac().toByteArray()));
        location.setGeofenceNames(locMsg.getGeofenceNamesList());
        if(locMsg.getGeofenceIdsCount() > 0){
            location.setGeofenceId(Converter.bytesToHex(locMsg.getGeofenceIds(0).toByteArray()));

        }
        location.setLocAlgorithm(String.valueOf(locMsg.getLocAlgorithm()));
        location.setLongitude(String.valueOf(locMsg.getLongitude()));
        location.setLatitude(String.valueOf(locMsg.getLatitude()));
        location.setAltitude(String.valueOf(locMsg.getAltitude()));
        location.setMUnit(String.valueOf(locMsg.getUnit()));
        location.setTargetType(String.valueOf(locMsg.getTargetType()));
        location.setErrorCode(String.valueOf(locMsg.getErrCode()));
        location.setRssiVal(String.valueOf(locMsg.getRssiVal()));

        List<LocationRecord> locRecords = new ArrayList<>();
        List<AleLimited.location.record> records = locMsg.getRecordsList();
        for(AleLimited.location.record rec:records){
            LocationRecord record = new LocationRecord();
            record.setTimestamp(DateUtils.dateToString(Instant.ofEpochSecond(rec.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
            record.setRadioMacAddr(Converter.transformMacAddress(Converter.bytesToHex(rec.getRadioMac().getAddr().toByteArray())));
            record.setRssiVal(String.valueOf(rec.getRssiVal()));
            record.setChannel(String.valueOf(rec.getChannel()));
            locRecords.add(record);
        }
        location.setRecords(locRecords);




        if(locMsg.getGeofenceIdsCount() > 0){
            location.setGeofenceId(Converter.bytesToHex(locMsg.getGeofenceIds(0).toByteArray()));
        }

        locationHistory.add(location);
        clientLocation.setLocationHistory(locationHistory);

        //save to db
        locationRepo.save(clientLocation);

        log.info("yyyyyyyyyyyyyyy location entity :{}", clientLocation);


    }

//    public void handleLocationMsgForDev(String topic, AleLimited.nb_event msg){
//        AleLimited.location locMsg = msg.getLocation();
//
//        log.info("xxxxxxxxxxxxxx location msg :{}", locMsg);
//
//        ClientLocation clientLocation = new ClientLocation();
//
//        String macAddress = Converter.transformMacAddress(Converter.bytesToHex(locMsg.getStaEthMac().toByteArray()));
//
//
//        clientLocation.setMacAddress(Converter.transformMacAddress(Converter.bytesToHex(locMsg.getStaEthMac().toByteArray())));
//        clientLocation.setXLocation(String.valueOf(locMsg.getStaLocationX()));
//        clientLocation.setYLocation(String.valueOf(locMsg.getStaLocationY()));
//        clientLocation.setErrorLevel(String.valueOf(locMsg.getErrorLevel()));
//        clientLocation.setIsAssociated(String.valueOf(locMsg.getAssociated()));
//        clientLocation.setCampusId(Converter.bytesToHex(locMsg.getCampusId().toByteArray()));
//        clientLocation.setBuildingId(Converter.bytesToHex(locMsg.getBuildingId().toByteArray()));
//        clientLocation.setFloorId(Converter.bytesToHex(locMsg.getFloorId().toByteArray()));
//        clientLocation.setHashedMacAddress(Converter.bytesToHex(locMsg.getHashedStaEthMac().toByteArray()));
//        if(locMsg.getGeofenceIdsCount() > 0){
//            clientLocation.setGeofenceId(Converter.bytesToHex(locMsg.getGeofenceIds(0).toByteArray()));
//        }
//        clientLocation.setLocAlgorithm(String.valueOf(locMsg.getLocAlgorithm()));
//        clientLocation.setLongitude(String.valueOf(locMsg.getLongitude()));
//        clientLocation.setLatitude(String.valueOf(locMsg.getLatitude()));
//        clientLocation.setAltitude(String.valueOf(locMsg.getAltitude()));
//        clientLocation.setMUnit(String.valueOf(locMsg.getUnit()));
//        clientLocation.setTargetType(String.valueOf(locMsg.getTargetType()));
//        clientLocation.setErrorCode(String.valueOf(locMsg.getErrCode()));
//
//        List<LocationRecord> locRecords = new ArrayList<>();
//        List<AleLimited.location.record> records = locMsg.getRecordsList();
//        for(AleLimited.location.record rec:records){
//            LocationRecord record = new LocationRecord();
//            record.setTimestamp(DateUtils.dateToString(Instant.ofEpochSecond(rec.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
//            record.setRadioMacAddr(Converter.transformMacAddress(Converter.bytesToHex(rec.getRadioMac().getAddr().toByteArray())));
//            record.setRssiVal(String.valueOf(rec.getRssiVal()));
//            record.setChannel(String.valueOf(rec.getChannel()));
//            locRecords.add(record);
//        }
//        clientLocation.setRecords(locRecords);
//
//        //save to db
//        locationRepo.save(clientLocation);
//
//        log.info("yyyyyyyyyyyyyyy location entity :{}", clientLocation);
//
//
//    }

   /* public void handleGeofenceNtfMsg(AleLimited.nb_event msg){
        AleLimited.geofence_notify gfntf = msg.getGeofenceNotify();

        log.info("xxxxxxxxxxxxxxxxxxxx geofence msg :{}", gfntf);

        GeofenceNtf geofenceNtf = new GeofenceNtf();

        geofenceNtf.setGfEvent(String.valueOf(gfntf.getGeofenceEvent()));
        geofenceNtf.setGfId(Converter.bytesToHex(gfntf.getGeofenceId().toByteArray()));
        geofenceNtf.setGfName(gfntf.getGeofenceName());
        geofenceNtf.setMacAddress(Converter.transformMacAddress(Converter.bytesToHex(gfntf.getStaMac().toByteArray())));
        geofenceNtf.setIsAssociated(String.valueOf(gfntf.getAssociated()));
        geofenceNtf.setDwellTime(String.valueOf(gfntf.getDwellTime()));
        geofenceNtf.setTimestamp(DateUtils.dateToString(Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime()));

        List<AccessPoint> apList = new ArrayList<>();
        List<AleLimited.geofence_notify.Access_point_info> apInfoList = gfntf.getAccessPointInfoList();
        for(AleLimited.geofence_notify.Access_point_info apInfo: apInfoList){
            AccessPoint ap = new AccessPoint();
            ap.setMacAddress(Converter.transformMacAddress(Converter.bytesToHex(apInfo.getApMac().getAddr().toByteArray())));
            ap.setName(apInfo.getApName());
            ap.setRadioBssid(Converter.transformMacAddress(Converter.bytesToHex(apInfo.getRadioBssid().getAddr().toByteArray())));
            ap.setRssiVal(String.valueOf(apInfo.getRssiVal()));

            apList.add(ap);
        }

        geofenceNtf.setApInfo(apList);
        geofenceNtf.setHashedMacAddress(Converter.bytesToHex(gfntf.getHashedStaMac().toByteArray()));

        log.info("yyyyyyyyyyyyyyyyyyy geofence entity :{}", geofenceNtf);

        //save to db
        geofenceNtfRepo.save(geofenceNtf);

    }*/

    private void handleClientGeofenceNtfMsg(AleLimited.nb_event msg){
        AleLimited.geofence_notify gfntf = msg.getGeofenceNotify();

        String macAddress = Converter.transformMacAddress(Converter.bytesToHex(gfntf.getStaMac().toByteArray()));

        log.info("xxxxxxxxxxxxxxxxxxxx geofence msg :{}", gfntf);
        saveGfToDb(gfntf, macAddress);

        //make call to brms
        sendToBrms(gfntf, macAddress);

    }

    @Async("asyncExecutor")
    public void sendToBrms(AleLimited.geofence_notify gfntf, String macAddress){
        final String uri = brmsUri + "/rules/scenario1";
        GeofenceNtf gfNtf = populateGfNtf(gfntf, macAddress);
        log.info("11111111111111111111111 gfNtf for brms :{}", gfNtf);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(gfNtf), new ParameterizedTypeReference<GeofenceNtf>() {
        });
    }

    private GeofenceNtf populateGfNtf(AleLimited.geofence_notify gfntf, String macAddress){
        GeofenceNtf geofenceNtf = new GeofenceNtf();

        geofenceNtf.setMacAddress(macAddress);
        geofenceNtf.setUntransformedAddress(Converter.bytesToHex(gfntf.getStaMac().toByteArray()));
        geofenceNtf.setGfEvent(String.valueOf(gfntf.getGeofenceEvent()));
        geofenceNtf.setGfId(Converter.bytesToHex(gfntf.getGeofenceId().toByteArray()));
        geofenceNtf.setGfName(gfntf.getGeofenceName());
        geofenceNtf.setIsAssociated(String.valueOf(gfntf.getAssociated()));
        geofenceNtf.setDwellTime(String.valueOf(gfntf.getDwellTime()));
        geofenceNtf.setTimestamp(DateUtils.dateToString(Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime()));

        List<AccessPoint> apList = new ArrayList<>();
        List<AleLimited.geofence_notify.Access_point_info> apInfoList = gfntf.getAccessPointInfoList();
        for(AleLimited.geofence_notify.Access_point_info apInfo: apInfoList){
            AccessPoint ap = new AccessPoint();
            ap.setMacAddress(Converter.transformMacAddress(Converter.bytesToHex(apInfo.getApMac().getAddr().toByteArray())));
            ap.setName(apInfo.getApName());
            ap.setRadioBssid(Converter.transformMacAddress(Converter.bytesToHex(apInfo.getRadioBssid().getAddr().toByteArray())));
            ap.setRssiVal(String.valueOf(apInfo.getRssiVal()));

            apList.add(ap);
        }

        geofenceNtf.setApInfo(apList);

        return geofenceNtf;
    }

    private void saveGfToDb(AleLimited.geofence_notify gfntf, String macAddress){


        //TODO create service with optional response
        ClientGeofence clientGeofence = clientGeofenceRepo.findByMacAddress(macAddress);
        List<GeofenceNtf> geofenceHistory = clientGeofence != null? clientGeofence.getGeofenceHistory() : new ArrayList<>();

        if(clientGeofence == null){
            clientGeofence = new ClientGeofence();

            clientGeofence.setMacAddress(macAddress);
            clientGeofence.setHashedMacAddress(Converter.bytesToHex(gfntf.getHashedStaMac().toByteArray()));
        }
        GeofenceNtf geofenceNtf = populateGfNtf(gfntf, macAddress);

        geofenceHistory.add(geofenceNtf);
        clientGeofence.setGeofenceHistory(geofenceHistory);

        log.info("yyyyyyyyyyyyyyyyyyy client geofence entity :{}", clientGeofence);

        //save to db
        clientGeofenceRepo.save(clientGeofence);
    }

    private void handleMessages(String topic, AleLimited.nb_event msg){

        if(topic.startsWith("location")){
            //if(/*topic.contains("b4:6b:fc:7e:64:95") || */topic.contains("38:37:8b:de:42:f8")){
            handleLocationMsg(msg);
            //}

        }

        if(topic.startsWith("geofence_notify")){
            handleClientGeofenceNtfMsg(msg);
        }
//        switch (topic){
//            case "location":
//                handleLocationMsg(msg);
//            break;
//            case "geofence_notify":
//                handleGeofenceNtfMsg(msg);
//            break;
//
//        }
    }

    private byte[] byteConcat(byte[] a, byte[] b){
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a,  0,  result,  0,  a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
