package com.healthchain.backend.model.entity;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DocumentReference {
    private String resourceType;
    private String id;
    private Reference subject;
    private String date;
    private List<Reference> author;
    private String description;
    private List<Content> content;

    @Data
    @Builder
    public static class Reference {
        private String reference;
    }

    @Data
    @Builder
    public static class Content {
        private Attachment attachment;
    }

    @Data
    @Builder
    public static class Attachment {
        private String contentType;
        private String url;
        private String data;
        private String title;
    }
}
