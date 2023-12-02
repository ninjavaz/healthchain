package com.healthchain.backend.model;

import lombok.Data;

@Data
public class CustomIdentity {
    private int version;
    private String mspId;
    private String type;
    private Credentials credentials;
}




