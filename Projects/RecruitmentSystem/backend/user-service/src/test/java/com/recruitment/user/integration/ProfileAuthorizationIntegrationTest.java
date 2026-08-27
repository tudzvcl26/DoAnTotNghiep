package com.recruitment.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.user.dto.request.CreateCandidatePreferenceRequest;
import com.recruitment.user.dto.request.CreateCertificateRequest;
import com.recruitment.user.dto.request.CreateEducationRequest;
import com.recruitment.user.dto.request.CreateExperienceRequest;
import com.recruitment.user.dto.request.CreateSkillRequest;
import com.recruitment.user.dto.request.UpdateCareerObjectiveRequest;
import com.recruitment.user.entity.AvailabilityStatus;
import com.recruitment.user.entity.EmploymentType;
import com.recruitment.user.entity.SkillLevel;
import com.recruitment.user.entity.WorkArrangement;
import com.recruitment.user.service.ProfileService;
import com.recruitment.user.service.storage.StorageService;
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
import org.springframework.mock.web.MockMultipartFile;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProfileAuthorizationIntegrationTest {

    private static final String SECRET = "4F8A9B72D35E1C847A91F6D28C5B9E73F84A9D21E6C4B7A15D8F2C93E7A4B6F18";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfileService profileService;

    @MockBean
    private StorageService storageService;

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
    @DisplayName("Candidate profile self-service enforces role and authenticated identity")
    void candidateProfileSelfServiceAuthorization() throws Exception {
        UUID candidateId = UUID.randomUUID();
        UUID employerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String candidateToken = generateToken(candidateId, "profile-owner@example.test", List.of("CANDIDATE"));
        String employerToken = generateToken(employerId, "profile-employer@example.test", List.of("EMPLOYER"));
        String adminToken = generateToken(adminId, "profile-admin@example.test", List.of("ADMIN"));

        mockMvc.perform(post("/api/v1/profiles/initialize")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Candidate Owner\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(candidateId.toString()));

        mockMvc.perform(get("/api/v1/profiles/me")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("Candidate Owner"));

        mockMvc.perform(put("/api/v1/profiles/me")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Candidate Updated\",\"profileVisibility\":\"PRIVATE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("Candidate Updated"));

        mockMvc.perform(post("/api/v1/profiles/initialize")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Employer Profile\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/profiles/me")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/profiles/initialize")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Admin Profile\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Task 3, 6, 7: Candidate Profile & Preference Ownership & IDOR Tests")
    void testProfilePreferenceOwnershipFlow() throws Exception {
        UUID candidate1UserId = UUID.randomUUID();
        UUID candidate2UserId = UUID.randomUUID();
        UUID adminUserId = UUID.randomUUID();

        String candidate1Token = generateToken(candidate1UserId, "cand1@test.com", List.of("CANDIDATE"));
        String candidate2Token = generateToken(candidate2UserId, "cand2@test.com", List.of("CANDIDATE"));
        String adminToken = generateToken(adminUserId, "admin@test.com", List.of("ADMIN"));

        // Initialize profiles for candidate 1 and candidate 2
        profileService.initialize(candidate1UserId, "Candidate One");
        profileService.initialize(candidate2UserId, "Candidate Two");

        // 1. Candidate 1 creates preference for own profile -> 200 OK
        CreateCandidatePreferenceRequest preferenceReq = new CreateCandidatePreferenceRequest();
        preferenceReq.setSalaryMinimum(new BigDecimal("1000"));
        preferenceReq.setSalaryMaximum(new BigDecimal("2000"));
        preferenceReq.setRecommendationConsent(false);
        preferenceReq.setAvailabilityStatus(AvailabilityStatus.ACTIVELY_LOOKING);
        preferenceReq.setWorkArrangement(WorkArrangement.HYBRID);

        mockMvc.perform(post("/api/v1/users/" + candidate1UserId + "/candidate-preference")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferenceReq)))
                .andExpect(status().isOk());

        // 2. Candidate 2 attempts to create preference for Candidate 1's profile -> 403 Forbidden (IDOR Protection)
        mockMvc.perform(post("/api/v1/users/" + candidate1UserId + "/candidate-preference")
                        .header("Authorization", "Bearer " + candidate2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferenceReq)))
                .andExpect(status().isForbidden());

        // 3. Admin deletes preference for Candidate 1 -> 200 OK (Admin Bypass)
        mockMvc.perform(delete("/api/v1/users/" + candidate1UserId + "/candidate-preference")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 4. Access without JWT -> 401 Unauthorized
        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Task 3, 7: Candidate Education Ownership & IDOR Tests")
    void testEducationOwnershipFlow() throws Exception {
        UUID candidate1UserId = UUID.randomUUID();
        UUID candidate2UserId = UUID.randomUUID();
        String candidate1Token = generateToken(candidate1UserId, "cand1_edu@test.com", List.of("CANDIDATE"));
        String candidate2Token = generateToken(candidate2UserId, "cand2_edu@test.com", List.of("CANDIDATE"));

        profileService.initialize(candidate1UserId, "Candidate Edu");
        profileService.initialize(candidate2UserId, "Candidate Edu 2");

        CreateEducationRequest eduReq = new CreateEducationRequest();
        eduReq.setInstitutionName("National University");
        eduReq.setQualification("Bachelor of Computer Science");
        eduReq.setFieldOfStudy("Computer Science");
        eduReq.setStartDate(LocalDate.of(2020, 9, 1));

        // 1. Candidate 1 creates Education for self -> 201 Created
        mockMvc.perform(post("/api/v1/users/" + candidate1UserId + "/educations")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eduReq)))
                .andExpect(status().is2xxSuccessful());

        // 2. Candidate 2 attempts to create Education for Candidate 1 -> 403 Forbidden (IDOR)
        mockMvc.perform(post("/api/v1/users/" + candidate1UserId + "/educations")
                        .header("Authorization", "Bearer " + candidate2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eduReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Task 3, 7: Candidate Experience Ownership & IDOR Tests")
    void testExperienceOwnershipFlow() throws Exception {
        UUID candidate1UserId = UUID.randomUUID();
        UUID candidate2UserId = UUID.randomUUID();
        String candidate1Token = generateToken(candidate1UserId, "cand1_exp@test.com", List.of("CANDIDATE"));
        String candidate2Token = generateToken(candidate2UserId, "cand2_exp@test.com", List.of("CANDIDATE"));

        profileService.initialize(candidate1UserId, "Candidate Exp");
        profileService.initialize(candidate2UserId, "Candidate Exp 2");

        CreateExperienceRequest expReq = new CreateExperienceRequest();
        expReq.setEmployerName("Tech Corp");
        expReq.setJobTitle("Software Engineer");
        expReq.setEmploymentType(EmploymentType.FULL_TIME);
        expReq.setStartDate(LocalDate.of(2022, 1, 1));
        expReq.setCurrent(false); // NOT NULL in DB — must be set explicitly via setter (Builder.Default does not apply)

        // 1. Candidate 1 creates Experience for self -> 201 Created
        mockMvc.perform(post("/api/v1/users/" + candidate1UserId + "/experiences")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expReq)))
                .andExpect(status().is2xxSuccessful());

        // 2. Candidate 2 attempts to create Experience for Candidate 1 -> 403 Forbidden (IDOR)
        mockMvc.perform(post("/api/v1/users/" + candidate1UserId + "/experiences")
                        .header("Authorization", "Bearer " + candidate2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Task 3, 7: Candidate Certificate & Skill Ownership & IDOR Tests")
    void testCertificateAndSkillOwnershipFlow() throws Exception {
        UUID candidate1UserId = UUID.randomUUID();
        UUID candidate2UserId = UUID.randomUUID();
        String candidate1Token = generateToken(candidate1UserId, "cand1_cert@test.com", List.of("CANDIDATE"));
        String candidate2Token = generateToken(candidate2UserId, "cand2_cert@test.com", List.of("CANDIDATE"));

        profileService.initialize(candidate1UserId, "Candidate Cert");
        profileService.initialize(candidate2UserId, "Candidate Cert 2");

        CreateCertificateRequest certReq = new CreateCertificateRequest();
        certReq.setCertificateName("AWS Certified Developer");
        certReq.setIssuerName("Amazon Web Services");
        certReq.setIssueDate(LocalDate.of(2023, 6, 1)); // NOT NULL in DB — must be supplied

        // Certificate: Candidate 1 creates for self -> 201, Candidate 2 blocked -> 403
        mockMvc.perform(post("/api/v1/users/" + candidate1UserId + "/certificates")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(certReq)))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post("/api/v1/users/" + candidate1UserId + "/certificates")
                        .header("Authorization", "Bearer " + candidate2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(certReq)))
                .andExpect(status().isForbidden());

        // Skill: Candidate 1 creates for self -> 201, Candidate 2 blocked -> 403
        CreateSkillRequest skillReq = new CreateSkillRequest();
        skillReq.setSkillName("Java Spring Boot");
        skillReq.setSkillLevel(SkillLevel.ADVANCED);
        skillReq.setYearsExperience(new BigDecimal("3.5"));

        mockMvc.perform(post("/api/v1/users/" + candidate1UserId + "/skills")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(skillReq)))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post("/api/v1/users/" + candidate1UserId + "/skills")
                        .header("Authorization", "Bearer " + candidate2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(skillReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Resume v1/v2 current selection, ownership, admin bypass and inactive behavior")
    void testResumeOwnershipAndVersioning() throws Exception {
        UUID candidate1 = UUID.randomUUID();
        UUID candidate2 = UUID.randomUUID();
        UUID employer = UUID.randomUUID();
        UUID admin = UUID.randomUUID();
        profileService.initialize(candidate1, "Resume Candidate");
        profileService.initialize(candidate2, "Other Candidate");
        String candidate1Token = generateToken(candidate1, "resume1@test.com", List.of("CANDIDATE"));
        String candidate2Token = generateToken(candidate2, "resume2@test.com", List.of("CANDIDATE"));
        String employerToken = generateToken(employer, "resume-employer@test.com", List.of("EMPLOYER"));
        String adminToken = generateToken(admin, "resume-admin@test.com", List.of("ADMIN"));
        given(storageService.getPresignedUrl(anyString())).willReturn("https://storage.invalid/immutable");

        MockMultipartFile v1 = new MockMultipartFile("file", "resume-v1.pdf", "application/pdf", "%PDF-1.7 resume-v1".getBytes());
        mockMvc.perform(multipart("/api/v1/users/" + candidate1 + "/resumes")
                        .file(v1).header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetVersion").value(1))
                .andExpect(jsonPath("$.data.current").value(true));

        MockMultipartFile v2 = new MockMultipartFile("file", "resume-v2.pdf", "application/pdf", "%PDF-1.7 resume-v2".getBytes());
        String v2Body = mockMvc.perform(multipart("/api/v1/users/" + candidate1 + "/resumes")
                        .file(v2).header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetVersion").value(2))
                .andReturn().getResponse().getContentAsString();
        UUID v2Id = UUID.fromString(objectMapper.readTree(v2Body).path("data").path("id").asText());

        mockMvc.perform(get("/api/v1/users/" + candidate1 + "/resumes/current")
                        .header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(v2Id.toString()))
                .andExpect(jsonPath("$.data.assetVersion").value(2));
        mockMvc.perform(get("/api/v1/users/" + candidate1 + "/resumes/current")
                        .header("Authorization", "Bearer " + candidate2Token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users/" + candidate1 + "/resumes/current")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users/" + candidate1 + "/resumes/current")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        profileService.initialize(employer, "Employer Self Profile");
        mockMvc.perform(get("/api/v1/users/" + employer + "/resumes")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users/" + candidate1 + "/resumes"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/users/" + candidate1 + "/resumes/" + v2Id)
                        .header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/users/" + candidate1 + "/resumes/current")
                        .header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Full nested-profile authorization matrix: owner, other candidate, employer, admin")
    void nestedResourceReadAuthorizationMatrix() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID otherCandidate = UUID.randomUUID();
        UUID employer = UUID.randomUUID();
        UUID admin = UUID.randomUUID();
        profileService.initialize(owner, "Matrix Owner");
        profileService.initialize(otherCandidate, "Matrix Other");
        String ownerToken = generateToken(owner, "matrix-owner@test.com", List.of("CANDIDATE"));
        String otherToken = generateToken(otherCandidate, "matrix-other@test.com", List.of("CANDIDATE"));
        String employerToken = generateToken(employer, "matrix-employer@test.com", List.of("EMPLOYER"));
        String adminToken = generateToken(admin, "matrix-admin@test.com", List.of("ADMIN"));

        UpdateCareerObjectiveRequest objective = new UpdateCareerObjectiveRequest();
        objective.setObjectiveText("Build secure recruitment systems");
        mockMvc.perform(put("/api/v1/users/" + owner + "/career-objective")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(objective)))
                .andExpect(status().isOk());

        CreateCandidatePreferenceRequest preference = new CreateCandidatePreferenceRequest();
        preference.setAvailabilityStatus(AvailabilityStatus.ACTIVELY_LOOKING);
        preference.setRecommendationConsent(false);
        mockMvc.perform(post("/api/v1/users/" + owner + "/candidate-preference")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preference)))
                .andExpect(status().isOk());

        List<String> nestedPaths = List.of(
                "/educations", "/experiences", "/skills", "/languages", "/certificates",
                "/social-links", "/career-objective", "/candidate-preference", "/assets"
        );
        for (String suffix : nestedPaths) {
            String path = "/api/v1/users/" + owner + suffix;
            mockMvc.perform(get(path).header("Authorization", "Bearer " + ownerToken))
                    .andExpect(status().isOk());
            mockMvc.perform(get(path).header("Authorization", "Bearer " + otherToken))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get(path).header("Authorization", "Bearer " + employerToken))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get(path).header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }
    }

}
