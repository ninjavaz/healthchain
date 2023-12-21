package com.healthchain.backend.model.user;

import lombok.Data;

import java.util.List;

@Data
public class Resource {
    private String resourceType;
    private String id;
    private String gender;
    private List<ContactPoint> telecom;
    private List<Name> name;
    private String birthDate;
    private Reference managingOrganization; //hospName

    @Data
    public static class ContactPoint {
        private String system; //phone | fax | email | pager | url | sms | other
        private String value;
    }

    @Data
    public static class Name {
        private String use;
        private String family;
        private List<String> given;
    }

    @Data
    public static class Reference {
        private String identifier;
    }
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

