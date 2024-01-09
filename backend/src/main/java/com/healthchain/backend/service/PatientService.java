package com.healthchain.backend.service;

import com.healthchain.backend.model.Mapper;
import com.healthchain.backend.model.dto.ConsentDTO;
import com.healthchain.backend.model.entity.Consent;
import com.healthchain.backend.model.entity.DocumentReference;
import com.healthchain.backend.model.network.CustomIdentity;
import com.healthchain.backend.model.network.TransactionType;
import com.healthchain.backend.model.entity.resource.PatientResource;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Log4j
public class PatientService {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private Mapper mapper;

    public PatientResource getResource(CustomIdentity customIdentity, String userId) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";
        return this.transactionService.evaluateTransaction(TransactionType.getResourceForUser,
                PatientResource.class, hospName, customIdentity, userId);
    }

    public Consent saveConsent(CustomIdentity customIdentity, ConsentDTO consentDTO) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";
        return this.transactionService.submitTransaction(TransactionType.createConsent, Consent.class
                , hospName, customIdentity, this.mapper.mapToConsent(consentDTO));
    }

    public List<Consent> getConsents(CustomIdentity customIdentity, String userId) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";
        return this.transactionService.evaluateTransactionWithList(TransactionType.getConsentsForPatient,
                Consent.class, hospName, customIdentity, userId);
    }

    public List<DocumentReference> getDocumentReferencesForPatient(CustomIdentity customIdentity, String userId) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";
        return this.transactionService.evaluateTransactionWithList(TransactionType.getDocumentReferencesForPatient,
                DocumentReference.class, hospName, customIdentity, userId);
    }
}
