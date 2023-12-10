package com.healthchain.backend.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomIdentity {
    private int version;
    private String mspId;
    private String type;
    private Credentials credentials;
}




