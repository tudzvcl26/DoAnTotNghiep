package com.recruitment.ai.provider;

import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.provider.embedding.EmbeddingProvider;
import com.recruitment.ai.provider.embedding.EmbeddingRequest;
import com.recruitment.ai.provider.embedding.EmbeddingResult;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationRequest;
import com.recruitment.ai.provider.llm.StructuredGenerationResult;
import org.springframework.stereotype.Component;

@Component
public class NoOpAiProvider implements StructuredGenerationProvider, EmbeddingProvider {

    private static final ProviderDescriptor DESCRIPTOR =
            new ProviderDescriptor("none", "NO_OP", false);

    @Override
    public ProviderDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public StructuredGenerationResult generate(StructuredGenerationRequest request) {
        throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
    }

    @Override
    public EmbeddingResult embed(EmbeddingRequest request) {
        throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
    }

}
