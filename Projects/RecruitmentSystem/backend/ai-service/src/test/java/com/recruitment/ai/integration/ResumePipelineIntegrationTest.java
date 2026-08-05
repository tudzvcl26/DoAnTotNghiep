package com.recruitment.ai.integration;

import com.recruitment.ai.entity.ModelDeployment;
import com.recruitment.ai.entity.PromptTemplateVersion;
import com.recruitment.ai.entity.enums.ModelCapability;
import com.recruitment.ai.provider.ModelRouter;
import com.recruitment.ai.provider.ProviderDescriptor;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationResult;
import com.recruitment.ai.repository.AiTaskRepository;
import com.recruitment.ai.repository.AnalysisKeywordItemRepository;
import com.recruitment.ai.repository.AnalysisSkillItemRepository;
import com.recruitment.ai.repository.ModelDeploymentRepository;
import com.recruitment.ai.repository.PromptTemplateVersionRepository;
import com.recruitment.ai.repository.ResumeAnalysisResultRepository;
import com.recruitment.ai.repository.ResumeDocumentRepository;
import com.recruitment.ai.repository.JobMatchResultRepository;
import com.recruitment.ai.repository.MatchScoreBreakdownRepository;
import com.recruitment.ai.repository.AiMatchExplanationRepository;
import com.recruitment.ai.repository.InterviewQuestionSetRepository;
import com.recruitment.ai.repository.JobRecommendationRepository;
import com.recruitment.ai.repository.CandidateRecommendationRepository;
import com.recruitment.ai.repository.AssistantSessionRepository;
import com.recruitment.ai.repository.AssistantResponseRepository;
import com.recruitment.ai.storage.AiStorageService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResumePipelineIntegrationTest {

    private static final String SECRET =
            "4F8A9B72D35E1C847A91F6D28C5B9E73F84A9D21E6C4B7A15D8F2C93E7A4B6F18";
    private static final String FACTS = """
            {
              "fullName":"Nguyen Van A","email":"candidate@example.com","phone":"+84901234567",
              "location":"Ho Chi Minh City","linkedIn":"https://linkedin.com/in/candidate",
              "portfolio":null,"summary":"Java backend engineer",
              "education":[{"school":"University","degree":"Software Engineering"}],
              "experience":[{"company":"Acme","title":"Backend Engineer"}],
              "projects":[{"name":"RecruitmentSystem","description":"Microservices platform"}],
              "skills":["Problem solving"],"technicalSkills":["Java","Spring Boot","PostgreSQL"],
              "softSkills":["Communication","Teamwork"],"languages":["Vietnamese","English"],
              "certificates":["Java Certificate"],"achievements":["Graduation project"],
              "keywords":["Java","Spring Boot","Microservices"]
            }
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ResumeDocumentRepository documentRepository;
    @Autowired
    private ResumeAnalysisResultRepository analysisRepository;
    @Autowired
    private AnalysisSkillItemRepository skillRepository;
    @Autowired
    private AnalysisKeywordItemRepository keywordRepository;
    @Autowired
    private AiTaskRepository taskRepository;
    @Autowired
    private PromptTemplateVersionRepository promptRepository;
    @Autowired
    private ModelDeploymentRepository modelRepository;
    @Autowired
    private JobMatchResultRepository matchRepository;
    @Autowired
    private MatchScoreBreakdownRepository matchBreakdownRepository;
    @Autowired
    private AiMatchExplanationRepository explanationRepository;
    @Autowired
    private InterviewQuestionSetRepository interviewRepository;
    @Autowired
    private JobRecommendationRepository jobRecommendationRepository;
    @Autowired
    private CandidateRecommendationRepository candidateRecommendationRepository;
    @Autowired
    private AssistantSessionRepository assistantSessionRepository;
    @Autowired
    private AssistantResponseRepository assistantResponseRepository;

    @MockitoBean
    private AiStorageService storageService;
    @MockitoBean
    private ModelRouter modelRouter;

    private StructuredGenerationProvider provider;
    private UUID ownerId;
    private String ownerToken;

    @BeforeEach
    void setUp() {
        assistantResponseRepository.deleteAllInBatch();
        assistantSessionRepository.deleteAllInBatch();
        jobRecommendationRepository.deleteAllInBatch();
        candidateRecommendationRepository.deleteAllInBatch();
        explanationRepository.deleteAllInBatch();
        interviewRepository.deleteAllInBatch();
        matchBreakdownRepository.deleteAllInBatch();
        matchRepository.deleteAllInBatch();
        keywordRepository.deleteAll();
        skillRepository.deleteAll();
        analysisRepository.deleteAll();
        taskRepository.deleteAll();
        documentRepository.deleteAll();
        promptRepository.deleteAll();
        modelRepository.deleteAll();

        PromptTemplateVersion prompt = new PromptTemplateVersion();
        prompt.setTemplateCode("RESUME_FACT_EXTRACTION");
        prompt.setVersionNumber(1);
        prompt.setSystemPrompt("Extract factual resume data only.");
        prompt.setUserPromptTemplate("{{resumeText}}");
        prompt.setOutputSchema("{\"type\":\"object\"}");
        prompt.setActive(true);
        promptRepository.saveAndFlush(prompt);

        ModelDeployment model = new ModelDeployment();
        model.setProviderName("test-provider");
        model.setModelName("test-structured-model");
        model.setDeploymentName("resume-test");
        model.setCapability(ModelCapability.STRUCTURED_GENERATION);
        model.setEnabled(true);
        model.setDefaultForCapability(true);
        modelRepository.saveAndFlush(model);

        provider = mock(StructuredGenerationProvider.class);
        when(provider.descriptor()).thenReturn(new ProviderDescriptor("test-provider", "TEST", true));
        when(provider.generate(any())).thenReturn(
                new StructuredGenerationResult("test-provider", "test-structured-model", FACTS, 120, 80)
        );
        when(modelRouter.structuredGenerationProvider()).thenReturn(provider);
        when(storageService.bucketName()).thenReturn("recruitment-ai-test");
        when(storageService.objectExists(any())).thenReturn(true);

        ownerId = UUID.randomUUID();
        ownerToken = token(ownerId, "candidate@example.com", List.of("CANDIDATE"));
    }

    @Test
    void uploadsExtractsAnalyzesScoresPersistsAndDeletesResume() throws Exception {
        UUID resumeId = upload(ownerToken);

        mockMvc.perform(post("/api/v1/ai/resumes/{id}/analyze", resumeId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .header("X-Correlation-Id", "resume-analysis-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.structuredData.fullName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.data.qualityScore").isNumber())
                .andExpect(jsonPath("$.data.scoreBreakdown.resumeCompleteness.maximum").value(15))
                .andExpect(jsonPath("$.data.skills.length()").value(6))
                .andExpect(jsonPath("$.data.keywords.length()").value(3))
                .andExpect(jsonPath("$.data.inputTokens").value(120))
                .andExpect(jsonPath("$.data.correlationId").value("resume-analysis-test"));

        mockMvc.perform(get("/api/v1/ai/resumes/{id}/analysis", resumeId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.modelName").value("test-structured-model"));

        mockMvc.perform(post("/api/v1/ai/resumes/{id}/analyze", resumeId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skills.length()").value(6))
                .andExpect(jsonPath("$.data.keywords.length()").value(3));

        org.assertj.core.api.Assertions.assertThat(analysisRepository.count()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(skillRepository.count()).isEqualTo(6);
        org.assertj.core.api.Assertions.assertThat(keywordRepository.count()).isEqualTo(3);

        mockMvc.perform(delete("/api/v1/ai/resumes/{id}", resumeId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        verify(storageService).delete(any());
        org.assertj.core.api.Assertions.assertThat(documentRepository.findById(resumeId)).isEmpty();
        org.assertj.core.api.Assertions.assertThat(analysisRepository.findByResumeDocumentId(resumeId)).isEmpty();
    }

    @Test
    void enforcesCandidateEmployerAdminAndOwnershipRules() throws Exception {
        UUID resumeId = upload(ownerToken);
        String otherCandidate = token(UUID.randomUUID(), "other@example.com", List.of("CANDIDATE"));
        String employer = token(UUID.randomUUID(), "employer@example.com", List.of("EMPLOYER"));
        String admin = token(UUID.randomUUID(), "admin@example.com", List.of("ADMIN"));

        mockMvc.perform(multipart("/api/v1/ai/resumes/upload").file(textResume()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/ai/resumes/{id}", resumeId)
                        .header("Authorization", "Bearer " + otherCandidate))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AI_RESUME_001"));

        mockMvc.perform(get("/api/v1/ai/resumes/{id}", resumeId)
                        .header("Authorization", "Bearer " + employer))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/ai/resumes/{id}", resumeId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ownerUserId").value(ownerId.toString()));

        UUID adminResumeId = upload(admin);
        mockMvc.perform(post("/api/v1/ai/resumes/{id}/analyze", adminResumeId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/ai/resumes/{id}", adminResumeId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        MockMultipartFile employerFile = textResume();
        mockMvc.perform(multipart("/api/v1/ai/resumes/upload").file(employerFile)
                        .header("Authorization", "Bearer " + employer))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsUnsupportedUploadWithBusinessError() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "file", "resume.png", "image/png", new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/v1/ai/resumes/upload").file(image)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("AI_RESUME_004"));
    }

    private UUID upload(String accessToken) throws Exception {
        String body = mockMvc.perform(multipart("/api/v1/ai/resumes/upload").file(textResume())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contentType").value("text/plain"))
                .andExpect(jsonPath("$.data.checksumSha256").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(body).path("data").path("id").asText());
        org.assertj.core.api.Assertions.assertThat(documentRepository.findById(id).orElseThrow().getExtractedText())
                .contains("Java", "Spring Boot");
        return id;
    }

    private MockMultipartFile textResume() {
        String content = """
                Nguyen Van A
                candidate@example.com | +84901234567
                Summary
                Java backend engineer
                Education
                Software Engineering University
                Experience
                Backend Engineer at Acme
                Skills
                Java, Spring Boot, PostgreSQL, Microservices
                Projects
                RecruitmentSystem graduation project
                """;
        return new MockMultipartFile("file", "resume.txt", "text/plain", content.getBytes());
    }

    private String token(UUID userId, String email, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder()
                .subject(email)
                .claim("token_type", "access")
                .claim("userId", userId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
    }
}
