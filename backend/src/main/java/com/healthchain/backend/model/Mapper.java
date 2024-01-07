package com.healthchain.backend.model;

import com.healthchain.backend.model.dto.ConsentDTO;
import com.healthchain.backend.model.dto.DocumentRefDTO;
import com.healthchain.backend.model.dto.ResourceDTO;
import com.healthchain.backend.model.entity.Consent;
import com.healthchain.backend.model.entity.DocumentReference;
import com.healthchain.backend.model.entity.resource.PatientResource;
import com.healthchain.backend.model.entity.resource.PractitionerResource;
import com.healthchain.backend.model.entity.resource.Resource;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

@Service
public class Mapper {

    public Consent mapToConsent(ConsentDTO consentDTO) {
        return Consent.builder()
                .id("")
                .resourceType("Consent")
                .subject(Consent.Subject.builder()
                        .reference(consentDTO.getPatientId())
                        .build())
                .date(System.currentTimeMillis())
                .grantee(Collections.singletonList(Consent.Grantee.builder()
                        .reference(consentDTO.getGranteeId())
                        .build()))
                .decision(Consent.Decision.PERMIT.getValue())
                .date(new Date().getTime())
                .build();
    }

    public DocumentReference mapToDocumentRef(DocumentRefDTO documentRefDTO) {
        return DocumentReference.builder()
                .id("")
                .resourceType("DocumentReference")
                .subject(DocumentReference.Reference.builder().reference(documentRefDTO.getPatientId()).build())
                .date(new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZZZZ").format(new Date()))
                .author(Collections.singletonList(DocumentReference.Reference.builder().reference(documentRefDTO.getPractitionerId()).build()))
                .description(documentRefDTO.getDesc())
                .content(Collections.singletonList(
                        DocumentReference.Content.builder().attachment(
                                        DocumentReference.Attachment.builder()
                                                .contentType(documentRefDTO.getContentType())
                                                .url(documentRefDTO.getContentUrl())
                                                .title(documentRefDTO.getContentTitle())
                                                .data(documentRefDTO.getContentData()).build())
                                .build()))
                .build();
    }

    public PatientResource mapToPatientResource(ResourceDTO resourceDTO) {
        return PatientResource.builder()
                .id(resourceDTO.getId())
                .resourceType("Patient")
                .gender(resourceDTO.getGender())
                .telecom(Collections.singletonList(Resource.ContactPoint.builder()
                        .system(resourceDTO.getContactSystem())
                        .value(resourceDTO.getContactValue())
                        .build()))
                .name(Collections.singletonList(Resource.Name.builder()
                        .use(resourceDTO.getTitleOfAddress())
                        .family(resourceDTO.getSurname())
                        .given(Collections.singletonList(resourceDTO.getName()))
                        .build()))
                .birthDate(new SimpleDateFormat("yyyy-MM-dd").format(resourceDTO.getBirthDate()))
                .managingOrganization(Resource.Reference.builder()
                        .identifier(resourceDTO.getHospName())
                        .build())
                .build();
    }

    public PractitionerResource mapToPractitionerResource(ResourceDTO resourceDTO) {
        return PractitionerResource.builder()
                .resourceType("Practitioner")
                .id(resourceDTO.getId())
                .gender(resourceDTO.getGender())
                .telecom(Collections.singletonList(Resource.ContactPoint.builder()
                        .system(resourceDTO.getContactSystem())
                        .value(resourceDTO.getContactValue())
                        .build()))
                .name(Collections.singletonList(Resource.Name.builder()
                        .use(resourceDTO.getTitleOfAddress())
                        .family(resourceDTO.getSurname())
                        .given(Collections.singletonList(resourceDTO.getName()))
                        .build()))
                .birthDate(new SimpleDateFormat("yyyy-MM-dd").format(resourceDTO.getBirthDate()))
                .managingOrganization(Resource.Reference.builder()
                        .identifier(resourceDTO.getHospName())
                        .build())
                .build();
    }
}
