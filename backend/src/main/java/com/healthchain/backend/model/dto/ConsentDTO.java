package com.healthchain.backend.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsentDTO {
    private String patientId;
    private String granteeId;
}
