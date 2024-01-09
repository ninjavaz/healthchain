package com.healthchain.backend.model.network;

import lombok.*;
import org.hyperledger.fabric.gateway.Identity;

@Data
@Builder
public class CustomIdentity implements Identity{
    private int version;
    private String mspId;
    private String type;
    private String hospName;
    private String username;
    private Role role;
    private Credentials credentials;

    @Override
    public String getMspId() {
        return this.mspId;
    }
}




