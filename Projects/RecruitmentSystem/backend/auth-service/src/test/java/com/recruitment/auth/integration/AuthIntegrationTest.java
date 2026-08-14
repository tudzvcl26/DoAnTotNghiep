package com.recruitment.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.auth.dto.request.LoginRequest;
import com.recruitment.auth.dto.request.LogoutRequest;
import com.recruitment.auth.dto.request.RefreshTokenRequest;
import com.recruitment.auth.dto.request.RegisterRequest;
import com.recruitment.auth.dto.request.RegistrationRole;
import com.recruitment.auth.dto.response.AuthResponse;
import com.recruitment.auth.service.AuthenticationService;
import com.recruitment.auth.entity.Role;
import com.recruitment.auth.entity.User;
import com.recruitment.auth.repository.RoleRepository;
import com.recruitment.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
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

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Admin user management is role-protected and prevents self lockout")
    void adminUserManagement() throws Exception {
        String stamp = String.valueOf(System.currentTimeMillis());
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        User admin = User.builder().email("admin_" + stamp + "@example.test")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Admin User").enabled(true).verified(true).roles(new HashSet<>()).build();
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
        AuthResponse adminAuth = authenticationService.login(login(admin.getEmail(), "Password123!"));

        RegisterRequest candidateRequest = new RegisterRequest();
        candidateRequest.setEmail("managed_" + stamp + "@example.test");
        candidateRequest.setPassword("Password123!");
        candidateRequest.setFullName("Managed Candidate");
        AuthResponse candidateAuth = authenticationService.register(candidateRequest);
        User candidate = userRepository.findByEmail(candidateRequest.getEmail()).orElseThrow();

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + candidateAuth.getAccessToken()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/users").param("keyword", "managed_" + stamp)
                        .header("Authorization", "Bearer " + adminAuth.getAccessToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/admin/users/" + candidate.getId() + "/roles")
                        .header("Authorization", "Bearer " + adminAuth.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"roles\":[\"EMPLOYER\"]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.roles[0]").value("EMPLOYER"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/admin/users/" + candidate.getId() + "/enabled")
                        .header("Authorization", "Bearer " + adminAuth.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.enabled").value(false));
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + candidate.getEmail() + "\",\"password\":\"Password123!\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_005"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/admin/users/" + admin.getId() + "/enabled")
                        .header("Authorization", "Bearer " + adminAuth.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("AUTH_012"));
    }

    private LoginRequest login(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    @Test
    @DisplayName("Registration defaults to CANDIDATE when role is omitted")
    void registrationDefaultsToCandidate() throws Exception {
        String email = "candidate_" + System.currentTimeMillis() + "@example.com";

        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password123!","fullName":"Candidate User"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AuthResponse auth = objectMapper.readTree(response).get("data").traverse(objectMapper)
                .readValueAs(AuthResponse.class);
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + auth.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0]").value("CANDIDATE"));
    }

    @Test
    @DisplayName("Registration accepts the whitelisted EMPLOYER role")
    void registrationAcceptsEmployer() throws Exception {
        String email = "employer_" + System.currentTimeMillis() + "@example.com";
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("Password123!");
        request.setFullName("Employer User");
        request.setRole(RegistrationRole.EMPLOYER);

        AuthResponse auth = authenticationService.register(request);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + auth.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0]").value("EMPLOYER"));
    }

    @Test
    @DisplayName("Registration rejects ADMIN and unknown roles")
    void registrationRejectsNonPublicRoles() throws Exception {
        for (String role : new String[]{"ADMIN", "SUPERUSER"}) {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"%s@example.com","password":"Password123!","fullName":"Invalid Role","role":"%s"}
                                    """.formatted(role.toLowerCase(), role)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("COMMON_400"));
        }
    }

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
                .andExpect(status().isUnauthorized());
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
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_008"));
    }

    @Test
    @DisplayName("Access secured endpoint without JWT returns 401")
    void testSecuredEndpointWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Malformed or tampered JWT returns 401")
    void malformedJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer malformed.jwt.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401"));
    }

    @Test
    @DisplayName("Configured development origin passes CORS preflight")
    void corsPreflightPasses() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin"))
                        .isEqualTo("http://localhost:5173"));
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
