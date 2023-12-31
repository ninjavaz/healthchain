package com.healthchain.backend.model.resource;

import lombok.Data;

import java.util.List;

@Data
public class PatientResource extends Resource{
    private List<Reference> generalPractitioner;
}

//{
//        "resourceType": "Patient", //Patient / Practitioner
//        "id": "123456",
//        "gender": "male",
//        "telecom": [
//        {
//        "system": "phone",
//        "value": "555-1234"
//        },
//        {
//        "system": "email",
//        "value": "example@email.com"
//        }
//        ],
//        "name": [
//        {
//        "use": "official",
//        "family": "Doe",
//        "given": ["John", "M."]
//        }
//        ],
//        "birthDate": "1980-01-01"
//        }

