package com.recruitment.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.auth.dto.request.LoginRequest;
import com.recruitment.auth.dto.request.LogoutRequest;
import com.recruitment.auth.dto.request.RefreshTokenRequest;
import com.recruitment.auth.dto.request.RegisterRequest;
import com.recruitment.auth.dto.response.AuthResponse;
import com.recruitment.auth.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Task 1: Successful registration and login flow")
    void testLoginSuccess() throws Exception {
        String email = "testuser_" + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword("Password123!");
        registerRequest.setFullName("Test User");

        authenticationService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("Task 1: Login with invalid password returns client error")
    void testLoginInvalidPassword() throws Exception {
        String email = "invalidpass_" + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword("Password123!");
        registerRequest.setFullName("Test User");

        authenticationService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Task 1: Login with unknown account returns client error")
    void testLoginUnknownAccount() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("unknown_user_99999@example.com");
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Task 1: Refresh Token success and invalid token failure")
    void testRefreshTokenFlow() throws Exception {
        String email = "refreshtest_" + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword("Password123!");
        registerRequest.setFullName("Test User");

        AuthResponse authResponse = authenticationService.register(registerRequest);

        Thread.sleep(1000);

        RefreshTokenRequest validRequest = new RefreshTokenRequest();
        validRequest.setRefreshToken(authResponse.getRefreshToken());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists());

        RefreshTokenRequest invalidRequest = new RefreshTokenRequest();
        invalidRequest.setRefreshToken("invalid.refresh.token.string");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Task 1: Access secured endpoint without JWT -> 403 Forbidden")
    void testSecuredEndpointWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Task 1: Access secured /me endpoint with valid JWT -> 200 OK")
    void testSecuredEndpointWithValidJwt() throws Exception {
        String email = "meuser_" + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword("Password123!");
        registerRequest.setFullName("Me User");

        AuthResponse authResponse = authenticationService.register(registerRequest);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + authResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email));
    }

    @Test
    @DisplayName("Task 1: Logout success with valid refresh token -> 200 OK")
    void testLogoutSuccess() throws Exception {
        String email = "logoutuser_" + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword("Password123!");
        registerRequest.setFullName("Logout User");

        AuthResponse authResponse = authenticationService.register(registerRequest);

        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken(authResponse.getRefreshToken());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + authResponse.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

}

