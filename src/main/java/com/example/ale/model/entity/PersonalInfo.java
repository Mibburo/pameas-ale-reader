package com.example.ale.model.entity;

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
public class PersonalInfo {

    @Id
    private String id;

    private String name;
    private String surname;
    private String dateOfBirth;
    private String sex;
    private String clientAddress;
    //eId or nationalId
    private String personalId;
    private String passportNo;
    private List<String> linkedIds;

    //TODO new objects probably
    private String ticketInfo;
    private String medicalStatus;

    private String macAddress;
    private String IMSI;
    private String MSIDN;
    private String IMEI;

    //new object ?
    private String contactInfo;

}
