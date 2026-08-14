package com.recruitment.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.account-actions")
public class AccountActionProperties {
    private long passwordResetExpiryMinutes = 15;
    private long emailVerificationExpiryMinutes = 1440;
    private long issuanceCooldownSeconds = 60;
}
