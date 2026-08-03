package com.recruitment.ai.repository;

import com.recruitment.ai.entity.AiTask;
import com.recruitment.ai.entity.AiMatchExplanation;
import com.recruitment.ai.entity.InterviewQuestionSet;
import com.recruitment.ai.entity.JobMatchResult;
import com.recruitment.ai.entity.MatchScoreBreakdown;
import com.recruitment.ai.entity.ModelDeployment;
import com.recruitment.ai.entity.PromptTemplateVersion;
import com.recruitment.ai.entity.ResumeAnalysisResult;
import com.recruitment.ai.entity.ResumeDocument;
import com.recruitment.ai.entity.enums.AiTaskStatus;
import com.recruitment.ai.entity.enums.ModelCapability;
import com.recruitment.ai.entity.enums.ResumeDocumentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class JobMatchResultRepositoryTest {

    @Autowired private JobMatchResultRepository matchRepository;
    @Autowired private ResumeDocumentRepository documentRepository;
    @Autowired private ResumeAnalysisResultRepository analysisRepository;
    @Autowired private AiTaskRepository taskRepository;
    @Autowired private PromptTemplateVersionRepository promptRepository;
    @Autowired private ModelDeploymentRepository modelRepository;
    @Autowired private AiMatchExplanationRepository explanationRepository;
    @Autowired private InterviewQuestionSetRepository interviewRepository;

    @Test
    void queriesResultsByJobResumeAndOwners() {
        UUID resumeOwner = UUID.randomUUID();
        UUID jobOwner = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        ResumeAnalysisResult analysis = analysis(resumeOwner);

        JobMatchResult result = new JobMatchResult();
        result.setResumeAnalysisResult(analysis);
        result.setResumeDocumentId(analysis.getResumeDocument().getId());
        result.setResumeOwnerUserId(resumeOwner);
        result.setJobId(jobId);
        result.setJobCompanyId(UUID.randomUUID());
        result.setJobOwnerUserId(jobOwner);
        result.setOverallScore(75);
        result.setMatchedSkills("[\"Java\"]");
        result.setMissingSkills("[]");
        result.setMatchedKeywords("[\"java\"]");
        result.setMissingKeywords("[]");
        result.setStrengths("[]");
        result.setWeaknesses("[]");
        result.setRecommendations("[]");
        result.setGapAnalysis("[]");
        result.setMatchedExperience("Experience matched.");
        result.setMatchedEducation("Education matched.");
        result.setRuleVersion("rules-v1");
        result.setWeightsVersion("weights-v1");
        result.setWeightsSnapshot("{\"technicalSkills\":40}");
        result.setMatchingDurationMs(3L);
        result.setCorrelationId("repository-test");
        MatchScoreBreakdown breakdown = new MatchScoreBreakdown();
        breakdown.setMatchResult(result);
        breakdown.setDimensionCode("technicalSkills");
        breakdown.setMaximumScore(40);
        breakdown.setActualScore(30);
        breakdown.setReason("Matched.");
        breakdown.setOrdinalPosition(0);
        result.getBreakdowns().add(breakdown);
        matchRepository.saveAndFlush(result);

        AiMatchExplanation explanation = new AiMatchExplanation();
        explanation.setMatchResult(result); explanation.setAiTask(analysis.getAiTask());
        explanation.setPromptTemplateVersion(analysis.getPromptTemplateVersion());
        explanation.setModelDeployment(analysis.getModelDeployment()); explanation.setProviderName("test");
        explanation.setModelName("test"); explanation.setPromptVersion("MATCH_EXPLANATION:v1");
        explanation.setExplanationData("{\"overallEvaluation\":\"test\"}"); explanation.setInputTokens(1L);
        explanation.setOutputTokens(1L); explanation.setGenerationDurationMs(1L); explanation.setCorrelationId("repository-test");
        explanationRepository.saveAndFlush(explanation);

        InterviewQuestionSet interview = new InterviewQuestionSet();
        interview.setMatchResult(result); interview.setAiTask(analysis.getAiTask());
        interview.setPromptTemplateVersion(analysis.getPromptTemplateVersion()); interview.setModelDeployment(analysis.getModelDeployment());
        interview.setProviderName("test"); interview.setModelName("test"); interview.setPromptVersion("INTERVIEW_PREPARATION:v1");
        interview.setQuestionData("{\"technicalQuestions\":[]}"); interview.setInputTokens(1L); interview.setOutputTokens(1L);
        interview.setGenerationDurationMs(1L); interview.setCorrelationId("repository-test");
        interviewRepository.saveAndFlush(interview);

        assertThat(matchRepository.findByJobId(jobId, PageRequest.of(0, 10))).hasSize(1);
        assertThat(matchRepository.findByJobIdAndResumeOwnerUserId(jobId, resumeOwner, PageRequest.of(0, 10))).hasSize(1);
        assertThat(matchRepository.findByResumeDocumentIdAndJobOwnerUserId(
                analysis.getResumeDocument().getId(), jobOwner, PageRequest.of(0, 10))).hasSize(1);
        assertThat(matchRepository.findByJobIdAndResumeAnalysisResultId(jobId, analysis.getId())).isPresent();
        assertThat(explanationRepository.findByMatchResultId(result.getId())).isPresent();
        assertThat(interviewRepository.findByMatchResultId(result.getId())).isPresent();
    }

    private ResumeAnalysisResult analysis(UUID ownerId) {
        ResumeDocument document = new ResumeDocument();
        document.setOwnerUserId(ownerId);
        document.setBucketName("test");
        document.setObjectKey(ownerId + "/resume.txt");
        document.setOriginalFilename("resume.txt");
        document.setContentType("text/plain");
        document.setFileSize(10L);
        document.setChecksumSha256("a".repeat(64));
        document.setExtractedText("Java");
        document.setStatus(ResumeDocumentStatus.ANALYZED);
        document.setExtractionDurationMs(1L);
        document.setUploadTime(LocalDateTime.now());
        documentRepository.saveAndFlush(document);

        AiTask task = new AiTask();
        task.setTaskType("RESUME_ANALYSIS");
        task.setStatus(AiTaskStatus.COMPLETED);
        task.setRequestedBy(ownerId);
        task.setCorrelationId("repository-test");
        taskRepository.saveAndFlush(task);

        PromptTemplateVersion prompt = new PromptTemplateVersion();
        prompt.setTemplateCode("REPOSITORY_TEST");
        prompt.setVersionNumber(1);
        prompt.setSystemPrompt("test");
        prompt.setUserPromptTemplate("test");
        prompt.setActive(true);
        promptRepository.saveAndFlush(prompt);

        ModelDeployment model = new ModelDeployment();
        model.setProviderName("test");
        model.setModelName("test");
        model.setDeploymentName("repository-test");
        model.setCapability(ModelCapability.STRUCTURED_GENERATION);
        model.setEnabled(true);
        model.setDefaultForCapability(false);
        modelRepository.saveAndFlush(model);

        ResumeAnalysisResult analysis = new ResumeAnalysisResult();
        analysis.setResumeDocument(document);
        analysis.setAiTask(task);
        analysis.setPromptTemplateVersion(prompt);
        analysis.setModelDeployment(model);
        analysis.setProviderName("test");
        analysis.setModelName("test");
        analysis.setPromptVersion("REPOSITORY_TEST:v1");
        analysis.setStructuredData("{}");
        analysis.setQualityScore(50);
        analysis.setScoreBreakdown("{}");
        analysis.setAnalysisDurationMs(1L);
        analysis.setCorrelationId("repository-test");
        return analysisRepository.saveAndFlush(analysis);
    }
}
