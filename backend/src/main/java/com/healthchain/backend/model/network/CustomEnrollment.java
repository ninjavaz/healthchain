package com.healthchain.backend.model.network;

import lombok.Builder;
import lombok.Data;
import java.security.PrivateKey;

@Data
@Builder
public class CustomEnrollment implements org.hyperledger.fabric.sdk.Enrollment {

    private PrivateKey key;
    private String cert;

}
