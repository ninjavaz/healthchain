package com.healthchain.backend.model.entity.resource;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
public class Resource {
    private String id;
    private String resourceType;
    private String gender;
    private List<ContactPoint> telecom;
    private List<Name> name;
    private String birthDate;
    private Reference managingOrganization; //hospName

    @Data
    @Builder
    public static class ContactPoint {
        private String system; //phone | fax | email | pager | url | sms | other
        private String value;
    }

    @Data
    @Builder
    public static class Name {
        private String use;
        private String family;
        private List<String> given;
    }

    @Data
    @Builder
    public static class Reference {
        private String identifier;
    }
}
