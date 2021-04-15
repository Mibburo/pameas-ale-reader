package com.example.ale.utils;

public class Converter {

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String transformMacAddress(String macAddress){
        //remove buffer chars if any
        String mcAddr = macAddress.length() < 16? macAddress : macAddress.substring(4, macAddress.length());
        String val = "2";

        //add : every val = 2 characters and remove the last one
        return mcAddr.replaceAll("(.{" + val + "})", "$0:").replaceAll(".$", "");
    }


}
