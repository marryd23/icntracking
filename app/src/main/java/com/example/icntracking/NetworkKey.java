package com.example.icntracking;

import java.util.Map;

public class NetworkKey {
    String description;
    String key; // key bytes in HEX
    String macMethod;

    public static final String MAC_METHOD_AES128CMAC_PUBLIC = "AES-128-CMAC-PUBLIC";
    public static final String MAC_METHOD_AES128CMAC = "AES-128-CMAC";
    final static public Map<String,Byte> macTypeMap = Map.of(MAC_METHOD_AES128CMAC_PUBLIC, (byte) 0, MAC_METHOD_AES128CMAC, (byte) 1);
    final static public Map<Integer,String> intMacTypeMap = Map.of(0, MAC_METHOD_AES128CMAC_PUBLIC, 1, MAC_METHOD_AES128CMAC);
    public final static NetworkKey ZMESH_MAC_PUBLIC_KEY = new NetworkKey("AES128 CMAC Public key", "11223344556677889900AABBCCDDEEFF", MAC_METHOD_AES128CMAC_PUBLIC);

    public NetworkKey() {
    }
    
    public NetworkKey(String description, String key, String macMethod) {
        this.description = description;
        this.key = key;
        this.macMethod = macMethod;
    }


    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMacMethod() {
        return this.macMethod;
    }

    public void setMacMethod(String macMethod) {
        this.macMethod = macMethod;
    }

}
