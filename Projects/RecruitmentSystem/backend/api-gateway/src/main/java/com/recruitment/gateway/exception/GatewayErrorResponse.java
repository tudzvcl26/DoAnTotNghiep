package com.recruitment.gateway.exception;

import java.time.Instant;

public record GatewayErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String traceId) {
}
