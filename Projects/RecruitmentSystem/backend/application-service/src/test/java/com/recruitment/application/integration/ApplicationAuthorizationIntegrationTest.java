package com.recruitment.application.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.application.client.CompanyClient;
import com.recruitment.application.client.CompanyClientDto;
import com.recruitment.application.client.JobClient;
import com.recruitment.application.client.JobClientDto;
import com.recruitment.application.client.UserClient;
import com.recruitment.application.client.UserClientDto;
import com.recruitment.application.client.ResumeClientDto;
import com.recruitment.application.dto.request.ApplyJobRequest;
import com.recruitment.application.dto.request.UpdateApplicationStatusRequest;
import com.recruitment.application.entity.enums.ApplicationStatus;
import com.recruitment.application.entity.Application;
import com.recruitment.application.entity.ApplicationStatusHistory;
import com.recruitment.application.repository.ApplicationRepository;
import com.recruitment.application.repository.CandidateProfileSnapshotRepository;
import com.recruitment.application.repository.ApplicationStatusHistoryRepository;
import com.recruitment.application.service.ResumeSnapshotStorage;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApplicationAuthorizationIntegrationTest {

    private static final String SECRET = "4F8A9B72D35E1C847A91F6D28C5B9E73F84A9D21E6C4B7A15D8F2C93E7A4B6F18";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobClient jobClient;

    @MockBean
    private UserClient userClient;

    @MockBean
    private CompanyClient companyClient;

    @MockBean
    private ResumeSnapshotStorage resumeSnapshotStorage;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CandidateProfileSnapshotRepository candidateProfileSnapshotRepository;

    @Autowired
    private ApplicationStatusHistoryRepository applicationStatusHistoryRepository;

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
    @DisplayName("Complete Application Lifecycle & Authorization Integration Test")
    void testApplicationLifecycleAndAuthorization() throws Exception {
        UUID candidateUserId = UUID.randomUUID();
        UUID candidate2UserId = UUID.randomUUID();
        UUID employerUserId = UUID.randomUUID();
        UUID employer2UserId = UUID.randomUUID();
        UUID adminUserId = UUID.randomUUID();

        UUID company1Id = UUID.randomUUID();
        UUID company2Id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        String candidateToken = generateToken(candidateUserId, "candidate@test.com", List.of("CANDIDATE"));
        String candidate2Token = generateToken(candidate2UserId, "candidate2@test.com", List.of("CANDIDATE"));
        String employerToken = generateToken(employerUserId, "employer@test.com", List.of("EMPLOYER"));
        String employer2Token = generateToken(employer2UserId, "employer2@test.com", List.of("EMPLOYER"));
        String adminToken = generateToken(adminUserId, "admin@test.com", List.of("ADMIN"));

        // Mock JobClient
        JobClientDto jobDto = JobClientDto.builder()
                .id(jobId)
                .title("Senior Backend Engineer")
                .jobCode("JOB_BE_01")
                .status("PUBLISHED")
                .companyId(company1Id)
                .rawJsonData("{\"id\":\"" + jobId + "\",\"title\":\"Senior Backend Engineer\"}")
                .build();
        given(jobClient.getJobById(jobId)).willReturn(Optional.of(jobDto));

        // Mock UserClient
        UserClientDto userDto = UserClientDto.builder()
                .id(UUID.randomUUID())
                .userId(candidateUserId)
                .displayName("John Candidate")
                .headline("Backend Engineer")
                .contactEmail("john.candidate@example.test")
                .contactPhone("0900000001")
                .version(7L)
                .rawJsonData("{\"userId\":\"" + candidateUserId + "\",\"displayName\":\"John Candidate\"}")
                .build();
        given(userClient.getCandidateProfile(eq(candidateUserId), any())).willReturn(Optional.of(userDto));
        ResumeClientDto resumeDto = ResumeClientDto.builder()
                .id(UUID.randomUUID())
                .storageKey(candidateUserId + "/resume/current.pdf")
                .originalFilename("resume.pdf")
                .contentType("application/pdf")
                .sizeBytes(1024L)
                .checksum("sha256-test")
                .assetVersion(1L)
                .rawJsonData("{\"id\":\"" + UUID.randomUUID() + "\",\"assetVersion\":1,\"storageKey\":\"immutable-v1.pdf\",\"originalFilename\":\"resume.pdf\",\"contentType\":\"application/pdf\"}")
                .build();
        given(userClient.getCurrentResume(eq(candidateUserId), any())).willReturn(Optional.of(resumeDto));
        given(resumeSnapshotStorage.download("immutable-v1.pdf")).willReturn("snapshot-pdf".getBytes());

        // Mock CompanyClient
        given(companyClient.getCompanyById(company1Id))
                .willReturn(Optional.of(CompanyClientDto.builder().id(company1Id).ownerId(employerUserId).name("Tech Corp").build()));
        given(companyClient.getCompanyById(company2Id))
                .willReturn(Optional.of(CompanyClientDto.builder().id(company2Id).ownerId(employer2UserId).name("Other Corp").build()));
        given(companyClient.getCompaniesByOwnerId(eq(employerUserId), any()))
                .willReturn(List.of(CompanyClientDto.builder().id(company1Id).ownerId(employerUserId).name("Tech Corp").build()));
        given(companyClient.getCompaniesByOwnerId(eq(employer2UserId), any()))
                .willReturn(List.of(CompanyClientDto.builder().id(company2Id).ownerId(employer2UserId).name("Other Corp").build()));

        // 1. Unauthenticated request -> 401 Unauthorized
        ApplyJobRequest applyReq = new ApplyJobRequest();
        applyReq.setJobId(jobId);
        applyReq.setCoverLetter("I am interested in this role.");

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyReq)))
                .andExpect(status().isUnauthorized());

        // 2. Candidate 1 applies -> 201 Created
        String responseContent = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.appliedAtInstant",
                        org.hamcrest.Matchers.matchesPattern(".*(?:Z|[+-]\\d{2}:\\d{2})$")))
                .andExpect(jsonPath("$.data.updatedAtInstant",
                        org.hamcrest.Matchers.matchesPattern(".*(?:Z|[+-]\\d{2}:\\d{2})$")))
                .andExpect(jsonPath("$.data.resumeSnapshot.snapshotData").value(org.hamcrest.Matchers.containsString("immutable-v1.pdf")))
                .andExpect(jsonPath("$.data.candidateProfileSnapshot.displayName").value("John Candidate"))
                .andExpect(jsonPath("$.data.candidateProfileSnapshot.headline").value("Backend Engineer"))
                .andExpect(jsonPath("$.data.candidateProfileSnapshot.profileVersion").value(7))
                .andReturn().getResponse().getContentAsString();

        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(responseContent);
        com.fasterxml.jackson.databind.JsonNode dataNode = root.has("data") ? root.get("data") : root;
        UUID applicationId = UUID.fromString(dataNode.get("id").asText());
        assertThat(candidateProfileSnapshotRepository.findByApplicationId(applicationId))
                .hasValueSatisfying(snapshot -> {
                    assertThat(snapshot.getDisplayName()).isEqualTo("John Candidate");
                    assertThat(snapshot.getContactEmail()).isEqualTo("john.candidate@example.test");
                    assertThat(snapshot.getCapturedAt()).isNotNull();
                });

        // A later profile update must not alter the data captured with this application.
        userDto.setDisplayName("John Candidate Updated");
        userDto.setHeadline("Principal Engineer");

        // 3. Duplicate apply -> 409 Conflict
        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyReq)))
                .andExpect(status().isConflict());

        // 4. Candidate get own applications list -> 200 OK
        mockMvc.perform(get("/api/v1/applications/my")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].jobSnapshot.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.data.content[0].jobSnapshot.snapshotData").value(org.hamcrest.Matchers.containsString("Senior Backend Engineer")))
                .andExpect(jsonPath("$.data.content[0].updatedAtInstant",
                        org.hamcrest.Matchers.matchesPattern(".*(?:Z|[+-]\\d{2}:\\d{2})$")));

        // 5. Candidate get application detail -> 200 OK
        mockMvc.perform(get("/api/v1/applications/" + applicationId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidateProfileSnapshot.displayName").value("John Candidate"))
                .andExpect(jsonPath("$.data.candidateProfileSnapshot.headline").value("Backend Engineer"));

        // 6. Employer 2 (unrelated company) attempts to view application -> 403 Forbidden (IDOR)
        mockMvc.perform(get("/api/v1/applications/" + applicationId)
                        .header("Authorization", "Bearer " + employer2Token))
                .andExpect(status().isForbidden());

        // 7. Employer 1 (job owner company) views application list for job -> 200 OK
        mockMvc.perform(get("/api/v1/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk());

        // 8. Employer 2 attempts to view application list for Job 1 -> 403 Forbidden
        mockMvc.perform(get("/api/v1/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + employer2Token))
                .andExpect(status().isForbidden());

        // Company-wide list derives ownership from JWT, supports paging/status/job filters,
        // and never returns Company 1's application to Employer 2.
        mockMvc.perform(get("/api/v1/applications/employer")
                        .param("page", "0").param("size", "1")
                        .param("status", "APPLIED").param("jobId", jobId.toString())
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(applicationId.toString()))
                .andExpect(jsonPath("$.data.content[0].candidateProfileSnapshot.displayName").value("John Candidate"));
        mockMvc.perform(get("/api/v1/applications/employer")
                        .header("Authorization", "Bearer " + employer2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        mockMvc.perform(get("/api/v1/applications/employer/statistics"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/applications/employer/statistics")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/applications/employer/statistics")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.applied").value(1))
                .andExpect(jsonPath("$.data.screening").value(0));
        mockMvc.perform(get("/api/v1/applications/employer/statistics")
                        .header("Authorization", "Bearer " + employer2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
        mockMvc.perform(get("/api/v1/applications/employer/statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/v1/admin/applications").header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/applications").param("companyId", company1Id.toString())
                        .param("candidateId", candidateUserId.toString()).param("status", "APPLIED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].candidateProfileSnapshot.displayName").value("John Candidate"));
        mockMvc.perform(get("/api/v1/admin/applications/" + applicationId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(applicationId.toString()));

        // Candidate owner and Employer owner can download the immutable snapshot;
        // unrelated Candidate/Employer identities are denied.
        mockMvc.perform(get("/api/v1/applications/" + applicationId + "/resume")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().bytes("snapshot-pdf".getBytes()));
        mockMvc.perform(get("/api/v1/applications/" + applicationId + "/resume")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/applications/" + applicationId + "/resume")
                        .header("Authorization", "Bearer " + employer2Token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/applications/" + applicationId + "/resume")
                        .header("Authorization", "Bearer " + candidate2Token))
                .andExpect(status().isForbidden());

        // 9. Employer 1 advances through the explicit APPLIED -> SCREENING -> INTERVIEW path
        UpdateApplicationStatusRequest updateStatusReq = new UpdateApplicationStatusRequest();
        updateStatusReq.setStatus(ApplicationStatus.SCREENING);
        updateStatusReq.setReasonCode("SCREENING_STARTED");

        mockMvc.perform(patch("/api/v1/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusHistory[1].changedAtInstant",
                        org.hamcrest.Matchers.matchesPattern(".*(?:Z|[+-]\\d{2}:\\d{2})$")))
                .andExpect(jsonPath("$.data.updatedAtInstant",
                        org.hamcrest.Matchers.matchesPattern(".*(?:Z|[+-]\\d{2}:\\d{2})$")));

        updateStatusReq.setStatus(ApplicationStatus.INTERVIEW);
        updateStatusReq.setReasonCode("INTERVIEW_SCHEDULED");
        mockMvc.perform(patch("/api/v1/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusReq)))
                .andExpect(status().isOk());

        // 10. Candidate 2 attempts to withdraw Candidate 1's application -> 403 Forbidden (IDOR)
        mockMvc.perform(patch("/api/v1/applications/" + applicationId + "/withdraw")
                        .header("Authorization", "Bearer " + candidate2Token))
                .andExpect(status().isForbidden());

        // 11. Admin bypasses check to update status -> 200 OK
        updateStatusReq.setStatus(ApplicationStatus.OFFER);
        mockMvc.perform(patch("/api/v1/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusReq)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Applying without a current resume fails with a stable 4xx code")
    void applyingWithoutCurrentResumeFails() throws Exception {
        UUID candidateId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        String token = generateToken(candidateId, "no-resume@test.com", List.of("CANDIDATE"));
        given(jobClient.getJobById(jobId)).willReturn(Optional.of(JobClientDto.builder()
                .id(jobId).companyId(companyId).status("PUBLISHED").rawJsonData("{}").build()));
        given(companyClient.getCompanyById(companyId)).willReturn(Optional.of(
                CompanyClientDto.builder().id(companyId).ownerId(UUID.randomUUID()).build()));
        given(userClient.getCandidateProfile(eq(candidateId), any())).willReturn(Optional.of(
                UserClientDto.builder().userId(candidateId).rawJsonData("{}").build()));
        given(userClient.getCurrentResume(eq(candidateId), any())).willReturn(Optional.empty());
        ApplyJobRequest request = new ApplyJobRequest();
        request.setJobId(jobId);

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("APP_010"));
    }

    @Test
    @DisplayName("Legacy zone-less application timestamps remain unchanged and gain no invented instant")
    void legacyZoneLessTimestampsArePreservedWithoutInventedInstant() throws Exception {
        UUID candidateId = UUID.randomUUID();
        LocalDateTime legacyAppliedAt = LocalDateTime.of(2024, 7, 1, 9, 30);
        LocalDateTime legacyChangedAt = LocalDateTime.of(2024, 7, 2, 14, 45);
        Application application = new Application();
        application.setCandidateId(candidateId);
        application.setCompanyId(UUID.randomUUID());
        application.setJobId(UUID.randomUUID());
        application.setStatus(ApplicationStatus.SCREENING);
        application.setAppliedAt(legacyAppliedAt);
        application.setAppliedAtInstant(null);
        application.setActive(true);
        application = applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplicationId(application.getId());
        history.setFromStatus(ApplicationStatus.APPLIED);
        history.setToStatus(ApplicationStatus.SCREENING);
        history.setChangedBy(candidateId);
        history.setChangedAt(legacyChangedAt);
        history.setChangedAtInstant(null);
        applicationStatusHistoryRepository.save(history);

        mockMvc.perform(get("/api/v1/applications/" + application.getId())
                        .header("Authorization", "Bearer " + generateToken(candidateId, "legacy@test.com", List.of("CANDIDATE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.appliedAt").value("2024-07-01T09:30:00"))
                .andExpect(jsonPath("$.data.appliedAtInstant").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data.updatedAtInstant").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data.statusHistory[0].changedAt").value("2024-07-02T14:45:00"))
                .andExpect(jsonPath("$.data.statusHistory[0].changedAtInstant").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("Application without a resume snapshot returns APP_011")
    void missingApplicationResumeReturnsNotFound() throws Exception {
        UUID candidateId = UUID.randomUUID();
        Application application = new Application();
        application.setCandidateId(candidateId);
        application.setCompanyId(UUID.randomUUID());
        application.setJobId(UUID.randomUUID());
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        application.setActive(true);
        application = applicationRepository.save(application);

        mockMvc.perform(get("/api/v1/applications/" + application.getId() + "/resume")
                        .header("Authorization", "Bearer " + generateToken(candidateId, "candidate@test.com", List.of("CANDIDATE"))))
                .andExpect(status().isNotFound());
    }

}
