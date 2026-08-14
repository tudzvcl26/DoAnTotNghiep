package com.recruitment.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@Profile("dev")
@ConfigurationProperties(prefix = "auth.dev-action-token")
public class DevActionTokenProperties {
    private String accessKey;
}
