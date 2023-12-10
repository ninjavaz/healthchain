package com.healthchain.backend.model;

import lombok.Builder;
import lombok.Data;

import java.security.PrivateKey;

@Data
@Builder
public class Enrollment implements org.hyperledger.fabric.sdk.Enrollment {

    private PrivateKey key;
    private String cert;

}
