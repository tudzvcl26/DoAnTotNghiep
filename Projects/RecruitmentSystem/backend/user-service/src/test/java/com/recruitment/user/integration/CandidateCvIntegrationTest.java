package com.recruitment.user.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.user.service.storage.StorageService;
import com.recruitment.user.service.ProfileService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CandidateCvIntegrationTest {
    private static final String SECRET = "4F8A9B72D35E1C847A91F6D28C5B9E73F84A9D21E6C4B7A15D8F2C93E7A4B6F18";
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProfileService profileService;
    @MockBean StorageService storageService;

    @Test
    void crudPdfAndAuthorizationAreOwnerScoped() throws Exception {
        UUID owner = UUID.randomUUID();
        String ownerToken = token(owner, "owner@cv.test", "CANDIDATE");
        String foreignToken = token(UUID.randomUUID(), "foreign@cv.test", "CANDIDATE");
        String employerToken = token(UUID.randomUUID(), "employer@cv.test", "EMPLOYER");
        String adminToken = token(UUID.randomUUID(), "admin@cv.test", "ADMIN");

        String body = payload("CV Backend", "classic", UUID.randomUUID());
        String response = mockMvc.perform(post("/api/v1/cvs").header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.personalInfo.fullName").value("Nguyễn Văn An"))
                .andExpect(jsonPath("$.data.candidateId").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        UUID cvId = UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());

        mockMvc.perform(get("/api/v1/cvs").header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(cvId.toString()));
        mockMvc.perform(get("/api/v1/cvs/" + cvId).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/cvs/" + cvId).header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(payload("CV Updated", "modern", UUID.randomUUID())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.title").value("CV Updated"));

        byte[] pdf = mockMvc.perform(get("/api/v1/cvs/" + cvId + "/pdf").header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(pdf).startsWith("%PDF".getBytes());

        for (String method : List.of("get", "put", "delete", "download")) {
            var request = switch (method) {
                case "put" -> put("/api/v1/cvs/" + cvId).contentType(MediaType.APPLICATION_JSON).content(body);
                case "delete" -> delete("/api/v1/cvs/" + cvId);
                case "download" -> get("/api/v1/cvs/" + cvId + "/pdf");
                default -> get("/api/v1/cvs/" + cvId);
            };
            mockMvc.perform(request.header("Authorization", "Bearer " + foreignToken)).andExpect(status().isNotFound());
        }
        mockMvc.perform(get("/api/v1/cvs/" + cvId).header("Authorization", "Bearer " + employerToken)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/cvs/" + cvId).header("Authorization", "Bearer " + adminToken)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/cvs/" + cvId)).andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/cvs/" + cvId).header("Authorization", "Bearer " + ownerToken)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/cvs/" + cvId).header("Authorization", "Bearer " + ownerToken)).andExpect(status().isNotFound());
    }

    @Test
    void createsIndependentVietnameseCvSnapshotFromAuthenticatedProfile() throws Exception {
        UUID candidate = UUID.randomUUID();
        String auth = "Bearer " + token(candidate, "profile-cv@example.test", "CANDIDATE");
        profileService.initialize(candidate, "Trần Minh Châu");

        String response = mockMvc.perform(post("/api/v1/cvs/from-profile").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"CV từ hồ sơ\",\"templateId\":\"student\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.language").value("vi"))
                .andExpect(jsonPath("$.data.content.personalInfo.fullName").value("Trần Minh Châu"))
                .andExpect(jsonPath("$.data.content.personalInfo.email").value("profile-cv@example.test"))
                .andReturn().getResponse().getContentAsString();
        UUID cvId = UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());
        mockMvc.perform(delete("/api/v1/cvs/" + cvId).header("Authorization", auth)).andExpect(status().isOk());
    }

    private String payload(String title, String template, UUID ignoredCandidateId) throws Exception {
        JsonNode content = objectMapper.readTree("""
                {"personalInfo":{"fullName":"Nguyễn Văn An","headline":"Kỹ sư phần mềm","email":"an@example.test","phone":"0900000000","location":"Hà Nội","website":"https://example.test"},"summary":"Phát triển sản phẩm tuyển dụng an toàn.","experiences":[{"position":"Kỹ sư","company":"Công ty Việt","startDate":"2022","endDate":"Hiện tại","description":"Xây dựng hệ thống."}],"education":[],"skills":["Java","Bảo mật"],"projects":[],"certifications":[],"awards":[],"activities":[]}
                """);
        var root = objectMapper.createObjectNode();
        root.put("title", title).put("templateId", template).put("language", "vi").put("candidateId", ignoredCandidateId.toString());
        root.set("content", content);
        return objectMapper.writeValueAsString(root);
    }

    private String token(UUID id, String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder().subject(email).claim("userId", id.toString()).claim("email", email)
                .claim("roles", List.of(role)).claim("token_type", "access").issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000)).signWith(key).compact();
    }
}
