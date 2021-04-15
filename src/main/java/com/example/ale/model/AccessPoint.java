package com.example.ale.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class AccessPoint {

    private String macAddress;
    private String name;
    private String radioBssid;
    private String rssiVal;
}
