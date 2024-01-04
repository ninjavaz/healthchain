package com.healthchain.backend.service;

import com.google.gson.Gson;
import com.healthchain.backend.model.consent.Consent;
import com.healthchain.backend.model.network.CustomIdentity;
import com.healthchain.backend.model.network.TransactionType;
import com.healthchain.backend.model.resource.PatientResource;
import com.healthchain.backend.model.resource.Resource;
import com.healthchain.backend.model.util.NetworkProperties;
import lombok.extern.log4j.Log4j;
import org.hyperledger.fabric.gateway.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Log4j
public class PatientService {
    @Autowired
    private TransactionService transactionService;

    public PatientResource getResource(CustomIdentity customIdentity) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";

        return this.transactionService.evaluateTransaction(TransactionType.getResourceForUser,
                PatientResource.class, hospName, customIdentity);
    }

    public Consent saveConsent(CustomIdentity customIdentity, String practitionerId) throws Exception {
//        Consent consent = Consent.builder().date(new Date()).decision(decision.getValue())
//                .subject(Consent.Subject.builder())

        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";
        System.out.println(customIdentity.getHospName());
        return this.transactionService.submitTransaction(TransactionType.createConsent, Consent.class
                , hospName, customIdentity, practitionerId, Consent.Decision.PERMIT, new Date().getTime());
    }

    public List<Consent> getConsents(CustomIdentity customIdentity) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";

        return this.transactionService.evaluateTransactionWithList(TransactionType.getConsentsForPatient,
                Consent.class, hospName, customIdentity);
    }


}
