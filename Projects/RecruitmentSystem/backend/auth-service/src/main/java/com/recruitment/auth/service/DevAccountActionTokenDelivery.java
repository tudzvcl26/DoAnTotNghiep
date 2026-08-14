package com.recruitment.auth.service;

import com.recruitment.auth.entity.AccountActionPurpose;
import com.recruitment.auth.entity.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("dev")
public class DevAccountActionTokenDelivery implements AccountActionTokenDelivery {
    private final Map<String, String> inbox = new ConcurrentHashMap<>();

    @Override
    public void deliver(User user, AccountActionPurpose purpose, String rawToken) {
        inbox.put(key(user.getEmail(), purpose), rawToken);
    }

    public Optional<String> latest(String email, AccountActionPurpose purpose) {
        return Optional.ofNullable(inbox.get(key(email, purpose)));
    }

    private String key(String email, AccountActionPurpose purpose) {
        return email.trim().toLowerCase() + ":" + purpose.name();
    }
}
