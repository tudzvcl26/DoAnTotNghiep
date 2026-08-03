package com.recruitment.ai.provider;

import com.recruitment.ai.provider.embedding.EmbeddingProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultModelRouter implements ModelRouter {

    private final NoOpAiProvider noOpAiProvider;
    private final OpenAiStructuredGenerationProvider openAiStructuredGenerationProvider;

    @Override
    public StructuredGenerationProvider structuredGenerationProvider() {
        return openAiStructuredGenerationProvider.descriptor().available()
                ? openAiStructuredGenerationProvider
                : noOpAiProvider;
    }

    @Override
    public EmbeddingProvider embeddingProvider() {
        return noOpAiProvider;
    }

}
