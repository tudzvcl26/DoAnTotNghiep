package com.recruitment.company.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.company.dto.request.CreateCompanyRequest;
import com.recruitment.company.dto.request.UpdateCompanyRequest;
import com.recruitment.company.enums.CompanySize;
import com.recruitment.company.enums.CompanyType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CompanyAuthorizationIntegrationTest {

    private static final String SECRET = "4F8A9B72D35E1C847A91F6D28C5B9E73F84A9D21E6C4B7A15D8F2C93E7A4B6F18";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String generateToken(UUID userId, String email, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .claim("token_type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Task 5: GET /api/v1/companies is public -> 200 OK without JWT")
    void testPublicGetAllCompanies() throws Exception {
        mockMvc.perform(get("/api/v1/companies"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Task 2, 6, 7: Company Ownership Authorization & IDOR Tests (Create, Update, Delete)")
    void testCompanyOwnershipFlow() throws Exception {
        UUID employer1Id = UUID.randomUUID();
        UUID employer2Id = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();

        String employer1Token = generateToken(employer1Id, "emp1@company.com", List.of("EMPLOYER"));
        String employer2Token = generateToken(employer2Id, "emp2@company.com", List.of("EMPLOYER"));
        String adminToken = generateToken(adminId, "admin@company.com", List.of("ADMIN"));
        String candidateToken = generateToken(candidateId, "candidate@company.com", List.of("CANDIDATE"));

        // 1. CANDIDATE cannot create company -> 403 Forbidden
        CreateCompanyRequest createReq = new CreateCompanyRequest();
        createReq.setName("Company Alpha " + System.currentTimeMillis());
        createReq.setTaxCode("TAX" + System.currentTimeMillis());
        createReq.setEmail("alpha@company.com");
        createReq.setCompanyType(CompanyType.PRIVATE);
        createReq.setCompanySize(CompanySize.SMALL);

        mockMvc.perform(post("/api/v1/companies")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());

        // 2. Protected endpoint without JWT -> 401 Unauthorized
        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isUnauthorized());

        // 3. EMPLOYER 1 creates company -> 201 Created
        createReq.setName("Company Beta " + System.currentTimeMillis());
        createReq.setTaxCode("TAX2_" + System.currentTimeMillis());

        String responseJson = mockMvc.perform(post("/api/v1/companies")
                        .header("Authorization", "Bearer " + employer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract company ID from ApiResponse wrapper
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(responseJson);
        com.fasterxml.jackson.databind.JsonNode companyNode = root.has("data") ? root.get("data") : root;
        UUID company1Id = UUID.fromString(companyNode.get("id").asText());

        // 4. EMPLOYER 1 updates own company -> 200 OK
        UpdateCompanyRequest updateReq = new UpdateCompanyRequest();
        updateReq.setName("Company Beta Updated");

        mockMvc.perform(put("/api/v1/companies/" + company1Id)
                        .header("Authorization", "Bearer " + employer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // 5. EMPLOYER 2 attempts to update EMPLOYER 1's company -> 403 Forbidden (IDOR Protection)
        mockMvc.perform(put("/api/v1/companies/" + company1Id)
                        .header("Authorization", "Bearer " + employer2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());

        // 6. EMPLOYER 2 attempts to delete EMPLOYER 1's company -> 403 Forbidden (IDOR Protection)
        mockMvc.perform(delete("/api/v1/companies/" + company1Id)
                        .header("Authorization", "Bearer " + employer2Token))
                .andExpect(status().isForbidden());

        // 7. ADMIN updates EMPLOYER 1's company -> 200 OK (Admin Bypass)
        mockMvc.perform(put("/api/v1/companies/" + company1Id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // 8. EMPLOYER 1 deletes own company -> 200 OK / 204 No Content
        mockMvc.perform(delete("/api/v1/companies/" + company1Id)
                        .header("Authorization", "Bearer " + employer1Token))
                .andExpect(status().is2xxSuccessful());

        // 9. Non-existent company update -> 404 Not Found
        mockMvc.perform(put("/api/v1/companies/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }

}
