package com.recruitment.notification.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private Map<String, String> errors;
    private String path;
    private LocalDateTime timestamp;

}
