package com.recruitment.ai.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.entity.ModelDeployment;
import com.recruitment.ai.entity.PromptTemplateVersion;
import com.recruitment.ai.entity.enums.ModelCapability;
import com.recruitment.ai.matching.client.JobGateway;
import com.recruitment.ai.matching.model.JobSnapshot;
import com.recruitment.ai.messaging.RecommendationRefreshMessage;
import com.recruitment.ai.messaging.RecommendationRefreshPublisher;
import com.recruitment.ai.recommendation.CandidateConsentGateway;
import com.recruitment.ai.service.RecommendationService;
import com.recruitment.ai.provider.ModelRouter;
import com.recruitment.ai.provider.ProviderDescriptor;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationRequest;
import com.recruitment.ai.provider.llm.StructuredGenerationResult;
import com.recruitment.ai.repository.AiMatchExplanationRepository;
import com.recruitment.ai.repository.AiTaskRepository;
import com.recruitment.ai.repository.AnalysisKeywordItemRepository;
import com.recruitment.ai.repository.AnalysisSkillItemRepository;
import com.recruitment.ai.repository.JobMatchResultRepository;
import com.recruitment.ai.repository.InterviewQuestionSetRepository;
import com.recruitment.ai.repository.JobRecommendationRepository;
import com.recruitment.ai.repository.CandidateRecommendationRepository;
import com.recruitment.ai.repository.AssistantSessionRepository;
import com.recruitment.ai.repository.AssistantResponseRepository;
import com.recruitment.ai.repository.MatchScoreBreakdownRepository;
import com.recruitment.ai.repository.ModelDeploymentRepository;
import com.recruitment.ai.repository.PromptTemplateVersionRepository;
import com.recruitment.ai.repository.ResumeAnalysisResultRepository;
import com.recruitment.ai.repository.ResumeDocumentRepository;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.mockito.ArgumentCaptor;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatchingIntegrationTest {

    private static final String SECRET =
            "4F8A9B72D35E1C847A91F6D28C5B9E73F84A9D21E6C4B7A15D8F2C93E7A4B6F18";
    private static final String FACTS = """
            {"fullName":"Candidate","email":"candidate@example.com","phone":null,"location":"HCM",
             "linkedIn":null,"portfolio":null,"summary":"Java engineer",
             "education":[{"degree":"Bachelor of Software Engineering"}],
             "experience":[{"title":"Backend Engineer","duration":"3 years"}],
             "projects":[{"description":"Java Spring Boot microservices with PostgreSQL and Docker"}],
             "skills":["Problem solving"],"technicalSkills":["Java","Spring Boot","PostgreSQL","Docker"],
             "softSkills":["Communication","Teamwork"],"languages":["English","Vietnamese"],
             "certificates":[],"achievements":[],"keywords":["Java","Spring Boot","Microservices","PostgreSQL"]}
            """;
    private static final String EXPLANATION = """
            {"overallEvaluation":"Strong backend alignment grounded in the deterministic result.",
             "strengths":["Java and Spring Boot align"],"weaknesses":["One missing technology"],
             "highScoreReasons":["Required backend skills matched"],"lowScoreReasons":["A gap remains"],
             "missingTechnologies":["Microservices"],"careerSuggestions":["Deepen distributed systems knowledge"],
             "resumeImprovementChecklist":["Quantify project outcomes"],"skillRecommendations":["Practice Microservices patterns"],
             "projectRecommendations":["Add resilience patterns"],"certificationSuggestions":["Consider a Java certification"],
             "keywordImprovements":["Add truthful REST evidence"],"experienceImprovements":["Quantify three years of impact"],
             "educationImprovements":["Clarify relevant coursework"],
             "gapExplanations":[{"area":"Technical skills","gap":"Microservices","priority":"HIGH","explanation":"The job names this skill."}],
             "learningRoadmap":["Study service boundaries","Build a production sample"],
             "recommendedTechnologies":["Resilience4j"],"recommendedCertifications":["Oracle Java"],
             "portfolioImprovements":["Document architecture decisions"]}
            """;
    private static final String INTERVIEW = """
            {"technicalQuestions":[{"question":"Explain Spring transaction boundaries.","expectedAnswerOutline":"Propagation and rollback.","whyInterviewerAsks":"Tests backend depth.","relatedResumeSection":"Technical skills","difficulty":"HARD"}],
             "behavioralQuestions":[{"question":"Describe a difficult team decision.","expectedAnswerOutline":"Situation, action, result.","whyInterviewerAsks":"Tests collaboration.","relatedResumeSection":"Soft skills","difficulty":"MEDIUM"}],
             "hrQuestions":[{"question":"Why this role?","expectedAnswerOutline":"Role alignment and goals.","whyInterviewerAsks":"Tests motivation.","relatedResumeSection":"Summary","difficulty":"EASY"}],
             "projectQuestions":[{"question":"How did you design RecruitmentSystem?","expectedAnswerOutline":"Boundaries and tradeoffs.","whyInterviewerAsks":"Validates project ownership.","relatedResumeSection":"Projects","difficulty":"MEDIUM"}]}
            """;
    private static final String JOB_RECOMMENDATION = """
            {"recommendationSummary":"Strong published-job alignment.","gapSummary":"Review one missing skill.",
             "recommendationReason":"The deterministic match shows relevant backend evidence."}
            """;
    private static final String CANDIDATE_RECOMMENDATION = """
            {"recommendationSummary":"Relevant backend candidate.","interviewRecommendation":"Validate project depth.",
             "recommendationReason":"The deterministic match contains strong Java evidence."}
            """;
    private static final String ASSISTANT = """
            {"summary":"Grounded structured-data summary.","recommendations":["Review Java evidence"],
             "risks":["Validate the stated experience"],"nextSteps":["Discuss the project"]}
            """;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JobMatchResultRepository matchRepository;
    @Autowired private MatchScoreBreakdownRepository breakdownRepository;
    @Autowired private ResumeDocumentRepository documentRepository;
    @Autowired private ResumeAnalysisResultRepository analysisRepository;
    @Autowired private AnalysisSkillItemRepository skillRepository;
    @Autowired private AnalysisKeywordItemRepository keywordRepository;
    @Autowired private AiTaskRepository taskRepository;
    @Autowired private PromptTemplateVersionRepository promptRepository;
    @Autowired private ModelDeploymentRepository modelRepository;
    @Autowired private AiMatchExplanationRepository explanationRepository;
    @Autowired private InterviewQuestionSetRepository interviewRepository;
    @Autowired private JobRecommendationRepository jobRecommendationRepository;
    @Autowired private CandidateRecommendationRepository candidateRecommendationRepository;
    @Autowired private AssistantSessionRepository assistantSessionRepository;
    @Autowired private AssistantResponseRepository assistantResponseRepository;
    @Autowired private RecommendationService recommendationService;

    @MockitoBean private AiStorageService storageService;
    @MockitoBean private ModelRouter modelRouter;
    @MockitoBean private JobGateway jobGateway;
    @MockitoBean private CandidateConsentGateway consentGateway;
    @MockitoBean private RecommendationRefreshPublisher refreshPublisher;
    @MockitoBean private RedisTemplate<String, Object> aiRedisTemplate;

    private UUID candidateId;
    private UUID employerId;
    private UUID companyId;
    private String candidateToken;
    private String employerToken;

    @BeforeEach
    void setUp() {
        assistantResponseRepository.deleteAllInBatch();
        assistantSessionRepository.deleteAllInBatch();
        jobRecommendationRepository.deleteAllInBatch();
        candidateRecommendationRepository.deleteAllInBatch();
        explanationRepository.deleteAllInBatch();
        interviewRepository.deleteAllInBatch();
        breakdownRepository.deleteAllInBatch();
        matchRepository.deleteAllInBatch();
        keywordRepository.deleteAllInBatch();
        skillRepository.deleteAllInBatch();
        analysisRepository.deleteAllInBatch();
        taskRepository.deleteAllInBatch();
        documentRepository.deleteAllInBatch();
        promptRepository.deleteAllInBatch();
        modelRepository.deleteAllInBatch();

        PromptTemplateVersion prompt = new PromptTemplateVersion();
        prompt.setTemplateCode("RESUME_FACT_EXTRACTION");
        prompt.setVersionNumber(1);
        prompt.setSystemPrompt("Extract facts.");
        prompt.setUserPromptTemplate("{{resumeText}}");
        prompt.setOutputSchema("{\"type\":\"object\"}");
        prompt.setActive(true);
        promptRepository.saveAndFlush(prompt);

        promptRepository.saveAndFlush(prompt("MATCH_EXPLANATION", "Explain immutable scores."));
        promptRepository.saveAndFlush(prompt("INTERVIEW_PREPARATION", "Generate grounded questions."));
        promptRepository.saveAndFlush(prompt("JOB_RECOMMENDATION", "Candidate recommendation."));
        promptRepository.saveAndFlush(prompt("CANDIDATE_RECOMMENDATION", "Recruiter recommendation."));
        promptRepository.saveAndFlush(prompt("RECRUITER_ASSISTANT", "Recruiter structured assistant."));
        promptRepository.saveAndFlush(prompt("CANDIDATE_ASSISTANT", "Candidate structured assistant."));

        ModelDeployment model = new ModelDeployment();
        model.setProviderName("test");
        model.setModelName("structured-test");
        model.setDeploymentName("matching-test");
        model.setCapability(ModelCapability.STRUCTURED_GENERATION);
        model.setEnabled(true);
        model.setDefaultForCapability(true);
        modelRepository.saveAndFlush(model);

        StructuredGenerationProvider provider = mock(StructuredGenerationProvider.class);
        when(provider.descriptor()).thenReturn(new ProviderDescriptor("test", "TEST", true));
        when(provider.generate(any())).thenAnswer(invocation -> {
            StructuredGenerationRequest request = invocation.getArgument(0);
            String output = request.systemPrompt().contains("Explain immutable") ? EXPLANATION
                    : request.systemPrompt().contains("Generate grounded") ? INTERVIEW
                    : request.systemPrompt().contains("Candidate recommendation") ? JOB_RECOMMENDATION
                    : request.systemPrompt().contains("Recruiter recommendation") ? CANDIDATE_RECOMMENDATION
                    : request.systemPrompt().contains("structured assistant") ? ASSISTANT : FACTS;
            return new StructuredGenerationResult("test", "structured-test", output, 100, 50);
        });
        when(modelRouter.structuredGenerationProvider()).thenReturn(provider);
        when(storageService.bucketName()).thenReturn("matching-test");
        when(storageService.objectExists(any())).thenReturn(true);

        candidateId = UUID.randomUUID();
        employerId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        candidateToken = token(candidateId, "candidate@example.com", List.of("CANDIDATE"));
        employerToken = token(employerId, "employer@example.com", List.of("EMPLOYER"));
        when(jobGateway.getJob(any(), any())).thenAnswer(invocation -> publishedJob(invocation.getArgument(0), employerId));
        when(jobGateway.getPublishedJobs(any())).thenAnswer(invocation ->
                List.of(publishedJob(UUID.randomUUID(), employerId)));
        when(consentGateway.hasConsent(any(), any())).thenReturn(true);
    }

    @Test
    void ranksRecommendationsRunsStructuredAssistantsAndEnforcesOwnership() throws Exception {
        UUID resumeId = analyzedResume();
        UUID jobId = UUID.randomUUID();
        when(jobGateway.getPublishedJobs(any())).thenReturn(List.of(publishedJob(jobId, employerId)));

        mockMvc.perform(get("/api/v1/ai/recommendations/jobs")
                        .param("resumeId", resumeId.toString()).param("sort", "overallScore")
                        .header("Authorization", "Bearer " + candidateToken)
                        .header("X-Correlation-Id", "job-recommendations"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(0));
        assertThat(matchRepository.count()).isZero();
        assertThat(jobRecommendationRepository.count()).isZero();
        verifyNoInteractions(refreshPublisher);
        mockMvc.perform(post("/api/v1/ai/recommendations/jobs/refresh")
                        .param("resumeId", resumeId.toString())
                        .header("Authorization", "Bearer " + candidateToken)
                        .header("X-Correlation-Id", "job-recommendations"))
                .andExpect(status().isAccepted()).andExpect(jsonPath("$.data.status").value("PENDING"));
        ArgumentCaptor<RecommendationRefreshMessage> refresh = ArgumentCaptor.forClass(RecommendationRefreshMessage.class);
        verify(refreshPublisher).publish(refresh.capture());
        recommendationService.processJobRefresh(refresh.getValue());

        String jobsBody = mockMvc.perform(get("/api/v1/ai/recommendations/jobs")
                        .param("resumeId", resumeId.toString()).param("sort", "overallScore")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].overallScore").isNumber())
                .andExpect(jsonPath("$.data.content[0].recommendation.recommendationSummary").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].promptVersion").value("JOB_RECOMMENDATION:v1"))
                .andReturn().getResponse().getContentAsString();
        UUID jobRecommendationId = UUID.fromString(objectMapper.readTree(jobsBody)
                .path("data").path("content").path(0).path("id").asText());
        mockMvc.perform(get("/api/v1/ai/recommendations/jobs/{id}", jobRecommendationId)
                        .header("Authorization", "Bearer " + candidateToken)).andExpect(status().isOk());

        String candidatesBody = mockMvc.perform(get("/api/v1/ai/recommendations/candidates")
                        .param("jobId", jobId.toString()).param("minScore", "1")
                        .header("Authorization", "Bearer " + employerToken)
                        .header("X-Correlation-Id", "candidate-recommendations"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].recommendation.interviewRecommendation").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].promptVersion").value("CANDIDATE_RECOMMENDATION:v1"))
                .andReturn().getResponse().getContentAsString();
        UUID candidateRecommendationId = UUID.fromString(objectMapper.readTree(candidatesBody)
                .path("data").path("content").path(0).path("id").asText());

        mockMvc.perform(post("/api/v1/ai/assistant/candidate")
                        .contentType("application/json")
                        .content("{\"task\":\"CAREER_ROADMAP\",\"resumeId\":\"" + resumeId + "\"}")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.assistantType").value("CANDIDATE"))
                .andExpect(jsonPath("$.data.response.summary").isNotEmpty())
                .andExpect(jsonPath("$.data.promptVersion").value("CANDIDATE_ASSISTANT:v1"));
        mockMvc.perform(post("/api/v1/ai/assistant/recruiter")
                        .contentType("application/json")
                        .content("{\"task\":\"SUGGEST_INTERVIEW_FOCUS\",\"jobId\":\"" + jobId
                                + "\",\"resumeId\":\"" + resumeId + "\"}")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.assistantType").value("RECRUITER"))
                .andExpect(jsonPath("$.data.response.nextSteps").isArray());

        String otherCandidate = token(UUID.randomUUID(), "other@example.com", List.of("CANDIDATE"));
        mockMvc.perform(get("/api/v1/ai/recommendations/jobs/{id}", jobRecommendationId)
                        .header("Authorization", "Bearer " + otherCandidate)).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/ai/assistant/candidate").contentType("application/json")
                        .content("{\"task\":\"RESUME_IMPROVEMENT\",\"resumeId\":\"" + resumeId + "\"}")
                        .header("Authorization", "Bearer " + otherCandidate)).andExpect(status().isNotFound());

        String wrongEmployer = token(UUID.randomUUID(), "wrong@example.com", List.of("EMPLOYER"));
        mockMvc.perform(get("/api/v1/ai/recommendations/candidates/{id}", candidateRecommendationId)
                        .header("Authorization", "Bearer " + wrongEmployer)).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/ai/assistant/recruiter").contentType("application/json")
                        .content("{\"task\":\"SUMMARIZE_JOB\",\"jobId\":\"" + jobId + "\"}")
                        .header("Authorization", "Bearer " + wrongEmployer)).andExpect(status().isForbidden());

        String admin = token(UUID.randomUUID(), "admin@example.com", List.of("ADMIN"));
        mockMvc.perform(get("/api/v1/ai/recommendations/jobs/{id}", jobRecommendationId)
                        .header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ai/recommendations/candidates/{id}", candidateRecommendationId)
                        .header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ai/recommendations/jobs")
                        .param("resumeId", resumeId.toString())
                        .header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ai/recommendations/candidates")
                        .param("jobId", jobId.toString())
                        .header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/ai/assistant/candidate")
                        .contentType("application/json")
                        .content("{\"task\":\"RESUME_IMPROVEMENT\",\"resumeId\":\"" + resumeId + "\"}")
                        .header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/ai/assistant/recruiter")
                        .contentType("application/json")
                        .content("{\"task\":\"SUMMARIZE_JOB\",\"jobId\":\"" + jobId + "\"}")
                        .header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
        assertThat(jobRecommendationRepository.count()).isEqualTo(1);
        assertThat(candidateRecommendationRepository.count()).isEqualTo(1);
        assertThat(assistantSessionRepository.count()).isEqualTo(4);
        assertThat(assistantResponseRepository.count()).isEqualTo(4);
    }

    @Test
    void matchesPersistsAndReturnsVersionedDeterministicResult() throws Exception {
        UUID resumeId = analyzedResume();
        UUID jobId = UUID.randomUUID();
        String body = mockMvc.perform(post("/api/v1/ai/matching/jobs/{jobId}/resumes/{resumeId}", jobId, resumeId)
                        .header("Authorization", "Bearer " + candidateToken)
                        .header("X-Correlation-Id", "matching-integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallScore").isNumber())
                .andExpect(jsonPath("$.data.scoreBreakdown.length()").value(8))
                .andExpect(jsonPath("$.data.matchedSkills").isArray())
                .andExpect(jsonPath("$.data.missingSkills").isArray())
                .andExpect(jsonPath("$.data.gapAnalysis").isArray())
                .andExpect(jsonPath("$.data.ruleVersion").value("rules-v1"))
                .andExpect(jsonPath("$.data.weightsVersion").value("weights-v1"))
                .andExpect(jsonPath("$.data.correlationId").value("matching-integration"))
                .andReturn().getResponse().getContentAsString();
        UUID matchId = UUID.fromString(objectMapper.readTree(body).path("data").path("id").asText());

        mockMvc.perform(post("/api/v1/ai/matching/{id}/explanation", matchId)
                        .header("Authorization", "Bearer " + candidateToken)
                        .header("X-Correlation-Id", "explanation-integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.explanation.overallEvaluation").isNotEmpty())
                .andExpect(jsonPath("$.data.explanation.gapExplanations[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.data.promptVersion").value("MATCH_EXPLANATION:v1"))
                .andExpect(jsonPath("$.data.inputTokens").value(100))
                .andExpect(jsonPath("$.data.correlationId").value("explanation-integration"));
        mockMvc.perform(post("/api/v1/ai/matching/{id}/interview", matchId)
                        .header("Authorization", "Bearer " + candidateToken)
                        .header("X-Correlation-Id", "interview-integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questionSet.technicalQuestions[0].difficulty").value("HARD"))
                .andExpect(jsonPath("$.data.questionSet.behavioralQuestions.length()").value(1))
                .andExpect(jsonPath("$.data.promptVersion").value("INTERVIEW_PREPARATION:v1"))
                .andExpect(jsonPath("$.data.correlationId").value("interview-integration"));
        mockMvc.perform(get("/api/v1/ai/matching/{id}/explanation", matchId)
                        .header("Authorization", "Bearer " + candidateToken)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ai/matching/{id}/interview", matchId)
                        .header("Authorization", "Bearer " + candidateToken)).andExpect(status().isOk());
        assertThat(explanationRepository.count()).isEqualTo(1);
        assertThat(interviewRepository.count()).isEqualTo(1);

        mockMvc.perform(post("/api/v1/ai/matching/jobs/{jobId}/resumes/{resumeId}", jobId, resumeId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());
        assertThat(matchRepository.count()).isEqualTo(1);
        assertThat(breakdownRepository.count()).isEqualTo(8);

        mockMvc.perform(get("/api/v1/ai/matching/{id}", matchId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ai/matching/job/{jobId}", jobId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
        mockMvc.perform(get("/api/v1/ai/matching/resume/{resumeId}", resumeId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void recommendationRefreshRequiresConsentAndCandidateRole() throws Exception {
        UUID resumeId = analyzedResume();
        when(consentGateway.hasConsent(any(), any())).thenReturn(false);
        mockMvc.perform(post("/api/v1/ai/recommendations/jobs/refresh")
                        .param("resumeId", resumeId.toString())
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("AI_RECOMMENDATION_005"));
        mockMvc.perform(get("/api/v1/ai/recommendations/jobs")
                        .param("resumeId", resumeId.toString())
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/v1/ai/recommendations/jobs/refresh")
                        .param("resumeId", resumeId.toString())
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isForbidden());
        assertThat(jobRecommendationRepository.count()).isZero();
    }

    @Test
    void enforcesCandidateEmployerAdminAndOwnershipRules() throws Exception {
        UUID resumeId = analyzedResume();
        UUID jobId = UUID.randomUUID();
        String employerBody = mockMvc.perform(post("/api/v1/ai/matching/jobs/{jobId}/resumes/{resumeId}", jobId, resumeId)
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        UUID matchId = UUID.fromString(objectMapper.readTree(employerBody).path("data").path("id").asText());

        mockMvc.perform(post("/api/v1/ai/matching/{id}/explanation", matchId)
                        .header("Authorization", "Bearer " + employerToken)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/ai/matching/{id}/interview", matchId)
                        .header("Authorization", "Bearer " + employerToken)).andExpect(status().isOk());

        String otherCandidate = token(UUID.randomUUID(), "other@example.com", List.of("CANDIDATE"));
        mockMvc.perform(post("/api/v1/ai/matching/jobs/{jobId}/resumes/{resumeId}", jobId, resumeId)
                        .header("Authorization", "Bearer " + otherCandidate))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("AI_RESUME_001"));
        mockMvc.perform(get("/api/v1/ai/matching/{id}", matchId)
                        .header("Authorization", "Bearer " + otherCandidate))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("AI_MATCH_001"));
        mockMvc.perform(get("/api/v1/ai/matching/{id}/explanation", matchId)
                        .header("Authorization", "Bearer " + otherCandidate)).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/ai/matching/{id}/interview", matchId)
                        .header("Authorization", "Bearer " + otherCandidate)).andExpect(status().isNotFound());

        String wrongEmployer = token(UUID.randomUUID(), "wrong@example.com", List.of("EMPLOYER"));
        mockMvc.perform(post("/api/v1/ai/matching/jobs/{jobId}/resumes/{resumeId}", jobId, resumeId)
                        .header("Authorization", "Bearer " + wrongEmployer))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/ai/matching/{id}", matchId)
                        .header("Authorization", "Bearer " + wrongEmployer))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/ai/matching/{id}/interview", matchId)
                        .header("Authorization", "Bearer " + wrongEmployer)).andExpect(status().isNotFound());

        String admin = token(UUID.randomUUID(), "admin@example.com", List.of("ADMIN"));
        mockMvc.perform(get("/api/v1/ai/matching/{id}", matchId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ai/matching/{id}/explanation", matchId)
                        .header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ai/matching/{id}/interview", matchId)
                        .header("Authorization", "Bearer " + admin)).andExpect(status().isOk());

        String adminWithBusinessRoles = token(UUID.randomUUID(), "multi-role-admin@example.com",
                List.of("ADMIN", "CANDIDATE", "EMPLOYER"));
        mockMvc.perform(post("/api/v1/ai/matching/jobs/{jobId}/resumes/{resumeId}", jobId, resumeId)
                        .header("Authorization", "Bearer " + adminWithBusinessRoles))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsMatchingForDraftJob() throws Exception {
        UUID resumeId = analyzedResume();
        UUID jobId = UUID.randomUUID();
        when(jobGateway.getJob(any(), any())).thenReturn(new JobSnapshot(jobId, "Java Engineer", "", "Java", "",
                "JUNIOR", "DRAFT", true, companyId, employerId));
        mockMvc.perform(post("/api/v1/ai/matching/jobs/{jobId}/resumes/{resumeId}", jobId, resumeId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("AI_MATCH_004"));
    }

    private UUID analyzedResume() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain",
                "Java Spring Boot engineer with PostgreSQL Docker experience".getBytes());
        String upload = mockMvc.perform(multipart("/api/v1/ai/resumes/upload").file(file)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        UUID resumeId = UUID.fromString(objectMapper.readTree(upload).path("data").path("id").asText());
        mockMvc.perform(post("/api/v1/ai/resumes/{id}/analyze", resumeId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());
        return resumeId;
    }

    private JobSnapshot publishedJob(UUID jobId, UUID ownerId) {
        return new JobSnapshot(jobId, "Senior Java Backend Engineer",
                "Build Spring Boot microservices using PostgreSQL and Docker.",
                "3 years experience. Required Java, Spring Boot, PostgreSQL. English and communication.",
                "Develop REST services.", "MIDDLE", "PUBLISHED", true, companyId, ownerId);
    }

    private PromptTemplateVersion prompt(String code, String systemPrompt) {
        PromptTemplateVersion prompt = new PromptTemplateVersion();
        prompt.setTemplateCode(code);
        prompt.setVersionNumber(1);
        prompt.setSystemPrompt(systemPrompt);
        prompt.setUserPromptTemplate("{{context}}");
        prompt.setOutputSchema("{\"type\":\"object\"}");
        prompt.setActive(true);
        return prompt;
    }

    private String token(UUID userId, String email, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder().subject(email).claim("token_type", "access")
                .claim("userId", userId.toString()).claim("email", email).claim("roles", roles)
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key).compact();
    }
}
