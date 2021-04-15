package com.example.ale.model.entity;

import com.example.ale.model.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class ClientGeofence {

    @Id
    private String id;

    private String macAddress;
    private String hashedMacAddress;
    private List<GeofenceNtf> geofenceHistory;
}
