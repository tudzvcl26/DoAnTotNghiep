package com.recruitment.ai.provider.embedding;

import java.util.List;

public record EmbeddingRequest(
        String model,
        List<String> inputs,
        String correlationId
) {
}
