package com.healthchain.backend.model.network;

import com.healthchain.backend.model.network.Credentials;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.math3.analysis.function.Identity;

@Data
@Builder
public class CustomIdentity extends Identity {
    private int version;
    private String mspId;
    private String type;
    private String hospName;

    private String username;
    private Role role;

    private Credentials credentials;
}




