package com.recruitment.ai.provider.llm;

public record StructuredGenerationRequest(
        String model,
        String systemPrompt,
        String userPrompt,
        String outputSchema,
        String correlationId,
        int maxOutputTokens
) {

    public StructuredGenerationRequest(
            String model,
            String systemPrompt,
            String userPrompt,
            String outputSchema,
            String correlationId
    ) {
        this(model, systemPrompt, userPrompt, outputSchema, correlationId, 0);
    }
}
