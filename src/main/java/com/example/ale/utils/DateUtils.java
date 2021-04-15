package com.example.ale.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static DateTimeFormatter dFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final static DateTimeFormatter testFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static LocalDateTime dateTimeStringToLDT(String dateS) {
        return LocalDateTime.parse(dateS, formatter);
    }

    public static String dateToString(LocalDateTime ldt) {
        return ldt.format(formatter);
    }

    public static LocalDate dateStringToLD(String dateS) {
        if(dateS.contains("/")){
            return LocalDate.parse(dateS, testFormatter);
        }
        return LocalDate.parse(dateS, dFormatter);
    }

    public static String dateToString(LocalDate ld) {
        return ld.format(dFormatter);
    }

}
