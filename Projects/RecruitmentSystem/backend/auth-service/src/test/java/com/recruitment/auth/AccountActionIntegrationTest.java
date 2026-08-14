package com.recruitment.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.auth.entity.AccountActionPurpose;
import com.recruitment.auth.entity.AccountActionToken;
import com.recruitment.auth.repository.AccountActionTokenRepository;
import com.recruitment.auth.security.RefreshTokenHasher;
import com.recruitment.auth.service.AccountActionTokenDelivery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "auth.account-actions.issuance-cooldown-seconds=0"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountActionIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountActionTokenRepository tokenRepository;
    @Autowired RefreshTokenHasher tokenHasher;
    @MockitoBean AccountActionTokenDelivery delivery;

    @Test
    void verificationTokensAreHashedOneTimeAndResendRevokesPrevious() throws Exception {
        String email = "verify_" + System.currentTimeMillis() + "@example.test";
        String accessToken = register(email, "OriginalPassword1!");
        String first = capturedToken(AccountActionPurpose.EMAIL_VERIFICATION, 1);

        AccountActionToken stored = tokenRepository
                .findByTokenHashAndPurpose(tokenHasher.hash(first), AccountActionPurpose.EMAIL_VERIFICATION)
                .orElseThrow();
        assertThat(stored.getTokenHash()).hasSize(64).isNotEqualTo(first);

        mockMvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isAccepted());
        String second = capturedToken(AccountActionPurpose.EMAIL_VERIFICATION, 2);
        assertThat(second).isNotEqualTo(first);

        verifyEmail(first).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("AUTH_013"));
        verifyEmail(second).andExpect(status().isOk());
        verifyEmail(second).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("AUTH_013"));
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.verified").value(true));
    }

    @Test
    void passwordResetIsNonEnumeratingExpiresRevokesRefreshAndUsesBcrypt() throws Exception {
        String email = "reset_" + System.currentTimeMillis() + "@example.test";
        JsonNode registration = registerResponse(email, "OriginalPassword1!");
        String oldRefresh = registration.get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing_" + email + "\"}"))
                .andExpect(status().isAccepted());
        mockMvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isAccepted());
        String resetToken = capturedToken(AccountActionPurpose.PASSWORD_RESET, 1);

        AccountActionToken stored = tokenRepository
                .findByTokenHashAndPurpose(tokenHasher.hash(resetToken), AccountActionPurpose.PASSWORD_RESET)
                .orElseThrow();
        assertThat(stored.getTokenHash()).isNotEqualTo(resetToken);

        reset(resetToken, "NewPassword2!").andExpect(status().isOk());
        reset(resetToken, "AnotherPassword3!").andExpect(status().isBadRequest());
        login(email, "OriginalPassword1!").andExpect(status().isUnauthorized());
        login(email, "NewPassword2!").andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + oldRefresh + "\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isAccepted());
        String expiring = capturedToken(AccountActionPurpose.PASSWORD_RESET, 2);
        AccountActionToken expiringEntity = tokenRepository
                .findByTokenHashAndPurpose(tokenHasher.hash(expiring), AccountActionPurpose.PASSWORD_RESET).orElseThrow();
        expiringEntity.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        tokenRepository.saveAndFlush(expiringEntity);
        reset(expiring, "AnotherPassword3!").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_014"));
    }

    private String register(String email, String password) throws Exception {
        return registerResponse(email, password).get("accessToken").asText();
    }

    private JsonNode registerResponse(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password
                                + "\",\"fullName\":\"Action Test\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("data");
    }

    private String capturedToken(AccountActionPurpose purpose, int occurrence) {
        ArgumentCaptor<com.recruitment.auth.entity.User> users = ArgumentCaptor.forClass(com.recruitment.auth.entity.User.class);
        ArgumentCaptor<AccountActionPurpose> purposes = ArgumentCaptor.forClass(AccountActionPurpose.class);
        ArgumentCaptor<String> tokens = ArgumentCaptor.forClass(String.class);
        verify(delivery, atLeastOnce()).deliver(users.capture(), purposes.capture(), tokens.capture());
        int seen = 0;
        for (int i = 0; i < purposes.getAllValues().size(); i++) {
            if (purposes.getAllValues().get(i) == purpose && ++seen == occurrence) return tokens.getAllValues().get(i);
        }
        throw new AssertionError("Token occurrence not delivered: " + purpose + " #" + occurrence);
    }

    private org.springframework.test.web.servlet.ResultActions verifyEmail(String token) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\"}"));
    }

    private org.springframework.test.web.servlet.ResultActions reset(String token, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\",\"newPassword\":\"" + password + "\"}"));
    }

    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }
}
