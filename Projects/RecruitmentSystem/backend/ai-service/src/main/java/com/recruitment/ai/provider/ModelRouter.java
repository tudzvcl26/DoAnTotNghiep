package com.recruitment.ai.provider;

import com.recruitment.ai.provider.embedding.EmbeddingProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;

public interface ModelRouter {

    StructuredGenerationProvider structuredGenerationProvider();

    EmbeddingProvider embeddingProvider();

}
