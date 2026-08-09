package com.recruitment.recruitmentservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.recruitmentservice.client.CompanyClient;
import com.recruitment.recruitmentservice.client.CompanyClientDto;
import com.recruitment.recruitmentservice.dto.job.CreateJobRequest;
import com.recruitment.recruitmentservice.dto.job.JobResponse;
import com.recruitment.recruitmentservice.dto.job.UpdateJobRequest;
import com.recruitment.recruitmentservice.entity.JobCategory;
import com.recruitment.recruitmentservice.entity.enums.EmploymentType;
import com.recruitment.recruitmentservice.entity.enums.ExperienceLevel;
import com.recruitment.recruitmentservice.repository.JobCategoryRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JobAuthorizationIntegrationTest {

    private static final String SECRET = "4F8A9B72D35E1C847A91F6D28C5B9E73F84A9D21E6C4B7A15D8F2C93E7A4B6F18";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobCategoryRepository jobCategoryRepository;

    @MockBean
    private CompanyClient companyClient;

    private JobCategory testCategory;

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

    @BeforeEach
    void setUp() {
        if (testCategory == null) {
            JobCategory cat = new JobCategory();
            cat.setName("IT & Technology " + System.currentTimeMillis());
            cat.setSlug("it-tech-" + System.currentTimeMillis());
            cat.setActive(true);
            testCategory = jobCategoryRepository.save(cat);
        }
    }

    @Test
    @DisplayName("Task 5: Verify Public APIs (GET /jobs, GET /job-categories, GET /skills, GET /benefits) -> 200 OK without JWT")
    void testPublicReadEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/jobs"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/job-categories"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/skills"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/benefits"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Task 4, 6, 7: Job Ownership Authorization & IDOR Tests (Create, Update, Delete)")
    void testJobOwnershipFlow() throws Exception {
        UUID employer1UserId = UUID.randomUUID();
        UUID employer2UserId = UUID.randomUUID();
        UUID adminUserId = UUID.randomUUID();
        UUID candidateUserId = UUID.randomUUID();

        UUID company1Id = UUID.randomUUID();
        UUID company2Id = UUID.randomUUID();

        // Mock CompanyClient for cross-service ownership check
        given(companyClient.getCompanyById(company1Id))
                .willReturn(Optional.of(new CompanyClientDto(company1Id, employer1UserId)));
        given(companyClient.getCompanyById(company2Id))
                .willReturn(Optional.of(new CompanyClientDto(company2Id, employer2UserId)));

        String employer1Token = generateToken(employer1UserId, "emp1@test.com", List.of("EMPLOYER"));
        String employer2Token = generateToken(employer2UserId, "emp2@test.com", List.of("EMPLOYER"));
        String adminToken = generateToken(adminUserId, "admin@test.com", List.of("ADMIN"));
        String candidateToken = generateToken(candidateUserId, "cand@test.com", List.of("CANDIDATE"));

        CreateJobRequest createReq = new CreateJobRequest();
        createReq.setTitle("Senior Java Developer");
        createReq.setJobCode("JOB_" + System.currentTimeMillis());
        createReq.setEmploymentType(EmploymentType.FULL_TIME);
        createReq.setExperienceLevel(ExperienceLevel.SENIOR);
        createReq.setCompanyId(company1Id);
        createReq.setCategoryId(testCategory.getId());

        // 1. Candidate cannot create jobs -> 403 Forbidden
        mockMvc.perform(post("/api/v1/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());

        // 2. Protected endpoint without JWT -> 401 Unauthorized
        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isUnauthorized());

        // 3. EMPLOYER 1 creates job for Company 1 via HTTP -> 201 Created
        String createdJobJson = mockMvc.perform(post("/api/v1/jobs")
                        .header("Authorization", "Bearer " + employer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract job response
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(createdJobJson);
        com.fasterxml.jackson.databind.JsonNode jobNode = root.has("data") ? root.get("data") : root;
        UUID createdJobId = UUID.fromString(jobNode.get("id").asText());

        // 4. Candidate cannot update job -> 403 Forbidden
        UpdateJobRequest updateReq = new UpdateJobRequest();
        updateReq.setTitle("Lead Java Architect");
        updateReq.setJobCode(createReq.getJobCode());
        updateReq.setEmploymentType(EmploymentType.FULL_TIME);
        updateReq.setExperienceLevel(ExperienceLevel.SENIOR);
        updateReq.setCompanyId(company1Id);
        updateReq.setCategoryId(testCategory.getId());

        mockMvc.perform(put("/api/v1/jobs/" + createdJobId)
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());

        // 5. Candidate cannot delete job -> 403 Forbidden
        mockMvc.perform(delete("/api/v1/jobs/" + createdJobId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());

        // 6. EMPLOYER 1 updates own job -> 200 OK
        mockMvc.perform(put("/api/v1/jobs/" + createdJobId)
                        .header("Authorization", "Bearer " + employer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // The current owner cannot reassign a job to a company owned by another employer.
        updateReq.setCompanyId(company2Id);
        mockMvc.perform(put("/api/v1/jobs/" + createdJobId)
                        .header("Authorization", "Bearer " + employer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
        updateReq.setCompanyId(company1Id);

        // 7. EMPLOYER 2 attempts to update EMPLOYER 1's job -> 403 Forbidden (Cross-Service IDOR Protection)
        mockMvc.perform(put("/api/v1/jobs/" + createdJobId)
                        .header("Authorization", "Bearer " + employer2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());

        // 8. EMPLOYER 2 attempts to delete EMPLOYER 1's job -> 403 Forbidden (Cross-Service IDOR Protection)
        mockMvc.perform(delete("/api/v1/jobs/" + createdJobId)
                        .header("Authorization", "Bearer " + employer2Token))
                .andExpect(status().isForbidden());

        // 9. ADMIN updates EMPLOYER 1's job -> 200 OK (Admin Bypass)
        mockMvc.perform(put("/api/v1/jobs/" + createdJobId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // 10. EMPLOYER 1 deletes own job -> 200 OK / 204 No Content
        mockMvc.perform(delete("/api/v1/jobs/" + createdJobId)
                        .header("Authorization", "Bearer " + employer1Token))
                .andExpect(status().is2xxSuccessful());

        // 11. Non-existent job -> 404 Not Found
        mockMvc.perform(get("/api/v1/jobs/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Job lifecycle enforces ownership, legal transitions, and published-only public visibility")
    void testJobLifecycle() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherEmployerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        given(companyClient.getCompanyById(companyId))
                .willReturn(Optional.of(new CompanyClientDto(companyId, ownerId)));

        String ownerToken = generateToken(ownerId, "lifecycle-owner@test.com", List.of("EMPLOYER"));
        String otherEmployerToken = generateToken(otherEmployerId, "lifecycle-other@test.com", List.of("EMPLOYER"));
        String adminToken = generateToken(adminId, "lifecycle-admin@test.com", List.of("ADMIN"));
        String candidateToken = generateToken(candidateId, "lifecycle-candidate@test.com", List.of("CANDIDATE"));

        CreateJobRequest request = new CreateJobRequest();
        request.setTitle("Lifecycle Java Developer");
        request.setJobCode("LIFECYCLE_" + System.currentTimeMillis());
        request.setEmploymentType(EmploymentType.FULL_TIME);
        request.setExperienceLevel(ExperienceLevel.JUNIOR);
        request.setCompanyId(companyId);
        request.setCategoryId(testCategory.getId());

        String createdJobJson = mockMvc.perform(post("/api/v1/jobs")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        UUID jobId = UUID.fromString(
                objectMapper.readTree(createdJobJson).get("data").get("id").asText()
        );

        mockMvc.perform(get("/api/v1/jobs/" + jobId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/jobs/" + jobId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(get("/api/v1/jobs/" + jobId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(patch("/api/v1/jobs/" + jobId + "/publish")
                        .header("Authorization", "Bearer " + otherEmployerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/jobs/" + jobId + "/publish")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.publishedAt").isNotEmpty());

        mockMvc.perform(get("/api/v1/jobs/" + jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        mockMvc.perform(patch("/api/v1/jobs/" + jobId + "/publish")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("JOB_003"));

        mockMvc.perform(patch("/api/v1/jobs/" + jobId + "/close")
                        .header("Authorization", "Bearer " + otherEmployerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/jobs/" + jobId + "/close")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        mockMvc.perform(get("/api/v1/jobs/" + jobId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/jobs/" + jobId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        mockMvc.perform(patch("/api/v1/jobs/" + jobId + "/close")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("JOB_004"));
    }

}
