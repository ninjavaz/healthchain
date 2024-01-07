package com.healthchain.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Builder
public class ConsentDTO {
//    private String id;
    private String patientId; //patients id
//    private long date;
    private String granteeId; //granted practitioners id
}
