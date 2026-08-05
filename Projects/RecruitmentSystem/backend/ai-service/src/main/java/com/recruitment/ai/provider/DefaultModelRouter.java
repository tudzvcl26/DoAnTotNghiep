package com.recruitment.ai.provider;

import com.recruitment.ai.config.AiProviderProperties;
import com.recruitment.ai.provider.embedding.EmbeddingProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultModelRouter implements ModelRouter {

    private final NoOpAiProvider noOpAiProvider;
    private final AiProviderProperties providerProperties;
    private final OpenAiStructuredGenerationProvider openAiStructuredGenerationProvider;
    private final OllamaStructuredGenerationProvider ollamaStructuredGenerationProvider;

    @Override
    public StructuredGenerationProvider structuredGenerationProvider() {
        StructuredGenerationProvider selected = switch (providerProperties.getType()) {
            case OPENAI -> openAiStructuredGenerationProvider;
            case OLLAMA -> ollamaStructuredGenerationProvider;
        };
        return selected.descriptor().available() ? selected : noOpAiProvider;
    }

    @Override
    public EmbeddingProvider embeddingProvider() {
        return noOpAiProvider;
    }

}
