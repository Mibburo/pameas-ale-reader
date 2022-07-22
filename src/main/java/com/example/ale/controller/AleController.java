package com.example.ale.controller;

import com.example.ale.service.AleReader;
import com.example.ale.service.AleRestService;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.GeneralSecurityException;

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
        aleReader.killSwitch();
    }

    @GetMapping("/geofence2")
    public String testGeofenceRestApi2() throws GeneralSecurityException {
        return aleRestService.getGeofences();
    }

    @GetMapping("/getAccessToken")
    public String getAccessToken() throws UnirestException {
        return aleRestService.getAccessToken();
    }

}
