package com.healthchain.backend.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentRefDTO {
    private String patientId;
    private String practitionerId;
    private String desc;
    private String contentType;
    private String contentUrl;
    private String contentData;
    private String contentTitle;
}


