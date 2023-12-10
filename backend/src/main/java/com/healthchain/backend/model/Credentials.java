package com.healthchain.backend.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Credentials {

    private String certificate;
    private String privateKey;

}