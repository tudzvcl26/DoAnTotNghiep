package com.recruitment.auth.service;

import com.recruitment.auth.entity.AccountActionPurpose;
import com.recruitment.auth.entity.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!dev")
public class ProductionAccountActionTokenDelivery implements AccountActionTokenDelivery {
    @Override
    public void deliver(User user, AccountActionPurpose purpose, String rawToken) {
        // Intentionally no-op until a production email provider is configured.
        // Raw account-action tokens must never be logged.
    }
}
