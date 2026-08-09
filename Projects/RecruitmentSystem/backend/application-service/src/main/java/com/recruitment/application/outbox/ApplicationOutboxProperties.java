package com.recruitment.application.outbox;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "application.outbox.publisher")
public class ApplicationOutboxProperties {
    private boolean enabled = true;
    private String exchange = "recruitment.events";
    private int batchSize = 50;
    private int maxAttempts = 10;
}
