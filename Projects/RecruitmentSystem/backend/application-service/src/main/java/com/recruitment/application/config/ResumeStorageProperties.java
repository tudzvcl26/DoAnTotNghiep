package com.recruitment.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "storage.resume")
public class ResumeStorageProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
