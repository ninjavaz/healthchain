package com.healthchain.backend.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class CustomUser implements org.hyperledger.fabric.sdk.User {

    private String name;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private CustomEnrollment enrollment;
    private String mspId;

}
