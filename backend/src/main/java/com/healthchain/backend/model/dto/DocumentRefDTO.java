package com.healthchain.backend.model.dto;

import com.healthchain.backend.model.entity.DocumentReference;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

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


