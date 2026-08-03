package com.recruitment.ai.service.impl;

import com.recruitment.ai.config.OpenAiProperties;
import com.recruitment.ai.provider.ModelRouter;
import com.recruitment.ai.provider.ProviderDescriptor;
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

    @Override
    public Map<String, Object> getProviderInfo() {
        ProviderDescriptor structured = modelRouter.structuredGenerationProvider().descriptor();
        ProviderDescriptor embedding = modelRouter.embeddingProvider().descriptor();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("phase", "COMPLETE");
        info.put("openAiEnabled", openAiProperties.isEnabled());
        info.put("openAiConfigured", openAiProperties.isConfigured());
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
