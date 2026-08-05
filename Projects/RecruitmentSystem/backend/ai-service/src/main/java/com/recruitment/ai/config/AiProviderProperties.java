package com.recruitment.ai.config;

import com.recruitment.ai.provider.ProviderType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.provider")
public class AiProviderProperties {

    private ProviderType type = ProviderType.OPENAI;

}
