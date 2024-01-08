package com.healthchain.backend.model.network;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransactionType {
    //ADMIN METHODS
    createResource("createResource"),

    //PATIENT METHODS
    createConsent("createConsent"),
    getConsentsForPatient("getConsentsForPatient"),
    getDocumentReferencesForPatient("getDocumentReferencesForPatient"),

    //PRACTITIONER METHODS
    createDocumentReference("createDocumentReference"),
    getDocumentReferencesForPractitioner("getDocumentReferencesForPractitioner"),
    getPatientResourcesForPractitioner("getPatientResourcesForPractitioner"),

    //UNIVERSAL METHODS
    getResourceForUser("getResourceForUser");

    private final String value;
}
