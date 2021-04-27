package com.example.ale.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class Location {

    //private String untransformedAddress;
    private String xLocation;
    private String yLocation;
    private String errorLevel;
    private String isAssociated;
    private String campusId;
    private String buildingId;
    private String floorId;
    private String hashedMacAddress;
    private String geofenceId;
    /*private String locAlgorithm;
    private String longitude;
    private String latitude;
    private String altitude;
    private String mUnit;
    private String targetType;
    private String errorCode;
    private List<LocationRecord> records;*/
    private List<String> geofenceNames;
   // private String rssiVal;
    private String timestamp;
}
