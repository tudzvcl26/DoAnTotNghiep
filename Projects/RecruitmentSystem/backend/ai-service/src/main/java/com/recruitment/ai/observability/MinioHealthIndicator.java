package com.recruitment.ai.observability;

import com.recruitment.ai.storage.AiStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("aiMinio")
@RequiredArgsConstructor
public class MinioHealthIndicator implements HealthIndicator {

    private final AiStorageService storageService;

    @Override
    public Health health() {
        if (storageService.bucketExists()) {
            return Health.up()
                    .withDetail("bucket", storageService.bucketName())
                    .build();
        }
        return Health.down()
                .withDetail("bucket", storageService.bucketName())
                .withDetail("reason", "Bucket is unavailable")
                .build();
    }

}
