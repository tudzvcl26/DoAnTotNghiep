package com.recruitment.user.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final StorageProperties storageProperties;

    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
                .endpoint(storageProperties.getEndpoint())
                .credentials(
                        storageProperties.getAccessKey(),
                        storageProperties.getSecretKey()
                )
                .build();

    }

    @Bean("minioPublicClient")
    public MinioClient minioPublicClient() {
        String endpoint = storageProperties.getPublicEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = storageProperties.getEndpoint();
        }
        return MinioClient.builder()
                .endpoint(endpoint)
                .region("us-east-1")
                .credentials(storageProperties.getAccessKey(), storageProperties.getSecretKey())
                .build();
    }

}
