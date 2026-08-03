package com.recruitment.ai.integration;

import com.recruitment.ai.entity.AiTask;
import com.recruitment.ai.entity.enums.AiTaskStatus;
import com.recruitment.ai.repository.AiTaskRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiFoundationAuthorizationIntegrationTest {

    private static final String SECRET =
            "4F8A9B72D35E1C847A91F6D28C5B9E73F84A9D21E6C4B7A15D8F2C93E7A4B6F18";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiTaskRepository aiTaskRepository;

    @BeforeEach
    void cleanDatabase() {
        aiTaskRepository.deleteAll();
    }

    @Test
    void publicFoundationEndpointsAreAvailable() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.phase").value("COMPLETE"))
                .andExpect(jsonPath("$.data.aiProviderAvailable").value(false));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("AI Service API"));
    }

    @Test
    void jwtPurposeRbacAndTaskOwnershipAreEnforced() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        String ownerToken = token(ownerId, "owner@test.com", List.of("CANDIDATE"), "access");
        String otherToken = token(otherUserId, "other@test.com", List.of("CANDIDATE"), "access");
        String adminToken = token(adminId, "admin@test.com", List.of("ADMIN"), "access");
        String refreshToken = token(ownerId, "owner@test.com", List.of("CANDIDATE"), "refresh");

        AiTask task = new AiTask();
        task.setTaskType("FOUNDATION_CHECK");
        task.setStatus(AiTaskStatus.PENDING);
        task.setRequestedBy(ownerId);
        task.setCorrelationId(UUID.randomUUID().toString());
        task = aiTaskRepository.saveAndFlush(task);

        mockMvc.perform(get("/api/v1/ai/tasks/{taskId}", task.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/ai/tasks/{taskId}", task.getId())
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AI_AUTH_401"));

        mockMvc.perform(get("/api/v1/ai/tasks/{taskId}", task.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(task.getId().toString()));

        mockMvc.perform(get("/api/v1/ai/tasks/{taskId}", task.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AI_TASK_001"));

        mockMvc.perform(get("/api/v1/ai/tasks/{taskId}", task.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/ai/providers")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/ai/providers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phase").value("COMPLETE"))
                .andExpect(jsonPath("$.data.openAiConfigured").value(false))
                .andExpect(jsonPath("$.data.structuredGeneration.implementation").value("NO_OP"));
    }

    private String token(UUID userId, String email, List<String> roles, String tokenType) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder()
                .subject(email)
                .claim("token_type", tokenType)
                .claim("userId", userId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
    }

}
