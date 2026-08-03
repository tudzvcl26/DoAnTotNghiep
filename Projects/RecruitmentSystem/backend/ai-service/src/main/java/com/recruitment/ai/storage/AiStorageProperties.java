package com.recruitment.ai.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.storage")
public class AiStorageProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private boolean autoCreateBucket = true;
    private boolean initializeOnStartup = true;

}
