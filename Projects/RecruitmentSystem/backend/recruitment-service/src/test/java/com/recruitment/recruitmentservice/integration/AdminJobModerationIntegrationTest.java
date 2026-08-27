package com.recruitment.recruitmentservice.integration;

import com.recruitment.recruitmentservice.client.CompanyClient;
import com.recruitment.recruitmentservice.client.CompanyClientDto;
import com.recruitment.recruitmentservice.entity.Job;
import com.recruitment.recruitmentservice.entity.JobCategory;
import com.recruitment.recruitmentservice.entity.enums.EmploymentType;
import com.recruitment.recruitmentservice.entity.enums.ExperienceLevel;
import com.recruitment.recruitmentservice.entity.enums.JobStatus;
import com.recruitment.recruitmentservice.repository.JobCategoryRepository;
import com.recruitment.recruitmentservice.repository.JobRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminJobModerationIntegrationTest {

    private static final String SECRET = "4F8A9B72D35E1C847A91F6D28C5B9E73F84A9D21E6C4B7A15D8F2C93E7A4B6F18";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobCategoryRepository jobCategoryRepository;

    @Autowired
    private JobRepository jobRepository;

    @MockBean
    private CompanyClient companyClient;

    private JobCategory category;
    private String adminToken;
    private String employerToken;
    private String candidateToken;

    @BeforeEach
    void setUp() {
        category = new JobCategory();
        category.setName("Admin moderation " + System.nanoTime());
        category.setSlug("admin-moderation-" + System.nanoTime());
        category.setActive(true);
        category = jobCategoryRepository.saveAndFlush(category);
        adminToken = token(UUID.randomUUID(), "admin-moderation@example.test", "ADMIN");
        employerToken = token(UUID.randomUUID(), "employer-moderation@example.test", "EMPLOYER");
        candidateToken = token(UUID.randomUUID(), "candidate-moderation@example.test", "CANDIDATE");
    }

    @Test
    void adminCanListViewPublishCloseAndDeactivateJobs() throws Exception {
        Job draft = saveJob("Moderation draft", JobStatus.DRAFT, UUID.randomUUID());

        mockMvc.perform(get("/api/v1/admin/jobs")
                        .header("Authorization", bearer(adminToken))
                        .param("status", "DRAFT")
                        .param("keyword", "Moderation draft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(draft.getId().toString()));

        mockMvc.perform(get("/api/v1/admin/jobs/{jobId}", draft.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(patch("/api/v1/admin/jobs/{jobId}/publish", draft.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.publishedAt").isNotEmpty());

        mockMvc.perform(patch("/api/v1/admin/jobs/{jobId}/close", draft.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        mockMvc.perform(delete("/api/v1/admin/jobs/{jobId}", draft.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/jobs/{jobId}", draft.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_001"));
    }

    @Test
    void employerAndCandidateCannotUseAdminModerationContract() throws Exception {
        Job draft = saveJob("Protected moderation", JobStatus.DRAFT, UUID.randomUUID());

        for (String unauthorizedToken : List.of(employerToken, candidateToken)) {
            mockMvc.perform(get("/api/v1/admin/jobs").header("Authorization", bearer(unauthorizedToken)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/v1/admin/jobs/{jobId}", draft.getId()).header("Authorization", bearer(unauthorizedToken)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(patch("/api/v1/admin/jobs/{jobId}/publish", draft.getId()).header("Authorization", bearer(unauthorizedToken)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(delete("/api/v1/admin/jobs/{jobId}", draft.getId()).header("Authorization", bearer(unauthorizedToken)))
                    .andExpect(status().isForbidden());
        }

        mockMvc.perform(get("/api/v1/admin/jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void moderationReturnsDomainErrorsForMissingJobsAndInvalidTransitions() throws Exception {
        UUID missingId = UUID.randomUUID();
        Job draft = saveJob("Invalid transition", JobStatus.DRAFT, UUID.randomUUID());

        mockMvc.perform(get("/api/v1/admin/jobs/{jobId}", missingId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_001"));

        mockMvc.perform(patch("/api/v1/admin/jobs/{jobId}/publish", missingId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_001"));

        mockMvc.perform(patch("/api/v1/admin/jobs/{jobId}/close", draft.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("JOB_005"));
    }

    @Test
    void existingEmployerOwnershipContractRemainsEffective() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherEmployerId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Job draft = saveJob("Employer ownership", JobStatus.DRAFT, companyId);
        given(companyClient.getCompanyById(companyId))
                .willReturn(Optional.of(new CompanyClientDto(companyId, ownerId)));

        mockMvc.perform(patch("/api/v1/jobs/{jobId}/publish", draft.getId())
                        .header("Authorization", bearer(token(otherEmployerId, "other@example.test", "EMPLOYER"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/jobs/{jobId}/publish", draft.getId())
                        .header("Authorization", bearer(token(ownerId, "owner@example.test", "EMPLOYER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    private Job saveJob(String title, JobStatus status, UUID companyId) {
        Job job = new Job();
        job.setTitle(title);
        job.setJobCode("ADMIN_MOD_" + UUID.randomUUID());
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setExperienceLevel(ExperienceLevel.JUNIOR);
        job.setStatus(status);
        job.setQuantity(1);
        job.setRemoteAllowed(false);
        job.setActive(true);
        job.setCompanyId(companyId);
        job.setCategory(category);
        return jobRepository.saveAndFlush(job);
    }

    private String token(UUID userId, String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("email", email)
                .claim("roles", List.of(role))
                .claim("token_type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
