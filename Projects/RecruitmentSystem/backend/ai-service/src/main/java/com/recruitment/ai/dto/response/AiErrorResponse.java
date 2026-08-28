package com.recruitment.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiErrorResponse {

    private boolean success;
    private String code;
    private String message;
    private boolean retryable;
    private String correlationId;
    private String traceId;
    private String path;
    private Map<String, String> details;
    private LocalDateTime timestamp;

}
