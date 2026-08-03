package com.recruitment.ai.provider.llm;

public record StructuredGenerationRequest(
        String model,
        String systemPrompt,
        String userPrompt,
        String outputSchema,
        String correlationId
) {
}
