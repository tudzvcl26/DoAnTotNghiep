package com.recruitment.ai.dto.response;

public record CareerChatResponse(
        String answer,
        String language,
        String providerName,
        String modelName,
        int correctionAttempts,
        long generationDurationMs,
        String correlationId
) { }
