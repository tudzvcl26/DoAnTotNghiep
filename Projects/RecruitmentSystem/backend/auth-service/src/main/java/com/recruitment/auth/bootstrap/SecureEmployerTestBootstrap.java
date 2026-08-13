package com.recruitment.auth.bootstrap;

import com.recruitment.auth.entity.Role;
import com.recruitment.auth.entity.User;
import com.recruitment.auth.repository.RefreshTokenRepository;
import com.recruitment.auth.repository.RoleRepository;
import com.recruitment.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;

/**
 * Creates one explicitly configured Employer identity for local runtime verification.
 * The dev profile and opt-in property are both required; no credential has a source default.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "auth.employer-test-bootstrap", name = "enabled", havingValue = "true")
public class SecureEmployerTestBootstrap implements ApplicationRunner {
    private final EmployerTestBootstrapProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validateConfiguration();
        String email = properties.getEmail().trim().toLowerCase(Locale.ROOT);
        Role employer = roleRepository.findByName("EMPLOYER")
                .orElseThrow(() -> new IllegalStateException("EMPLOYER role is not available"));

        User user = userRepository.findByEmail(email)
                .map(existing -> validateExistingAccount(existing, employer))
                .orElseGet(() -> User.builder()
                        .email(email)
                        .roles(new HashSet<>())
                        .build());

        user.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
        user.setFullName(properties.getFullName().trim());
        user.setEnabled(true);
        user.setVerified(true);
        user.getRoles().add(employer);
        userRepository.save(user);
        refreshTokenRepository.deleteAllByUser(user);
    }

    private User validateExistingAccount(User user, Role employer) {
        if (!user.getRoles().contains(employer)) {
            throw new IllegalStateException("Configured Employer test email belongs to a non-Employer account");
        }
        return user;
    }

    private void validateConfiguration() {
        String email = properties.getEmail();
        if (email == null || !email.trim().toLowerCase(Locale.ROOT).endsWith(".test")) {
            throw new IllegalStateException("Employer test bootstrap requires an email in the reserved .test domain");
        }
        if (properties.getPassword() == null || properties.getPassword().length() < 12) {
            throw new IllegalStateException("Employer test bootstrap requires a password of at least 12 characters");
        }
        if (properties.getFullName() == null || properties.getFullName().isBlank()) {
            throw new IllegalStateException("Employer test bootstrap requires a full name");
        }
    }
}
