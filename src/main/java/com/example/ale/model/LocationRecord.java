package com.example.ale.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class LocationRecord {

    private String timestamp;
    private String radioMacAddr;
    private String rssiVal;
    private String channel;

}
