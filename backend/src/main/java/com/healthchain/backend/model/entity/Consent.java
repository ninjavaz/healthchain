package com.healthchain.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class Consent {
    private String resourceType;
    private String id;
    private Subject subject; //patients id
    private long date;
    private List<Grantee> grantee; //granted practitioners id
    private String decision; //permit | deny

    @Data
    @Builder
    public static class Subject {
        private String reference;
        private String display;
    }

    @Data
    @Builder
    public static class Grantee {
        private String reference;
        private String display;
    }

    @AllArgsConstructor
    @Getter
    public enum Decision {
        DENY("deny"),
        PERMIT("permit");
        private final String value;
    }
}
