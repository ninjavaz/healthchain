package com.healthchain.backend.model;

import lombok.Data;

import java.security.KeyFactory;
import java.security.PrivateKey;

@Data
public class Credentials {
    private String certificate;
    private String privateKey;


//    public PrivateKey getKey() {
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        return keyFactory.generatePrivate(privateKey);
//    }
}