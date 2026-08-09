package com.recruitment.auth.bootstrap;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.admin-bootstrap")
public class AdminBootstrapProperties {
    private boolean enabled;
    private String email;
    private String password;
    private String fullName = "System Administrator";
}
