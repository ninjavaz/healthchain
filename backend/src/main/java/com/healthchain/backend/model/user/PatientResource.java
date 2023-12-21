package com.healthchain.backend.model.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
public class PatientResource extends Resource{
    private List<Reference> generalPractitioner;
}
