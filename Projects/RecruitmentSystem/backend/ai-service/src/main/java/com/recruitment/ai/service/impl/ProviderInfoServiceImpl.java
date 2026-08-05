package com.recruitment.ai.service.impl;

import com.recruitment.ai.config.AiProviderProperties;
import com.recruitment.ai.config.OllamaProperties;
import com.recruitment.ai.config.OpenAiProperties;
import com.recruitment.ai.provider.ModelRouter;
import com.recruitment.ai.provider.OllamaStructuredGenerationProvider;
import com.recruitment.ai.provider.ProviderDescriptor;
import com.recruitment.ai.provider.ProviderType;
import com.recruitment.ai.repository.ModelDeploymentRepository;
import com.recruitment.ai.service.ProviderInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProviderInfoServiceImpl implements ProviderInfoService {

    private final ModelRouter modelRouter;
    private final ModelDeploymentRepository modelDeploymentRepository;
    private final OpenAiProperties openAiProperties;
    private final OllamaProperties ollamaProperties;
    private final AiProviderProperties providerProperties;
    private final OllamaStructuredGenerationProvider ollamaProvider;

    @Override
    public Map<String, Object> getProviderInfo() {
        ProviderDescriptor structured = modelRouter.structuredGenerationProvider().descriptor();
        ProviderDescriptor embedding = modelRouter.embeddingProvider().descriptor();
        Map<String, Object> info = new LinkedHashMap<>();
        ProviderType providerType = providerProperties.getType();
        boolean ollamaOnline = providerType == ProviderType.OLLAMA && ollamaProvider.availability().online();
        info.put("phase", "COMPLETE");
        info.put("provider", providerType.name());
        info.put("model", providerType == ProviderType.OLLAMA
                ? ollamaProperties.getModel()
                : openAiProperties.getStructuredModel());
        info.put("endpoint", providerType == ProviderType.OLLAMA
                ? ollamaProperties.getBaseUrl()
                : openAiProperties.getBaseUrl());
        info.put("status", providerType == ProviderType.OLLAMA
                ? (ollamaOnline ? "ONLINE" : "OFFLINE")
                : (structured.available() ? "CONFIGURED" : "OFFLINE"));
        info.put("openAiEnabled", openAiProperties.isEnabled());
        info.put("openAiConfigured", openAiProperties.isConfigured());
        info.put("ollamaEnabled", ollamaProperties.isEnabled());
        info.put("ollamaConfigured", ollamaProperties.isConfigured());
        info.put("structuredGeneration", descriptor(structured));
        info.put("embedding", descriptor(embedding));
        info.put("enabledModelDeployments", modelDeploymentRepository.findByEnabledTrueOrderByProviderNameAscModelNameAsc().size());
        return info;
    }

    private Map<String, Object> descriptor(ProviderDescriptor descriptor) {
        return Map.of(
                "providerName", descriptor.providerName(),
                "implementation", descriptor.implementation(),
                "available", descriptor.available()
        );
    }

}
