package com.healthchain.backend.model.network;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    PATIENT("PATIENT"),
    PRACTITIONER("PRACTITIONER"),
    ADMIN("ADMIN");

    private final String value;
}
