package com.recruitment.auth;

import com.recruitment.auth.bootstrap.SecureAdminBootstrap;
import com.recruitment.auth.bootstrap.SecureEmployerTestBootstrap;
import com.recruitment.auth.dto.request.RegisterRequest;
import com.recruitment.auth.dto.response.AuthResponse;
import com.recruitment.auth.entity.RefreshToken;
import com.recruitment.auth.repository.RefreshTokenRepository;
import com.recruitment.auth.security.RefreshTokenHasher;
import com.recruitment.auth.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "auth.admin-bootstrap.enabled=false"
})
@ActiveProfiles("test")
class RefreshTokenHardeningIntegrationTest {
    @Autowired AuthenticationService authenticationService;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired RefreshTokenHasher hasher;
    @Autowired ApplicationContext context;

    @Test
    void rawRefreshTokenIsNeverPersistedAndRotationUsesHashLookup() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("hash-" + UUID.randomUUID() + "@test.local");
        request.setPassword("StrongPassword123!");
        request.setFullName("Hash Test");

        AuthResponse registered = authenticationService.register(request);
        String rawV1 = registered.getRefreshToken();
        RefreshToken storedV1 = refreshTokenRepository.findByTokenHash(hasher.hash(rawV1)).orElseThrow();
        assertThat(storedV1.getTokenHash()).hasSize(64).isNotEqualTo(rawV1);

        AuthResponse rotated = authenticationService.refreshToken(rawV1);
        assertThat(refreshTokenRepository.findByTokenHash(hasher.hash(rawV1)).orElseThrow().getRevoked()).isTrue();
        String rawV2 = rotated.getRefreshToken();
        RefreshToken storedV2 = refreshTokenRepository.findByTokenHash(hasher.hash(rawV2)).orElseThrow();
        assertThat(storedV2.getTokenHash()).isNotEqualTo(rawV2);
        assertThat(rawV2).isNotEqualTo(rawV1);

        authenticationService.logout(rawV2);
        assertThat(refreshTokenRepository.findByTokenHash(hasher.hash(rawV2)).orElseThrow().getRevoked()).isTrue();
    }

    @Test
    void adminBootstrapIsOffByDefault() {
        assertThat(context.getBeansOfType(SecureAdminBootstrap.class)).isEmpty();
        assertThat(context.getBeansOfType(SecureEmployerTestBootstrap.class)).isEmpty();
    }
}
