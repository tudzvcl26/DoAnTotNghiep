package com.recruitment.auth;

import com.recruitment.auth.bootstrap.SecureEmployerTestBootstrap;
import com.recruitment.auth.entity.User;
import com.recruitment.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"test", "dev"})
class EmployerTestBootstrapIntegrationTest {
    private static final String TEST_PASSWORD = "Test-" + UUID.randomUUID() + "!Aa";

    @DynamicPropertySource
    static void employerBootstrapProperties(DynamicPropertyRegistry registry) {
        registry.add("auth.employer-test-bootstrap.enabled", () -> true);
        registry.add("auth.employer-test-bootstrap.email", () -> "bootstrap-employer@example.test");
        registry.add("auth.employer-test-bootstrap.password", () -> TEST_PASSWORD);
        registry.add("auth.employer-test-bootstrap.full-name", () -> "Bootstrap Employer");
    }

    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired SecureEmployerTestBootstrap bootstrap;

    @Test
    void createsEnabledEmployerThroughTheApplicationLayer() {
        User user = userRepository.findByEmail("bootstrap-employer@example.test").orElseThrow();

        assertThat(bootstrap).isNotNull();
        assertThat(user.getEnabled()).isTrue();
        assertThat(user.getVerified()).isTrue();
        assertThat(user.getRoles()).extracting("name").containsExactly("EMPLOYER");
        assertThat(passwordEncoder.matches(TEST_PASSWORD, user.getPasswordHash())).isTrue();
    }
}
