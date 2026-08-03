package com.recruitment.ai.provider.embedding;

import com.recruitment.ai.provider.ProviderDescriptor;

public interface EmbeddingProvider {

    ProviderDescriptor descriptor();

    EmbeddingResult embed(EmbeddingRequest request);

}
