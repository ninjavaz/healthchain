package com.healthchain.backend.model.network;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Credentials {

    private String certificate;
    private String privateKey;

}