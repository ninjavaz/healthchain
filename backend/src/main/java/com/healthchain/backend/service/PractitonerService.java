package com.healthchain.backend.service;

import com.healthchain.backend.model.Mapper;
import com.healthchain.backend.model.dto.DocumentRefDTO;
import com.healthchain.backend.model.entity.DocumentReference;
import com.healthchain.backend.model.network.CustomIdentity;
import com.healthchain.backend.model.network.TransactionType;
import com.healthchain.backend.model.entity.resource.PatientResource;
import com.healthchain.backend.model.entity.resource.PractitionerResource;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Log4j
public class PractitonerService {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private Mapper mapper;

    public PractitionerResource getResource(CustomIdentity customIdentity, String userId) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";
        return this.transactionService.evaluateTransaction(TransactionType.getResourceForUser,
                PractitionerResource.class, hospName, customIdentity, userId);
    }

    public List<PatientResource> getPatientResourcesForPractitioner(CustomIdentity customIdentity, String userId) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";
        return this.transactionService.evaluateTransactionWithList(TransactionType.getPatientResourcesForPractitioner,
                PatientResource.class, hospName, customIdentity, userId);
    }

    public List<DocumentReference> getDocumentReferences(CustomIdentity customIdentity, String userId) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";
        return this.transactionService.evaluateTransactionWithList(TransactionType.getDocumentReferencesForPractitioner,
                DocumentReference.class, hospName, customIdentity, userId);
    }

    public DocumentReference saveDocumentReference(CustomIdentity customIdentity, DocumentRefDTO documentRefDTO) throws Exception {
        String hospName = customIdentity.getHospName() != null ? customIdentity.getHospName() : "hosp1";
//String patientId, String desc, DocumentReference.Attachment attachment
        return this.transactionService.submitTransaction(TransactionType.createDocumentReference, DocumentReference.class
                , hospName, customIdentity, this.mapper.mapToDocumentRef(documentRefDTO));
    }

}
