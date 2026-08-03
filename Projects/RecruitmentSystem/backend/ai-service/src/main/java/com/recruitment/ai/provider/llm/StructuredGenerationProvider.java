package com.recruitment.ai.provider.llm;

import com.recruitment.ai.provider.ProviderDescriptor;

public interface StructuredGenerationProvider {

    ProviderDescriptor descriptor();

    StructuredGenerationResult generate(StructuredGenerationRequest request);

}
