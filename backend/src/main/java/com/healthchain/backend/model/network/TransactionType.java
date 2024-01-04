package com.healthchain.backend.model.network;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransactionType {
    createResource("createResource"),
    getResourceForUser("getResourceForUser"),
    createConsent("createConsent"),
    getConsentsForPatient("getConsentsForPatient"),
    getPatientResourcesForPractitioner("getPatientResourcesForPractitioner");
    private final String value;
}
