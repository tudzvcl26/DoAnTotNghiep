package com.recruitment.ai.integration;

import com.recruitment.ai.dto.response.CareerChatResponse;
import com.recruitment.ai.service.CareerCompanionService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CareerCompanionAuthorizationIntegrationTest {

    private static final String SECRET =
            "4F8A9B72D35E1C847A91F6D28C5B9E73F84A9D21E6C4B7A15D8F2C93E7A4B6F18";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CareerCompanionService careerCompanionService;

    @Test
    void candidateCanChatWithStableVietnameseContractAndCandidateIdIsNotAnAuthority() throws Exception {
        when(careerCompanionService.chat(any())).thenReturn(new CareerChatResponse(
                "Bạn nên tập trung cải thiện Java và Spring Boot.", "vi", "ollama",
                "Qwen2.5:3B-Instruct", 0, 120, "correlation-test"));

        mockMvc.perform(post("/api/v1/ai/career/chat")
                        .header("Authorization", "Bearer " + token("CANDIDATE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"What skills should I improve?",
                                 "candidateId":"00000000-0000-0000-0000-000000000999"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answer").value("Bạn nên tập trung cải thiện Java và Spring Boot."))
                .andExpect(jsonPath("$.data.language").value("vi"))
                .andExpect(jsonPath("$.data.providerName").value("ollama"))
                .andExpect(jsonPath("$.data.correctionAttempts").value(0));
    }

    @Test
    void unauthenticatedEmployerAndAdminAreRejected() throws Exception {
        String body = "{\"message\":\"Tư vấn nghề nghiệp cho tôi\"}";
        mockMvc.perform(post("/api/v1/ai/career/chat").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bạn cần đăng nhập để sử dụng chức năng này."));
        mockMvc.perform(post("/api/v1/ai/career/chat").header("Authorization", "Bearer " + token("EMPLOYER"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Bạn không có quyền sử dụng chức năng này."));
        mockMvc.perform(post("/api/v1/ai/career/chat").header("Authorization", "Bearer " + token("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void emptyAndOversizedInputsReturnVietnameseValidationErrors() throws Exception {
        mockMvc.perform(post("/api/v1/ai/career/chat").header("Authorization", "Bearer " + token("CANDIDATE"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.message").isString());
        String oversized = "a".repeat(2001);
        mockMvc.perform(post("/api/v1/ai/career/chat").header("Authorization", "Bearer " + token("CANDIDATE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"" + oversized + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.message").value("Câu hỏi phải có từ 3 đến 2000 ký tự."));
    }

    private String token(String role) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder()
                .subject(role.toLowerCase() + "@example.test")
                .claim("token_type", "access")
                .claim("userId", UUID.randomUUID().toString())
                .claim("email", role.toLowerCase() + "@example.test")
                .claim("roles", List.of(role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
    }
}
