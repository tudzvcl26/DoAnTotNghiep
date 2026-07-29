package com.recruitment.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.user.dto.request.CreateCandidatePreferenceRequest;
import com.recruitment.user.dto.request.CreateCertificateRequest;
import com.recruitment.user.dto.request.CreateEducationRequest;
import com.recruitment.user.dto.request.CreateExperienceRequest;
import com.recruitment.user.dto.request.CreateSkillRequest;
import com.recruitment.user.entity.AvailabilityStatus;
import com.recruitment.user.entity.EmploymentType;
import com.recruitment.user.entity.SkillLevel;
import com.recruitment.user.entity.WorkArrangement;
import com.recruitment.user.service.ProfileService;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private String generateToken(UUID userId, String email, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
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

}

