package com.healthchain.backend.model.util;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class ErrorMessage {
    private int code;
    private Date timestamp;
    private String name;
    private String message;
}
