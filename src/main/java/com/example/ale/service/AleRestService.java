package com.example.ale.service;

import com.example.ale.config.RestTemplateConfig;
import com.example.ale.model.AccessPoint;
import com.example.ale.model.Location;
import com.example.ale.model.entity.ClientLocation;
import com.example.ale.model.entity.GeofenceNtf;
import com.example.ale.utils.Converter;
import com.example.ale.utils.DateUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class AleRestService {

    @Value("${ale.host}")
    private String aleHost;

    @Autowired
    RestTemplateConfig restTemplate;

    /*private static HttpHeaders getHeaders ()
    {
        String adminuserCredentials = "admin:welcome123";
        String encodedCredentials =
                new String(Base64.encodeBase64(adminuserCredentials.getBytes()));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Basic " + encodedCredentials);
        httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }*/

//    public Map<String, Object> getSomething() {
//        final String uri = "https://" + aleHost + "/api/v1/geo_fence";
//
//        HttpHeaders httpHeaders = getHeaders();
//
//        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
//
//        RestTemplate restTemplate = new RestTemplate();
//        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<Map<String, Object>>(){};
//
//        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, responseType);
//        //String result = restTemplate.getForObject(uri, String.class);
//        //log.info("QQQQQQQQQQQQQQQQQQQQQQQQQ geofence rest api result :{}", result);
//        return responseEntity.getBody();
//    }

    public String getGeofences() throws GeneralSecurityException {

        String clientId = "admin";
        String clientSecret = "welcome123";

        final String uri = "https://" + aleHost + "/api/v1/geo_fence";
        RestTemplate restTempl = restTemplate.restTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        String token = new String(Base64.encodeBase64((clientId + ":" + clientSecret).getBytes()));
        headers.add("Authorization", "Basic " + token);
        HttpEntity<?> request = new HttpEntity<>(headers);
        log.info(request.toString());
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);
        log.info("URI -" + builder.toUriString());
        ResponseEntity<String> response = restTempl.exchange(builder.build().encode().toUri(), HttpMethod.GET, request, String.class);
        log.info("Response" + response.getBody());

        return response.getBody();
    }

    public String getAccessToken() throws UnirestException {
        HttpResponse<String> response = Unirest.post("https://dss1.aegean.gr/auth/realms/palaemon/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("client_id=palaemonRegistration&client_secret=bdbbb8d5-3ee7-4907-b95c-2baae17bd10f&grant_type=client_credentials&scope=openid")
                .asString();

        JSONObject jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject.get("access_token"));
    }

    public String sendToDbProxy() throws UnirestException {
        String accessToken = getAccessToken();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+accessToken);
        ClientLocation clLocation = generateLocation();
        String result = restTemplate.postForObject("", clLocation, String.class);
        return null;
    }

    private ClientLocation generateLocation(){

        ClientLocation clLocation = new ClientLocation();
        clLocation.setMacAddress("38:37:8B:DE:42:F8");
        clLocation.setHashedMacAddress("88E11064690B48367511DA2D5F4FEB1CACE7E041");

        GeofenceNtf geofenceNtf = new GeofenceNtf();
        geofenceNtf.setGfEvent("ZONE_IN");
        geofenceNtf.setGfId("02719D22D1923693BE0C892FA3EAFA30");
        geofenceNtf.setGfName("muster_station");
        geofenceNtf.setIsAssociated("true");
        geofenceNtf.setDwellTime("0");
        geofenceNtf.setTimestamp("2021-12-17 17:26:33");

        GeofenceNtf geofenceNtf2 = new GeofenceNtf();
        geofenceNtf2.setGfEvent("ZONE_OUT");
        geofenceNtf2.setGfId("02719D22D1923693BE0C892FA3EAFA30");
        geofenceNtf2.setGfName("muster_station");
        geofenceNtf2.setIsAssociated("true");
        geofenceNtf2.setDwellTime("32");
        geofenceNtf2.setTimestamp("2021-12-17 17:27:05");

        Location location = new Location();
        location.setXLocation("11.685399");
        location.setYLocation("5.0785675");
        location.setErrorLevel("17");
        location.setIsAssociated("true");
        location.setCampusId("84C82330FDF935898A54B7BCAC01E955");
        location.setBuildingId("54D076B49BE53D3DA49F7007A7F02474");
        location.setFloorId("489341871340374998D07AEFE0168F65");
        location.setGeofenceId("02719D22D1923693BE0C892FA3EAFA30");
        location.setGeofenceNames(Arrays.asList(geofenceNtf.getGfName()));
        location.setTimestamp("2021-12-17 17:26:01");

        Location location2 = new Location();
        location2.setXLocation("16.679125");
        location2.setYLocation("4.144419");
        location2.setErrorLevel("17");
        location2.setIsAssociated("true");
        location2.setCampusId("84C82330FDF935898A54B7BCAC01E955");
        location2.setBuildingId("54D076B49BE53D3DA49F7007A7F02474");
        location2.setFloorId("489341871340374998D07AEFE0168F65");
        location2.setGeofenceId("02719D22D1923693BE0C892FA3EAFA30");
        location2.setGeofenceNames(Arrays.asList("cabin_area"));
        location2.setTimestamp("2021-12-17 17:27:22");

        List<GeofenceNtf> geofenceHistory = new ArrayList<>();
        geofenceHistory.add(geofenceNtf);
        geofenceHistory.add(geofenceNtf2);

        List<Location> locationHistory = new ArrayList<>();
        locationHistory.add(location);
        locationHistory.add(location2);

        clLocation.setLocationHistory(locationHistory);
        clLocation.setGeofenceHistory(geofenceHistory);

        return clLocation;
    }
}
