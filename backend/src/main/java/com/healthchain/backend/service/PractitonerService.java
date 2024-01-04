package com.healthchain.backend.service;

import com.healthchain.backend.model.network.CustomIdentity;
import com.healthchain.backend.model.network.TransactionType;
import com.healthchain.backend.model.resource.PatientResource;
import com.healthchain.backend.model.resource.PractitionerResource;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j
public class PractitonerService {
    @Autowired
    private TransactionService transactionService;

    public PractitionerResource getResource(CustomIdentity customIdentity) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";
        return this.transactionService.evaluateTransaction(TransactionType.getResourceForUser,
                PractitionerResource.class, hospName, customIdentity);
    }

    public List<PatientResource> getPatientResourcesForPractitioner(CustomIdentity customIdentity) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";
        return this.transactionService.evaluateTransactionWithList(TransactionType.getPatientResourcesForPractitioner,
                PatientResource.class, hospName, customIdentity);
    }

}
