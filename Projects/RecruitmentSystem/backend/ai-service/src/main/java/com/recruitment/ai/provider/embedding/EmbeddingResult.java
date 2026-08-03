package com.recruitment.ai.provider.embedding;

import java.util.List;

public record EmbeddingResult(
        String providerName,
        String model,
        List<List<Float>> vectors,
        long inputTokens
) {
}
