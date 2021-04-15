package com.example.ale.controller;

import com.example.ale.service.AleReader;
import com.example.ale.service.AleRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value ="aleReader")
public class AleController {

    //@Autowired
    private final AleReader aleReader;
    private final AleRestService aleRestService;

    @Autowired
    public AleController(AleReader aleReader, AleRestService aleRestService){
        this.aleReader = aleReader;
        this.aleRestService = aleRestService;
    }

    @PostMapping("/startReader")
    @ResponseStatus(value = HttpStatus.OK)
    public void startAleReader(){
        aleReader.aleReader();
    }

    @PostMapping("/killReader")
    @ResponseStatus(value = HttpStatus.OK)
    public void testSecondRestCall() throws Exception {
        log.info("LLLLLLLLLLLLLLLLLLLLLLLLL new rest call");
        aleReader.killSwitch();
        log.info("KKKKKKKKKKKKKKKKKKKKKKKK end kill switch");
    }

    @GetMapping("/geofence")
    public String testGeofenceRestApi(){
        return aleRestService.getSomething();
    }

    @GetMapping("/geofence2")
    public String testGeofenceRestApi2(){
        return aleRestService.getSomething2();
    }

}
