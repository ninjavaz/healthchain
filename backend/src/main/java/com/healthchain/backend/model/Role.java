package com.healthchain.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
public enum Role {
    PATIENT("PATIENT"),
    DOCTOR("DOCTOR");

    private final String value;
}