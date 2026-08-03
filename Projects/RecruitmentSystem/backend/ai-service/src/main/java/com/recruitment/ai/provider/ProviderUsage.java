package com.recruitment.ai.provider;

public record ProviderUsage(
        String providerName,
        String model,
        String operation,
        long inputTokens,
        long outputTokens,
        long durationMillis,
        boolean successful,
        String correlationId
) {
}
