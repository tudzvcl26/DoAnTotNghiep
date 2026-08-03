package com.recruitment.ai.config;

import com.recruitment.ai.storage.AiStorageProperties;
import com.recruitment.ai.storage.AiStorageService;
import io.minio.MinioClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    MinioClient minioClient(AiStorageProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "ai.storage",
            name = "initialize-on-startup",
            havingValue = "true",
            matchIfMissing = true
    )
    ApplicationRunner aiStorageInitializer(AiStorageService storageService) {
        return arguments -> storageService.initialize();
    }

}
