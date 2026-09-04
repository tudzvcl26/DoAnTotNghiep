package com.recruitment.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.assistant.VietnameseGenerationPolicy;
import com.recruitment.ai.config.RecommendationProperties;
import com.recruitment.ai.config.MatchingProperties;
import com.recruitment.ai.dto.response.*;
import com.recruitment.ai.entity.*;
import com.recruitment.ai.entity.enums.AiTaskStatus;
import com.recruitment.ai.entity.enums.ModelCapability;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.matching.client.JobGateway;
import com.recruitment.ai.matching.model.JobSnapshot;
import com.recruitment.ai.prompt.GenerationContextBuilder;
import com.recruitment.ai.mapper.AiTaskMapper;
import com.recruitment.ai.messaging.RecommendationRefreshMessage;
import com.recruitment.ai.messaging.RecommendationRefreshPublisher;
import com.recruitment.ai.provider.*;
import com.recruitment.ai.provider.llm.*;
import com.recruitment.ai.recommendation.RecommendationJsonValidator;
import com.recruitment.ai.recommendation.GroundedRecommendationComposer;
import com.recruitment.ai.recommendation.CandidateConsentGateway;
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
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private static final String JOB_RECOMMENDATION_FALLBACK = """
            {"recommendationSummary":"Mô hình chưa tạo được gợi ý việc làm bằng tiếng Việt đáng tin cậy.","gapSummary":"Vui lòng xem các kỹ năng còn thiếu trong kết quả đối chiếu theo quy tắc.","recommendationReason":"Kết quả gợi ý tạm thời dựa trên điểm phù hợp đã được hệ thống tính toán độc lập."}
            """;
    private static final String CANDIDATE_RECOMMENDATION_FALLBACK = """
            {"recommendationSummary":"Mô hình chưa tạo được phần nhận xét ứng viên bằng tiếng Việt đáng tin cậy.","interviewRecommendation":"Có thể dùng kết quả đối chiếu theo quy tắc để chuẩn bị nội dung xác minh trong phỏng vấn.","recommendationReason":"Nhận xét tạm thời không thay đổi điểm phù hợp và không đưa ra quyết định tuyển dụng."}
            """;
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
    private final GroundedRecommendationComposer groundedRecommendationComposer;
    private final VietnameseGenerationPolicy vietnameseGenerationPolicy;
    private final ModelRouter modelRouter;
    private final ProviderUsageRecorder usageRecorder;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;
    private final CandidateConsentGateway consentGateway;
    private final RecommendationRefreshPublisher refreshPublisher;
    private final AiTaskMapper taskMapper;
    private final RecommendationProperties recommendationProperties;
    private final RedisTemplate<String, Object> aiRedisTemplate;
    private final MatchingProperties matchingProperties;

    @Override
    public PageResponse<JobRecommendationResponse> recommendJobs(
            UUID resumeId, int minimumScore, int maximumScore, Pageable pageable) {
        CurrentUser user = currentUser();
        if (!user.isAdmin() && !user.hasRole("CANDIDATE")) throw new AccessDeniedException("Chức năng này chỉ dành cho ứng viên.");
        ResumeAnalysisResult analysis = resolveCandidateAnalysis(user, resumeId);
        requireConsent(analysis.getResumeDocument().getOwnerUserId());
        PageResponse<JobRecommendationResponse> response = transaction().execute(status -> {
            var page = jobRecommendationRepository.findByCandidateUserIdAndResumeDocumentIdAndOverallScoreBetween(
                    analysis.getResumeDocument().getOwnerUserId(), analysis.getResumeDocument().getId(),
                    bounded(minimumScore), bounded(maximumScore), pageable);
            return PageResponse.from(page, this::jobResponse);
        });
        log.info("Persisted job recommendations read owner={} resumeId={} recommendationCount={} correlationId={}",
                analysis.getResumeDocument().getOwnerUserId(), analysis.getResumeDocument().getId(),
                response.getTotalElements(), correlationId());
        return response;
    }

    @Override
    public AiTaskResponse refreshJobs(UUID resumeId) {
        CurrentUser user = currentUser();
        if (!user.hasRole("CANDIDATE")) throw new AccessDeniedException("Chức năng này chỉ dành cho ứng viên.");
        ResumeAnalysisResult analysis = resolveCandidateAnalysis(user, resumeId);
        requireConsent(user.getUserId());
        Optional<AiTask> active = taskRepository
                .findFirstByRequestedByAndTaskTypeAndStatusInOrderByCreatedAtDesc(user.getUserId(),
                        "JOB_RECOMMENDATION_REFRESH", List.of(AiTaskStatus.PENDING, AiTaskStatus.RUNNING));
        if (active.isPresent()) return taskMapper.toResponse(active.get());

        AiTask task = transaction().execute(status -> {
            AiTask value = new AiTask();
            value.setTaskType("JOB_RECOMMENDATION_REFRESH"); value.setStatus(AiTaskStatus.PENDING);
            value.setRequestedBy(user.getUserId()); value.setSubjectType("RESUME_DOCUMENT");
            value.setSubjectId(analysis.getResumeDocument().getId()); value.setCorrelationId(correlationId());
            value.setProgress(0); return taskRepository.saveAndFlush(value);
        });
        try {
            refreshPublisher.publish(new RecommendationRefreshMessage(task.getId(), user.getUserId(),
                    analysis.getResumeDocument().getId(), task.getCorrelationId()));
        } catch (RuntimeException exception) {
            failTask(task.getId(), exception);
            throw new BusinessException(ErrorCode.MATCH_UPSTREAM_UNAVAILABLE);
        }
        return taskMapper.toResponse(task);
    }

    @Override
    public void processJobRefresh(RecommendationRefreshMessage message) {
        startRefreshTask(message.taskId());
        try {
            ResumeAnalysisResult analysis = analysisRepository.findByResumeDocumentId(message.resumeId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_RESUME_REQUIRED));
            if (!analysis.getResumeDocument().getOwnerUserId().equals(message.candidateId())) {
                throw new BusinessException(ErrorCode.RESUME_NOT_FOUND);
            }
            List<JobSnapshot> jobs = jobGateway.getPublishedJobs("").stream()
                    .limit(Math.max(1, recommendationProperties.getCandidatePoolSize())).toList();
            String checksum = recommendationChecksum(analysis, jobs);
            updateTaskChecksum(message.taskId(), checksum, 15);
            String cacheKey = "ai:job-recommendations:" + recommendationProperties.getCacheVersion() + ":"
                    + message.candidateId() + ":" + message.resumeId() + ":" + checksum;
            if (cacheHit(cacheKey) && jobRecommendationRepository.countByCandidateUserIdAndResumeDocumentId(
                    message.candidateId(), message.resumeId()) > 0) {
                completeRefreshTask(message.taskId(), AiTaskStatus.COMPLETED, "CACHE", 100);
                return;
            }

            List<RankedJob> ranked = new ArrayList<>();
            int completed = 0;
            for (JobSnapshot job : jobs) {
                MatchingResultResponse match = matchingService.matchForRecommendation(job, analysis, message.correlationId());
                JobMatchResult entity = matchRepository.findDetailedById(match.id())
                        .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
                ranked.add(new RankedJob(job, entity));
                completed++;
                updateTaskProgress(message.taskId(), 15 + Math.min(50, completed * 50 / Math.max(1, jobs.size())));
            }
            ranked.sort(Comparator.comparingInt((RankedJob item) -> item.match().getOverallScore()).reversed());
            List<RankedJob> top = ranked.stream().limit(Math.max(1, recommendationProperties.getTopK())).toList();
            int fallbacks = 0;
            for (RankedJob item : top) {
                RefreshGenerated generated = generateForRefresh(item.match(), item.job(), message);
                saveRefreshRecommendation(item.match(), generated.generated());
                if (generated.fallback()) fallbacks++;
            }
            removeStaleRecommendations(message.candidateId(), message.resumeId(),
                    top.stream().map(item -> item.job().id()).toList());
            completeRefreshTask(message.taskId(), fallbacks == 0 ? AiTaskStatus.COMPLETED : AiTaskStatus.PARTIAL,
                    "recommendations=" + top.size(), 100);
            cache(cacheKey);
        } catch (RuntimeException exception) {
            failTask(message.taskId(), exception);
            log.warn("Recommendation refresh failed taskId={} candidateId={} cause={}",
                    message.taskId(), message.candidateId(), exception.getClass().getSimpleName());
        }
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
        if (!user.isAdmin() && !user.hasRole("EMPLOYER")) throw new AccessDeniedException("Chức năng này chỉ dành cho nhà tuyển dụng.");
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
            throw new AccessDeniedException("Bạn không có quyền quản lý việc làm này.");
        }
        return job;
    }

    private void requireConsent(UUID candidateId) {
        if (!consentGateway.hasConsent(candidateId, accessToken())) {
            throw new BusinessException(ErrorCode.RECOMMENDATION_CONSENT_REQUIRED);
        }
    }

    private void startRefreshTask(UUID taskId) {
        transaction().executeWithoutResult(status -> taskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(AiTaskStatus.RUNNING); task.setProgress(5); task.setStartedAt(LocalDateTime.now());
            taskRepository.save(task);
        }));
    }

    private void updateTaskChecksum(UUID taskId, String checksum, int progress) {
        transaction().executeWithoutResult(status -> taskRepository.findById(taskId).ifPresent(task -> {
            task.setInputChecksum(checksum); task.setProgress(progress); taskRepository.save(task);
        }));
    }

    private void updateTaskProgress(UUID taskId, int progress) {
        transaction().executeWithoutResult(status -> taskRepository.findById(taskId).ifPresent(task -> {
            task.setProgress(Math.max(task.getProgress(), progress)); taskRepository.save(task);
        }));
    }

    private void completeRefreshTask(UUID taskId, AiTaskStatus status, String reference, int progress) {
        transaction().executeWithoutResult(transactionStatus -> taskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(status); task.setProgress(progress); task.setResultReference(reference);
            task.setCompletedAt(LocalDateTime.now()); taskRepository.save(task);
        }));
    }

    private RefreshGenerated generateForRefresh(
            JobMatchResult match, JobSnapshot job, RecommendationRefreshMessage message) {
        PromptTemplateVersion prompt = promptRepository.findByTemplateCodeAndActiveTrue("JOB_RECOMMENDATION")
                .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_PROMPT_NOT_CONFIGURED));
        ModelDeployment model = modelRepository.findByCapabilityAndEnabledTrueAndDefaultForCapabilityTrue(
                ModelCapability.STRUCTURED_GENERATION)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_MODEL_NOT_CONFIGURED));
        long started = System.nanoTime();
        JsonNode data = groundedRecommendationComposer.job(match);
        StructuredGenerationResult result = new StructuredGenerationResult(
                "deterministic-grounded", GroundedRecommendationComposer.POLICY_VERSION, data.toString(), 0, 0);
        Generated generated = new Generated(message.taskId(), prompt, model, result, data,
                elapsed(started), message.correlationId());
        record(generated, "JOB_RECOMMENDATION_REFRESH", true);
        return new RefreshGenerated(generated, false);
    }

    private void saveRefreshRecommendation(JobMatchResult match, Generated generated) {
        transaction().executeWithoutResult(status -> {
            JobRecommendation value = jobRecommendationRepository.findByMatchResultId(match.getId())
                    .orElseGet(JobRecommendation::new);
            value.setMatchResult(matchRepository.getReferenceById(match.getId()));
            value.setResumeDocumentId(match.getResumeDocumentId());
            value.setCandidateUserId(match.getResumeOwnerUserId()); value.setJobId(match.getJobId());
            value.setOverallScore(match.getOverallScore()); apply(value, generated);
            jobRecommendationRepository.saveAndFlush(value);
        });
    }

    private void removeStaleRecommendations(UUID candidateId, UUID resumeId, List<UUID> retainedJobIds) {
        transaction().executeWithoutResult(status -> {
            if (retainedJobIds.isEmpty()) {
                jobRecommendationRepository.deleteByCandidateUserIdAndResumeDocumentId(candidateId, resumeId);
            } else {
                jobRecommendationRepository.deleteByCandidateUserIdAndResumeDocumentIdAndJobIdNotIn(
                        candidateId, resumeId, retainedJobIds);
            }
        });
    }

    private String recommendationChecksum(ResumeAnalysisResult analysis, List<JobSnapshot> jobs) {
        StringBuilder value = new StringBuilder(recommendationProperties.getCacheVersion())
                .append('|').append(GroundedRecommendationComposer.POLICY_VERSION)
                .append('|').append(analysis.getId()).append('|').append(analysis.getUpdatedAt())
                .append('|').append(matchingProperties.getRuleVersion())
                .append('|').append(matchingProperties.getWeightsVersion());
        promptRepository.findByTemplateCodeAndActiveTrue("JOB_RECOMMENDATION").ifPresent(prompt -> value
                .append('|').append(prompt.getTemplateCode()).append(':').append(prompt.getVersionNumber()));
        modelRepository.findByCapabilityAndEnabledTrueAndDefaultForCapabilityTrue(ModelCapability.STRUCTURED_GENERATION)
                .ifPresent(model -> value.append('|').append(model.getProviderName()).append(':').append(model.getModelName()));
        jobs.stream().sorted(Comparator.comparing(JobSnapshot::id)).forEach(job -> value.append('|')
                .append(job.id()).append(':').append(job.title()).append(':').append(job.description())
                .append(':').append(job.requirements()).append(':').append(job.responsibilities())
                .append(':').append(job.experienceLevel()).append(':').append(job.companyId())
                .append(':').append(job.status()).append(':').append(job.active()));
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not calculate recommendation checksum.", exception);
        }
    }

    private boolean cacheHit(String key) {
        try { return Boolean.TRUE.equals(aiRedisTemplate.hasKey(key)); }
        catch (RuntimeException exception) { log.warn("Recommendation cache read unavailable"); return false; }
    }

    private void cache(String key) {
        try { aiRedisTemplate.opsForValue().set(key, true, Duration.ofHours(recommendationProperties.getCacheTtlHours())); }
        catch (RuntimeException exception) { log.warn("Recommendation cache write unavailable"); }
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
        long started = System.nanoTime();
        JsonNode data = candidateFacing ? groundedRecommendationComposer.job(match)
                : groundedRecommendationComposer.candidate(match);
        StructuredGenerationResult result = new StructuredGenerationResult(
                "deterministic-grounded", GroundedRecommendationComposer.POLICY_VERSION, data.toString(), 0, 0);
        return new Generated(task.getId(), prompt, model, result, data, elapsed(started), correlationId);
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
        if (user == null || user.getUserId() == null) throw new AccessDeniedException("Bạn chưa đăng nhập.");
        return user;
    }
    private String accessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) throw new AccessDeniedException("Không thể xác thực phiên làm việc.");
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
    private record RefreshGenerated(Generated generated, boolean fallback) { }
    private record RankedJob(JobSnapshot job, JobMatchResult match) { }
}
