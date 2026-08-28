package com.recruitment.notification.exception;

import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String code;
    private String error;
    private String message;
    private Map<String, String> errors;
    private String path;
    private LocalDateTime timestamp;
    @Builder.Default
    private String traceId = MDC.get("correlationId");

}
