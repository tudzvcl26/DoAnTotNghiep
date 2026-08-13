package com.recruitment.auth.bootstrap;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@Profile("dev")
@ConfigurationProperties(prefix = "auth.employer-test-bootstrap")
public class EmployerTestBootstrapProperties {
    private boolean enabled;
    private String email;
    private String password;
    private String fullName = "Runtime Test Employer";
}
