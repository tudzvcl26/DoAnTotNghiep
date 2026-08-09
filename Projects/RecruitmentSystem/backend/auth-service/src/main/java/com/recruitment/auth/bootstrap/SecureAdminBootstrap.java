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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "auth.admin-bootstrap", name = "enabled", havingValue = "true")
public class SecureAdminBootstrap implements ApplicationRunner {
    private final AdminBootstrapProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validateConfiguration();
        Role admin = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role is not available"));
        User user = userRepository.findByEmail(properties.getEmail().trim().toLowerCase())
                .orElseGet(() -> User.builder()
                        .email(properties.getEmail().trim().toLowerCase())
                        .fullName(properties.getFullName())
                        .roles(new HashSet<>())
                        .build());
        user.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
        user.setFullName(properties.getFullName());
        user.setEnabled(true);
        user.setVerified(true);
        user.getRoles().add(admin);
        userRepository.save(user);
        refreshTokenRepository.deleteAllByUser(user);
    }

    private void validateConfiguration() {
        if (properties.getEmail() == null || properties.getEmail().isBlank()
                || properties.getPassword() == null || properties.getPassword().length() < 12) {
            throw new IllegalStateException(
                    "Enabled admin bootstrap requires an email and a password of at least 12 characters");
        }
    }
}
