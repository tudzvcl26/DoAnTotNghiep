package com.recruitment.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.dto.response.*;
import com.recruitment.ai.entity.*;
import com.recruitment.ai.entity.enums.AiTaskStatus;
import com.recruitment.ai.entity.enums.ModelCapability;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.matching.client.JobGateway;
import com.recruitment.ai.matching.model.JobSnapshot;
import com.recruitment.ai.prompt.GenerationContextBuilder;
import com.recruitment.ai.provider.*;
import com.recruitment.ai.provider.llm.*;
import com.recruitment.ai.recommendation.RecommendationJsonValidator;
import com.recruitment.ai.repository.*;
import com.recruitment.ai.security.CurrentUser;
import com.recruitment.ai.security.SecurityUtils;
import com.recruitment.ai.service.MatchingService;
import com.recruitment.ai.service.RecommendationService;
import com.recruitment.ai.util.CorrelationIds;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private static final TypeReference<List<String>> STRINGS = new TypeReference<>() { };
    private final JobRecommendationRepository jobRecommendationRepository;
    private final CandidateRecommendationRepository candidateRecommendationRepository;
    private final ResumeAnalysisResultRepository analysisRepository;
    private final JobMatchResultRepository matchRepository;
    private final PromptTemplateVersionRepository promptRepository;
    private final ModelDeploymentRepository modelRepository;
    private final AiTaskRepository taskRepository;
    private final MatchingService matchingService;
    private final JobGateway jobGateway;
    private final GenerationContextBuilder contextBuilder;
    private final RecommendationJsonValidator validator;
    private final ModelRouter modelRouter;
    private final ProviderUsageRecorder usageRecorder;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    public PageResponse<JobRecommendationResponse> recommendJobs(
            UUID resumeId, int minimumScore, int maximumScore, Pageable pageable) {
        CurrentUser user = currentUser();
        if (!user.isAdmin() && !user.hasRole("CANDIDATE")) throw new AccessDeniedException("Candidate access required.");
        ResumeAnalysisResult analysis = resolveCandidateAnalysis(user, resumeId);
        List<JobSnapshot> jobs = jobGateway.getPublishedJobs(accessToken());
        for (JobSnapshot job : jobs) {
            MatchingResultResponse matched = matchingService.match(job.id(), analysis.getResumeDocument().getId());
            JobMatchResult result = matchRepository.findDetailedById(matched.id())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
            ensureJobRecommendation(result, job);
        }
        PageResponse<JobRecommendationResponse> response = transaction().execute(status -> {
            var page = jobRecommendationRepository.findByCandidateUserIdAndResumeDocumentIdAndOverallScoreBetween(
                    analysis.getResumeDocument().getOwnerUserId(), analysis.getResumeDocument().getId(),
                    bounded(minimumScore), bounded(maximumScore), pageable);
            return PageResponse.from(page, this::jobResponse);
        });
        log.info("Job recommendations completed owner={} resumeId={} recommendationCount={} correlationId={}",
                analysis.getResumeDocument().getOwnerUserId(), analysis.getResumeDocument().getId(),
                response.getTotalElements(), correlationId());
        return response;
    }

    @Override
    public JobRecommendationResponse getJobRecommendation(UUID id) {
        JobRecommendation value = jobRecommendationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_NOT_FOUND));
        CurrentUser user = currentUser();
        if (!user.isAdmin() && !value.getCandidateUserId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.RECOMMENDATION_NOT_FOUND);
        }
        return jobResponse(value);
    }

    @Override
    public PageResponse<CandidateRecommendationResponse> recommendCandidates(
            UUID jobId, int minimumScore, int maximumScore, Pageable pageable) {
        CurrentUser user = currentUser();
        if (!user.isAdmin() && !user.hasRole("EMPLOYER")) throw new AccessDeniedException("Employer access required.");
        JobSnapshot job = ownedPublishedJob(jobId, user);
        for (ResumeAnalysisResult analysis : analysisRepository.findAllByOrderByUpdatedAtDesc()) {
            MatchingResultResponse matched = matchingService.match(jobId, analysis.getResumeDocument().getId());
            JobMatchResult result = matchRepository.findDetailedById(matched.id())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
            ensureCandidateRecommendation(result, job);
        }
        PageResponse<CandidateRecommendationResponse> response = transaction().execute(status -> {
            var page = candidateRecommendationRepository.findByJobIdAndOverallScoreBetween(
                    jobId, bounded(minimumScore), bounded(maximumScore), pageable);
            return PageResponse.from(page, this::candidateResponse);
        });
        log.info("Candidate recommendations completed jobId={} owner={} recommendationCount={} correlationId={}",
                jobId, job.companyOwnerId(), response.getTotalElements(), correlationId());
        return response;
    }

    @Override
    public CandidateRecommendationResponse getCandidateRecommendation(UUID id) {
        CandidateRecommendation value = candidateRecommendationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_NOT_FOUND));
        CurrentUser user = currentUser();
        if (!user.isAdmin() && !value.getJobOwnerUserId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.RECOMMENDATION_NOT_FOUND);
        }
        return candidateResponse(value);
    }

    private ResumeAnalysisResult resolveCandidateAnalysis(CurrentUser user, UUID resumeId) {
        ResumeAnalysisResult analysis = resumeId == null
                ? analysisRepository.findFirstByResumeDocumentOwnerUserIdOrderByUpdatedAtDesc(user.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_RESUME_REQUIRED))
                : analysisRepository.findByResumeDocumentId(resumeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_RESUME_REQUIRED));
        if (!user.isAdmin() && !analysis.getResumeDocument().getOwnerUserId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.RESUME_NOT_FOUND);
        }
        return analysis;
    }

    private JobSnapshot ownedPublishedJob(UUID jobId, CurrentUser user) {
        JobSnapshot job = jobGateway.getJob(jobId, accessToken());
        if (!job.active() || !"PUBLISHED".equalsIgnoreCase(job.status())) {
            throw new BusinessException(ErrorCode.MATCH_JOB_NOT_PUBLISHED);
        }
        if (!user.isAdmin() && !job.companyOwnerId().equals(user.getUserId())) {
            throw new AccessDeniedException("You do not own this job.");
        }
        return job;
    }

    private void ensureJobRecommendation(JobMatchResult match, JobSnapshot job) {
        Optional<JobRecommendation> existing = jobRecommendationRepository.findByMatchResultId(match.getId());
        Generated generated = generate("JOB_RECOMMENDATION", match, job, true);
        transaction().executeWithoutResult(status -> {
            JobRecommendation value = existing.orElseGet(JobRecommendation::new);
            value.setMatchResult(matchRepository.getReferenceById(match.getId()));
            value.setResumeDocumentId(match.getResumeDocumentId()); value.setCandidateUserId(match.getResumeOwnerUserId());
            value.setJobId(match.getJobId()); value.setOverallScore(match.getOverallScore());
            apply(value, generated); jobRecommendationRepository.saveAndFlush(value);
            completeTask(generated.taskId(), generated.result(), generated.prompt(), value.getId());
        });
        record(generated, "JOB_RECOMMENDATION", true);
    }

    private void ensureCandidateRecommendation(JobMatchResult match, JobSnapshot job) {
        Optional<CandidateRecommendation> existing = candidateRecommendationRepository.findByMatchResultId(match.getId());
        Generated generated = generate("CANDIDATE_RECOMMENDATION", match, job, false);
        transaction().executeWithoutResult(status -> {
            CandidateRecommendation value = existing.orElseGet(CandidateRecommendation::new);
            value.setMatchResult(matchRepository.getReferenceById(match.getId())); value.setJobId(match.getJobId());
            value.setJobOwnerUserId(match.getJobOwnerUserId()); value.setResumeDocumentId(match.getResumeDocumentId());
            value.setCandidateUserId(match.getResumeOwnerUserId()); value.setOverallScore(match.getOverallScore());
            apply(value, generated); candidateRecommendationRepository.saveAndFlush(value);
            completeTask(generated.taskId(), generated.result(), generated.prompt(), value.getId());
        });
        record(generated, "CANDIDATE_RECOMMENDATION", true);
    }

    private Generated generate(String code, JobMatchResult match, JobSnapshot job, boolean candidateFacing) {
        PromptTemplateVersion prompt = promptRepository.findByTemplateCodeAndActiveTrue(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_PROMPT_NOT_CONFIGURED));
        ModelDeployment model = modelRepository.findByCapabilityAndEnabledTrueAndDefaultForCapabilityTrue(
                ModelCapability.STRUCTURED_GENERATION)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_MODEL_NOT_CONFIGURED));
        String correlationId = correlationId();
        AiTask task = createTask(code, match.getId(), correlationId);
        StructuredGenerationProvider provider = modelRouter.structuredGenerationProvider();
        long started = System.nanoTime();
        try {
            StructuredGenerationResult result = provider.generate(new StructuredGenerationRequest(
                    model.getModelName(), prompt.getSystemPrompt() + "\nRequired JSON Schema: " + prompt.getOutputSchema(),
                    prompt.getUserPromptTemplate().replace("{{context}}", contextBuilder.build(match, job)),
                    prompt.getOutputSchema(), correlationId));
            JsonNode data = candidateFacing ? validator.validateJob(result.structuredOutput())
                    : validator.validateCandidate(result.structuredOutput());
            return new Generated(task.getId(), prompt, model, result, data, elapsed(started), correlationId);
        } catch (RuntimeException exception) {
            long duration = elapsed(started); failTask(task.getId(), exception);
            usageRecorder.record(new ProviderUsage(provider.descriptor().providerName(), model.getModelName(),
                    code, 0, 0, duration, false, correlationId));
            log.warn("Recommendation generation failed type={} matchId={} durationMs={} correlationId={}",
                    code, match.getId(), duration, correlationId);
            throw exception;
        }
    }

    private AiTask createTask(String type, UUID matchId, String correlationId) {
        return transaction().execute(status -> {
            AiTask task = new AiTask(); task.setTaskType(type); task.setStatus(AiTaskStatus.RUNNING);
            task.setRequestedBy(currentUser().getUserId()); task.setSubjectType("JOB_MATCH_RESULT");
            task.setSubjectId(matchId); task.setCorrelationId(correlationId); task.setProgress(10);
            task.setStartedAt(LocalDateTime.now()); return taskRepository.saveAndFlush(task);
        });
    }

    private void apply(JobRecommendation value, Generated generated) {
        value.setAiTask(taskRepository.getReferenceById(generated.taskId())); value.setPromptTemplateVersion(generated.prompt());
        value.setModelDeployment(generated.model()); value.setProviderName(generated.result().providerName());
        value.setModelName(generated.result().model()); value.setPromptVersion(version(generated.prompt()));
        value.setRecommendationData(write(generated.data())); value.setInputTokens(generated.result().inputTokens());
        value.setOutputTokens(generated.result().outputTokens()); value.setGenerationDurationMs(generated.duration());
        value.setCorrelationId(generated.correlationId());
    }

    private void apply(CandidateRecommendation value, Generated generated) {
        value.setAiTask(taskRepository.getReferenceById(generated.taskId())); value.setPromptTemplateVersion(generated.prompt());
        value.setModelDeployment(generated.model()); value.setProviderName(generated.result().providerName());
        value.setModelName(generated.result().model()); value.setPromptVersion(version(generated.prompt()));
        value.setRecommendationData(write(generated.data())); value.setInputTokens(generated.result().inputTokens());
        value.setOutputTokens(generated.result().outputTokens()); value.setGenerationDurationMs(generated.duration());
        value.setCorrelationId(generated.correlationId());
    }

    private void completeTask(UUID taskId, StructuredGenerationResult result, PromptTemplateVersion prompt, UUID reference) {
        AiTask task = taskRepository.findById(taskId).orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        task.setStatus(AiTaskStatus.COMPLETED); task.setProgress(100); task.setProviderName(result.providerName());
        task.setModelName(result.model()); task.setPromptVersion(version(prompt)); task.setResultReference(reference.toString());
        task.setCompletedAt(LocalDateTime.now()); taskRepository.save(task);
    }

    private void failTask(UUID taskId, RuntimeException exception) {
        transaction().executeWithoutResult(status -> taskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(AiTaskStatus.FAILED); task.setProgress(100);
            task.setErrorCode(exception instanceof BusinessException business ? business.getErrorCode().getCode()
                    : ErrorCode.INTERNAL_SERVER_ERROR.getCode());
            task.setErrorMessage(exception.getMessage()); task.setCompletedAt(LocalDateTime.now()); taskRepository.save(task);
        }));
    }

    private void record(Generated generated, String type, boolean success) {
        usageRecorder.record(new ProviderUsage(generated.result().providerName(), generated.result().model(), type,
                generated.result().inputTokens(), generated.result().outputTokens(), generated.duration(), success,
                generated.correlationId()));
    }

    private JobRecommendationResponse jobResponse(JobRecommendation value) {
        JobMatchResult match = value.getMatchResult();
        return new JobRecommendationResponse(value.getId(), match.getId(), value.getResumeDocumentId(), value.getJobId(),
                value.getOverallScore(), breakdown(match), readList(match.getStrengths()), readList(match.getWeaknesses()),
                readList(match.getMissingSkills()), read(value.getRecommendationData()), value.getProviderName(),
                value.getModelName(), value.getPromptVersion(), value.getInputTokens(), value.getOutputTokens(),
                value.getGenerationDurationMs(), value.getCorrelationId(), value.getCreatedAt(), value.getUpdatedAt());
    }

    private CandidateRecommendationResponse candidateResponse(CandidateRecommendation value) {
        JobMatchResult match = value.getMatchResult();
        return new CandidateRecommendationResponse(value.getId(), match.getId(), value.getJobId(), value.getResumeDocumentId(),
                value.getCandidateUserId(), value.getOverallScore(), breakdown(match), readList(match.getStrengths()),
                readList(match.getWeaknesses()), readList(match.getMissingSkills()), read(value.getRecommendationData()),
                value.getProviderName(), value.getModelName(), value.getPromptVersion(), value.getInputTokens(),
                value.getOutputTokens(), value.getGenerationDurationMs(), value.getCorrelationId(), value.getCreatedAt(),
                value.getUpdatedAt());
    }

    private List<MatchScoreBreakdownResponse> breakdown(JobMatchResult match) {
        return match.getBreakdowns().stream().sorted(Comparator.comparing(MatchScoreBreakdown::getOrdinalPosition))
                .map(item -> new MatchScoreBreakdownResponse(item.getDimensionCode(), item.getMaximumScore(),
                        item.getActualScore(), item.getReason())).toList();
    }

    private CurrentUser currentUser() {
        CurrentUser user = SecurityUtils.getCurrentUser();
        if (user == null || user.getUserId() == null) throw new AccessDeniedException("User is not authenticated.");
        return user;
    }
    private String accessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) throw new AccessDeniedException("Access token is unavailable.");
        return authentication.getCredentials().toString();
    }
    private TransactionTemplate transaction() { return new TransactionTemplate(transactionManager); }
    private int bounded(int score) { return Math.max(0, Math.min(100, score)); }
    private String correlationId() { String value = CorrelationIds.current(); return value == null || value.isBlank() ? UUID.randomUUID().toString() : value; }
    private String version(PromptTemplateVersion prompt) { return prompt.getTemplateCode() + ":v" + prompt.getVersionNumber(); }
    private long elapsed(long started) { return Math.max(0, (System.nanoTime() - started) / 1_000_000); }
    private String write(JsonNode value) { try { return objectMapper.writeValueAsString(value); } catch (Exception e) { throw new IllegalStateException(e); } }
    private JsonNode read(String value) { try { return objectMapper.readTree(value); } catch (Exception e) { throw new IllegalStateException(e); } }
    private List<String> readList(String value) { try { return objectMapper.readValue(value, STRINGS); } catch (Exception e) { throw new IllegalStateException(e); } }

    private record Generated(UUID taskId, PromptTemplateVersion prompt, ModelDeployment model,
                             StructuredGenerationResult result, JsonNode data, long duration, String correlationId) { }
}
