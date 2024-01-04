package com.healthchain.backend.model.network;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
public class Credentials {
    private String certificate;
    private String privateKey;
}