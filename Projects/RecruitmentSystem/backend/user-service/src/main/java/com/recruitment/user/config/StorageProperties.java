package com.recruitment.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * MinIO endpoint
     * vd: http://localhost:9000
     */
    private String endpoint;

    /** Browser-reachable endpoint used only when generating presigned URLs. */
    private String publicEndpoint;

    /**
     * access key
     */
    private String accessKey;

    /**
     * secret key
     */
    private String secretKey;

    /**
     * bucket name
     */
    private String bucket;

    /**
     * tạo bucket nếu chưa tồn tại
     */
    private boolean autoCreateBucket = true;

}
