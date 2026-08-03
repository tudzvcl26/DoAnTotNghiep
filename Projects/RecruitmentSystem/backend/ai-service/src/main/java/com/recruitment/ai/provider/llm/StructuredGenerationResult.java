package com.recruitment.ai.provider.llm;

public record StructuredGenerationResult(
        String providerName,
        String model,
        String structuredOutput,
        long inputTokens,
        long outputTokens
) {
}
