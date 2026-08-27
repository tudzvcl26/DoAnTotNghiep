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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmployerJobManagementIntegrationTest {

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
    private UUID ownerId;
    private UUID companyId;
    private UUID foreignCompanyId;
    private String employerToken;
    private String candidateToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        category = new JobCategory();
        category.setName("Employer management " + System.nanoTime());
        category.setSlug("employer-management-" + System.nanoTime());
        category.setActive(true);
        category = jobCategoryRepository.saveAndFlush(category);

        ownerId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        foreignCompanyId = UUID.randomUUID();
        employerToken = token(ownerId, "employer-management@example.test", "EMPLOYER");
        candidateToken = token(UUID.randomUUID(), "candidate-management@example.test", "CANDIDATE");
        adminToken = token(UUID.randomUUID(), "admin-management@example.test", "ADMIN");

        given(companyClient.getCompaniesByOwner(eq(ownerId), anyString()))
                .willReturn(List.of(new CompanyClientDto(companyId, ownerId)));
        given(companyClient.getCompanyById(companyId))
                .willReturn(Optional.of(new CompanyClientDto(companyId, ownerId)));
        given(companyClient.getCompanyById(foreignCompanyId))
                .willReturn(Optional.of(new CompanyClientDto(foreignCompanyId, UUID.randomUUID())));
    }

    @Test
    void employerListSupportsOwnershipPaginationSearchStatusAndSorting() throws Exception {
        Job alpha = saveJob("Owned Alpha Draft", JobStatus.DRAFT, companyId);
        Job zeta = saveJob("Owned Zeta Draft", JobStatus.DRAFT, companyId);
        saveJob("Owned Published", JobStatus.PUBLISHED, companyId);
        Job foreign = saveJob("Foreign Published", JobStatus.PUBLISHED, foreignCompanyId);

        mockMvc.perform(get("/api/v1/jobs/employer")
                        .header("Authorization", bearer(employerToken))
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "title,asc")
                        .param("status", "DRAFT")
                        .param("keyword", "Owned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.content[0].id").value(alpha.getId().toString()))
                .andExpect(jsonPath("$.data.content[?(@.id == '%s')]", foreign.getId()).isEmpty());

        mockMvc.perform(get("/api/v1/jobs/employer")
                        .header("Authorization", bearer(employerToken))
                        .param("page", "1")
                        .param("size", "1")
                        .param("sort", "title,asc")
                        .param("status", "DRAFT")
                        .param("keyword", "Owned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(zeta.getId().toString()));
    }

    @Test
    void ownerScopedDetailDeniesIdorWhilePublicAndAdminBehaviorRemainAvailable() throws Exception {
        Job ownedDraft = saveJob("Owned Private Draft", JobStatus.DRAFT, companyId);
        Job foreignPublished = saveJob("Foreign Public Job", JobStatus.PUBLISHED, foreignCompanyId);

        mockMvc.perform(get("/api/v1/jobs/employer/{id}", ownedDraft.getId())
                        .header("Authorization", bearer(employerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(ownedDraft.getId().toString()));

        mockMvc.perform(get("/api/v1/jobs/employer/{id}", foreignPublished.getId())
                        .header("Authorization", bearer(employerToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/jobs/employer/{id}", foreignPublished.getId())
                        .header("Authorization", bearer(candidateToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/jobs/employer/{id}", foreignPublished.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/jobs/employer/{id}", foreignPublished.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/jobs/{id}", foreignPublished.getId()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/jobs/public-search")
                        .param("keyword", "Foreign Public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void employerStatisticsAreOwnerScopedAndEmptyScopeIsStable() throws Exception {
        saveJob("Owned Draft", JobStatus.DRAFT, companyId);
        saveJob("Owned Published", JobStatus.PUBLISHED, companyId);
        saveJob("Owned Closed", JobStatus.CLOSED, companyId);
        saveJob("Foreign Published", JobStatus.PUBLISHED, foreignCompanyId);

        mockMvc.perform(get("/api/v1/jobs/employer/statistics")
                        .header("Authorization", bearer(employerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.draft").value(1))
                .andExpect(jsonPath("$.data.published").value(1))
                .andExpect(jsonPath("$.data.closed").value(1));

        UUID emptyOwner = UUID.randomUUID();
        String emptyEmployerToken = token(emptyOwner, "empty-employer@example.test", "EMPLOYER");
        given(companyClient.getCompaniesByOwner(eq(emptyOwner), anyString())).willReturn(List.of());
        mockMvc.perform(get("/api/v1/jobs/employer/statistics")
                        .header("Authorization", bearer(emptyEmployerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(get("/api/v1/jobs/employer")
                        .header("Authorization", bearer(employerToken))
                        .param("companyId", foreignCompanyId.toString()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/jobs/employer/statistics")
                        .header("Authorization", bearer(candidateToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/jobs/employer/statistics"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/jobs/employer/statistics")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(4));
    }

    private Job saveJob(String title, JobStatus status, UUID targetCompanyId) {
        Job job = new Job();
        job.setTitle(title);
        job.setJobCode("EMPLOYER_MGMT_" + UUID.randomUUID());
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setExperienceLevel(ExperienceLevel.JUNIOR);
        job.setStatus(status);
        job.setQuantity(1);
        job.setRemoteAllowed(false);
        job.setActive(true);
        job.setCompanyId(targetCompanyId);
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
