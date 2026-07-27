package com.recruitment.company.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;

    private String error;

    private String message;

    /**
     * Validation errors
     * key = field
     * value = message
     */
    private Map<String, String> errors;

    private String path;

    private LocalDateTime timestamp;

}