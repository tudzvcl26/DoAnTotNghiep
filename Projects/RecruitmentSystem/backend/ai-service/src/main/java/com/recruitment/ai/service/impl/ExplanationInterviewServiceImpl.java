package com.recruitment.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.dto.response.InterviewPreparationResponse;
import com.recruitment.ai.assistant.VietnameseGenerationPolicy;
import com.recruitment.ai.dto.response.MatchExplanationResponse;
import com.recruitment.ai.entity.AiMatchExplanation;
import com.recruitment.ai.entity.AiTask;
import com.recruitment.ai.entity.InterviewQuestionSet;
import com.recruitment.ai.entity.JobMatchResult;
import com.recruitment.ai.entity.ModelDeployment;
import com.recruitment.ai.entity.PromptTemplateVersion;
import com.recruitment.ai.entity.enums.AiTaskStatus;
import com.recruitment.ai.entity.enums.ModelCapability;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.explanation.ExplanationJsonValidator;
import com.recruitment.ai.interview.InterviewJsonValidator;
import com.recruitment.ai.matching.client.JobGateway;
import com.recruitment.ai.matching.model.JobSnapshot;
import com.recruitment.ai.prompt.GenerationContextBuilder;
import com.recruitment.ai.provider.ModelRouter;
import com.recruitment.ai.provider.ProviderUsage;
import com.recruitment.ai.provider.ProviderUsageRecorder;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationRequest;
import com.recruitment.ai.provider.llm.StructuredGenerationResult;
import com.recruitment.ai.repository.AiMatchExplanationRepository;
import com.recruitment.ai.repository.AiTaskRepository;
import com.recruitment.ai.repository.InterviewQuestionSetRepository;
import com.recruitment.ai.repository.JobMatchResultRepository;
import com.recruitment.ai.repository.ModelDeploymentRepository;
import com.recruitment.ai.repository.PromptTemplateVersionRepository;
import com.recruitment.ai.security.CurrentUser;
import com.recruitment.ai.security.SecurityUtils;
import com.recruitment.ai.service.ExplanationInterviewService;
import com.recruitment.ai.service.MatchingService;
import com.recruitment.ai.util.CorrelationIds;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExplanationInterviewServiceImpl implements ExplanationInterviewService {
    private static final String EXPLANATION_PROMPT = "MATCH_EXPLANATION";
    private static final String INTERVIEW_PROMPT = "INTERVIEW_PREPARATION";
    private static final String EXPLANATION_FALLBACK = """
            {"overallEvaluation":"Mô hình chưa tạo được phần giải thích tiếng Việt đáng tin cậy. Điểm phù hợp theo quy tắc vẫn được giữ nguyên.","strengths":[],"weaknesses":[],"highScoreReasons":[],"lowScoreReasons":[],"missingTechnologies":[],"careerSuggestions":["Vui lòng thử lại sau để nhận gợi ý chi tiết hơn."],"resumeImprovementChecklist":[],"skillRecommendations":[],"projectRecommendations":[],"certificationSuggestions":[],"keywordImprovements":[],"experienceImprovements":[],"educationImprovements":[],"gapExplanations":[],"learningRoadmap":[],"recommendedTechnologies":[],"recommendedCertifications":[],"portfolioImprovements":[]}
            """;
    private static final String INTERVIEW_FALLBACK = """
            {"technicalQuestions":[{"question":"Nếu đã thực hành một kỹ năng, bạn có thể mô tả cách áp dụng không?","expectedAnswerOutline":"Nêu minh chứng thực tế nếu có; nếu chưa thực hành, nói rõ và trình bày kế hoạch học. Không tự nhận kết quả chưa đạt.","whyInterviewerAsks":"Để xác minh năng lực được trình bày trong CV.","relatedResumeSection":"Kỹ năng chuyên môn","difficulty":"MEDIUM"}],"behavioralQuestions":[{"question":"Nếu từng phối hợp nhóm trong học tập hoặc công việc, bạn đã giải quyết vấn đề thế nào?","expectedAnswerOutline":"Trình bày tình huống thật nếu có; nếu chưa có, nói rõ rồi mô tả cách xử lý tình huống giả định.","whyInterviewerAsks":"Để đánh giá cách phối hợp và xử lý tình huống.","relatedResumeSection":"Kinh nghiệm","difficulty":"MEDIUM"}],"hrQuestions":[{"question":"Mục tiêu nghề nghiệp tiếp theo của bạn là gì?","expectedAnswerOutline":"Nêu mục tiêu học tập hoặc nghề nghiệp và điều cần tìm hiểu về vị trí, không giả định đã có kinh nghiệm.","whyInterviewerAsks":"Để hiểu định hướng và mức độ phù hợp lâu dài.","relatedResumeSection":"Tóm tắt nghề nghiệp","difficulty":"EASY"}],"projectQuestions":[{"question":"Nếu đã có dự án học tập hoặc công việc, bạn có thể mô tả vai trò của mình không?","expectedAnswerOutline":"Nếu có dự án, nêu vai trò và kết quả thật; nếu chưa có, nói rõ chưa có minh chứng và đề xuất dự án để luyện tập.","whyInterviewerAsks":"Để làm rõ minh chứng thực tế trong dự án.","relatedResumeSection":"Dự án","difficulty":"MEDIUM"}]}
            """;

    private final MatchingService matchingService;
    private final JobMatchResultRepository matchRepository;
    private final AiMatchExplanationRepository explanationRepository;
    private final InterviewQuestionSetRepository interviewRepository;
    private final PromptTemplateVersionRepository promptRepository;
    private final ModelDeploymentRepository modelRepository;
    private final AiTaskRepository taskRepository;
    private final JobGateway jobGateway;
    private final GenerationContextBuilder contextBuilder;
    private final ExplanationJsonValidator explanationValidator;
    private final InterviewJsonValidator interviewValidator;
    private final VietnameseGenerationPolicy vietnameseGenerationPolicy;
    private final ModelRouter modelRouter;
    private final ProviderUsageRecorder usageRecorder;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;
    private final com.recruitment.ai.mapper.AiTaskMapper taskMapper;
    private final com.recruitment.ai.explanation.CompactExplanation compactExplanation;
    private final com.recruitment.ai.interview.CompactInterview compactInterview;

    @Override
    public com.recruitment.ai.dto.response.AiTaskResponse queueExplanation(UUID matchId) {
        return queue(matchId, EXPLANATION_PROMPT, ErrorCode.EXPLANATION_PROMPT_NOT_CONFIGURED);
    }

    @Override
    public com.recruitment.ai.dto.response.AiTaskResponse queueInterview(UUID matchId) {
        return queue(matchId, INTERVIEW_PROMPT, ErrorCode.INTERVIEW_PROMPT_NOT_CONFIGURED);
    }

    private com.recruitment.ai.dto.response.AiTaskResponse queue(UUID matchId, String type, ErrorCode promptError) {
        GenerationSetup setup = setup(matchId, type, promptError);
        UUID owner = currentUser().getUserId();
        return transaction().execute(status -> {
            matchRepository.lockById(matchId).orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
            var active = activeTask(matchId, type);
            if (active.isPresent()) {
                if (!owner.equals(active.get().getRequestedBy())) throw new BusinessException(ErrorCode.GENERATION_RUNNING);
                return taskMapper.toResponse(active.get());
            }
            AiTask task = new AiTask();
            task.setTaskType(type); task.setStatus(AiTaskStatus.PENDING);
            task.setRequestedBy(owner); task.setSubjectType("JOB_MATCH_RESULT"); task.setSubjectId(matchId);
            task.setCorrelationId(setup.correlationId());
            var payload = objectMapper.createObjectNode();
            payload.put("context", setup.context()); payload.put("promptId", setup.prompt().getId().toString());
            payload.put("modelId", setup.model().getId().toString());
            task.setInputPayload(write(payload));
            return taskMapper.toResponse(taskRepository.saveAndFlush(task));
        });
    }

    @Override
    public com.recruitment.ai.dto.response.AiTaskResponse latestExplanationTask(UUID matchId) {
        return latestTask(matchId, EXPLANATION_PROMPT);
    }

    @Override
    public com.recruitment.ai.dto.response.AiTaskResponse latestInterviewTask(UUID matchId) {
        return latestTask(matchId, INTERVIEW_PROMPT);
    }

    private com.recruitment.ai.dto.response.AiTaskResponse latestTask(UUID matchId, String type) {
        authorize(matchId);
        return taskRepository.findFirstBySubjectIdAndTaskTypeAndRequestedByOrderByCreatedAtDesc(
                matchId, type, currentUser().getUserId()).map(taskMapper::toResponse).orElse(null);
    }

    /** Called by the database worker; no browser token or security context is retained. */
    public boolean processQueuedExplanation(UUID taskId) {
        AiTask claimed = transaction().execute(status -> {
            AiTask task = taskRepository.lockById(taskId).orElse(null);
            if (task == null || task.getStatus() != AiTaskStatus.PENDING
                    || !java.util.List.of(EXPLANATION_PROMPT, INTERVIEW_PROMPT).contains(task.getTaskType())) return null;
            task.setStatus(AiTaskStatus.RUNNING); task.setStartedAt(LocalDateTime.now()); task.setProgress(10);
            return taskRepository.saveAndFlush(task);
        });
        if (claimed == null) return false;
        try {
            var match = matchRepository.findDetailedById(claimed.getSubjectId()).orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
            if (match.getResumeAnalysisResult().getResumeDocument().getDeletedAt() != null) throw new BusinessException(ErrorCode.RESUME_NOT_FOUND);
            JsonNode payload = read(claimed.getInputPayload());
            PromptTemplateVersion prompt = promptRepository.findById(UUID.fromString(payload.path("promptId").asText())).orElseThrow();
            ModelDeployment model = modelRepository.findById(UUID.fromString(payload.path("modelId").asText())).orElseThrow();
            var setup = new GenerationSetup(claimed.getSubjectId(), payload.path("context").asText(), prompt, model, claimed.getCorrelationId());
            if (INTERVIEW_PROMPT.equals(claimed.getTaskType())) generateInterview(setup, claimed);
            else generateExplanation(setup, claimed);
        } catch (RuntimeException error) { failed(taskId, error); }
        return true;
    }

    public void expireAbandonedTasks() {
        var now = LocalDateTime.now();
        for (AiTask candidate : taskRepository.findByTaskTypeInAndStatusInAndCreatedAtBefore(
                java.util.List.of(EXPLANATION_PROMPT, INTERVIEW_PROMPT, "RESUME_ANALYSIS"),
                java.util.List.of(AiTaskStatus.PENDING, AiTaskStatus.RUNNING), now.minusMinutes(10))) {
            transaction().executeWithoutResult(status -> taskRepository.lockById(candidate.getId()).ifPresent(task -> {
                boolean expired = task.getStatus() == AiTaskStatus.RUNNING && (task.getStartedAt() == null || task.getStartedAt().isBefore(now.minusMinutes(10)))
                        || task.getStatus() == AiTaskStatus.PENDING && task.getCreatedAt().isBefore(now.minusMinutes(30));
                if (expired) {
                    task.setStatus(AiTaskStatus.FAILED); task.setProgress(100); task.setCompletedAt(now);
                    task.setErrorCode(ErrorCode.PROVIDER_TIMEOUT.getCode()); task.setRetryable(true);
                    task.setErrorMessage("Tác vụ đã hết hạn hoặc bị gián đoạn. Vui lòng gửi lại yêu cầu."); task.setInputPayload(null);
                    taskRepository.save(task);
                }
            }));
        }
    }

    /**
     * A RUNNING row belongs to the previous process because this hook executes
     * before the local worker starts polling. Payloads cannot safely resume in
     * the middle of a provider call, so expose an explicit retryable terminal
     * state instead of leaving the UI polling a task that can never finish.
     */
    public void recoverInterruptedTasksAfterRestart() {
        var now = LocalDateTime.now();
        for (AiTask candidate : taskRepository.findByStatus(AiTaskStatus.RUNNING)) {
            transaction().executeWithoutResult(status -> taskRepository.lockById(candidate.getId()).ifPresent(task -> {
                if (task.getStatus() != AiTaskStatus.RUNNING) return;
                task.setStatus(AiTaskStatus.FAILED); task.setProgress(100); task.setCompletedAt(now);
                task.setErrorCode(ErrorCode.PROVIDER_UNAVAILABLE.getCode()); task.setRetryable(true);
                task.setErrorMessage("Tác vụ bị gián đoạn khi dịch vụ khởi động lại. Vui lòng thử lại.");
                task.setInputPayload(null);
                taskRepository.save(task);
            }));
        }
    }

    private java.util.Optional<AiTask> activeTask(UUID matchId, String type) {
        return taskRepository.findFirstBySubjectIdAndTaskTypeAndStatusInOrderByCreatedAtDesc(matchId, type,
                java.util.List.of(AiTaskStatus.PENDING, AiTaskStatus.RUNNING));
    }

    @Override
    public MatchExplanationResponse generateExplanation(UUID matchId) {
        GenerationSetup setup = setup(matchId, EXPLANATION_PROMPT, ErrorCode.EXPLANATION_PROMPT_NOT_CONFIGURED);
        return generateExplanation(setup);
    }

    @Override
    public MatchExplanationResponse getExplanation(UUID matchId) {
        authorize(matchId);
        return transaction().execute(status -> explanationRepository.findByMatchResultId(matchId)
                .map(this::explanationResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPLANATION_NOT_FOUND)));
    }

    @Override
    public InterviewPreparationResponse generateInterview(UUID matchId) {
        GenerationSetup setup = setup(matchId, INTERVIEW_PROMPT, ErrorCode.INTERVIEW_PROMPT_NOT_CONFIGURED);
        return generateInterview(setup);
    }

    @Override
    public InterviewPreparationResponse getInterview(UUID matchId) {
        authorize(matchId);
        return transaction().execute(status -> interviewRepository.findByMatchResultId(matchId)
                .map(this::interviewResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND)));
    }

    private MatchExplanationResponse generateExplanation(GenerationSetup setup) {
        AiTask task = createTask(setup.matchId(), "MATCH_EXPLANATION", setup.correlationId());
        return generateExplanation(setup, task);
    }

    private MatchExplanationResponse generateExplanation(GenerationSetup setup, AiTask task) {
        long started = System.nanoTime();
        try {
            String output = compactExplanation.compose(null, setup.context());
            JsonNode data = explanationValidator.validate(output);
            StructuredGenerationResult generated = new StructuredGenerationResult(
                    "deterministic-grounded", com.recruitment.ai.explanation.CompactExplanation.POLICY_VERSION,
                    output, 0, 0);
            long duration = elapsed(started);
            AiMatchExplanation saved = transaction().execute(status -> {
                AiTask active = taskRepository.lockById(task.getId()).orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
                if (active.getStatus() != AiTaskStatus.RUNNING) throw new BusinessException(ErrorCode.PROVIDER_TIMEOUT);
                AiMatchExplanation entity = explanationRepository.findByMatchResultId(setup.matchId())
                        .orElseGet(AiMatchExplanation::new);
                entity.setMatchResult(matchRepository.getReferenceById(setup.matchId()));
                applyMetadata(entity, task.getId(), setup, generated, data, duration);
                entity = explanationRepository.saveAndFlush(entity);
                completeTask(task.getId(), generated, setup.prompt(), entity.getId());
                return entity;
            });
            record(generated, "MATCH_EXPLANATION", duration, true, setup.correlationId());
            log.info("Match explanation generated matchId={} explanationId={} model={} promptVersion={} durationMs={} inputTokens={} outputTokens={} correlationId={}",
                    setup.matchId(), saved.getId(), generated.model(), version(setup.prompt()), duration,
                    generated.inputTokens(), generated.outputTokens(), setup.correlationId());
            return explanationResponse(saved);
        } catch (RuntimeException exception) {
            failed(task.getId(), exception);
            record(new StructuredGenerationResult("deterministic-grounded",
                            com.recruitment.ai.explanation.CompactExplanation.POLICY_VERSION, "", 0, 0),
                    "MATCH_EXPLANATION", elapsed(started), false, setup.correlationId());
            log.warn("Deterministic explanation failed matchId={} correlationId={}",
                    setup.matchId(), setup.correlationId());
            throw exception;
        }
    }

    private InterviewPreparationResponse generateInterview(GenerationSetup setup) {
        AiTask task = createTask(setup.matchId(), "INTERVIEW_PREPARATION", setup.correlationId());
        return generateInterview(setup, task);
    }

    private InterviewPreparationResponse generateInterview(GenerationSetup setup, AiTask task) {
        StructuredGenerationProvider provider = modelRouter.structuredGenerationProvider();
        long started = System.nanoTime();
        try {
            StructuredGenerationResult generated = generate(provider, setup);
            JsonNode data = interviewValidator.validate(setup.prompt().getVersionNumber() >= 3
                    ? compactInterview.compose(generated.structuredOutput()) : generated.structuredOutput());
            long duration = elapsed(started);
            InterviewQuestionSet saved = transaction().execute(status -> {
                AiTask active = taskRepository.lockById(task.getId()).orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
                if (active.getStatus() != AiTaskStatus.RUNNING) throw new BusinessException(ErrorCode.PROVIDER_TIMEOUT);
                InterviewQuestionSet entity = interviewRepository.findByMatchResultId(setup.matchId())
                        .orElseGet(InterviewQuestionSet::new);
                entity.setMatchResult(matchRepository.getReferenceById(setup.matchId()));
                applyMetadata(entity, task.getId(), setup, generated, data, duration);
                entity = interviewRepository.saveAndFlush(entity);
                completeTask(task.getId(), generated, setup.prompt(), entity.getId());
                return entity;
            });
            record(generated, "INTERVIEW_PREPARATION", duration, true, setup.correlationId());
            log.info("Interview preparation generated matchId={} questionSetId={} model={} promptVersion={} durationMs={} inputTokens={} outputTokens={} correlationId={}",
                    setup.matchId(), saved.getId(), generated.model(), version(setup.prompt()), duration,
                    generated.inputTokens(), generated.outputTokens(), setup.correlationId());
            return interviewResponse(saved);
        } catch (RuntimeException exception) {
            failed(task.getId(), exception); recordFailure(provider, setup, "INTERVIEW_PREPARATION", started);
            throw exception;
        }
    }

    private GenerationSetup setup(UUID matchId, String promptCode, ErrorCode promptError) {
        UUID jobId = authorize(matchId);
        JobSnapshot job = jobGateway.getJob(jobId, accessToken());
        if (!job.active() || !"PUBLISHED".equalsIgnoreCase(job.status())) {
            throw new BusinessException(ErrorCode.MATCH_JOB_NOT_PUBLISHED);
        }
        String context = transaction().execute(status -> contextBuilder.build(
                matchRepository.findById(matchId).orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND)), job));
        PromptTemplateVersion prompt = promptRepository.findByTemplateCodeAndActiveTrue(promptCode)
                .orElseThrow(() -> new BusinessException(promptError));
        ModelDeployment model = modelRepository
                .findByCapabilityAndEnabledTrueAndDefaultForCapabilityTrue(ModelCapability.STRUCTURED_GENERATION)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_MODEL_NOT_CONFIGURED));
        return new GenerationSetup(matchId, context, prompt, model, correlationId());
    }

    private UUID authorize(UUID matchId) {
        matchingService.getById(matchId);
        return transaction().execute(status -> matchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND)).getJobId());
    }

    private StructuredGenerationResult generate(StructuredGenerationProvider provider, GenerationSetup setup) {
        boolean compact = EXPLANATION_PROMPT.equals(setup.prompt().getTemplateCode()) && setup.prompt().getVersionNumber() >= 3;
        boolean compactQuestions = INTERVIEW_PROMPT.equals(setup.prompt().getTemplateCode()) && setup.prompt().getVersionNumber() >= 3;
        StructuredGenerationRequest request = new StructuredGenerationRequest(setup.model().getModelName(),
                vietnameseGenerationPolicy.applyContract(setup.prompt().getSystemPrompt(), setup.prompt().getOutputSchema())
                        + (compactQuestions ? "\n" + com.recruitment.ai.interview.CompactInterview.ANSWER_CONTRACT : ""),
                setup.prompt().getUserPromptTemplate().replace("{{context}}", compact ? compactExplanation.context(setup.context())
                        : compactQuestions ? compactInterview.context(setup.context()) : setup.context()),
                setup.prompt().getOutputSchema(), setup.correlationId(), compact ? 512 : compactQuestions ? 640 : 0);
        String fallback = EXPLANATION_PROMPT.equals(setup.prompt().getTemplateCode())
                ? EXPLANATION_FALLBACK : null;
        return compactQuestions
                ? new StructuredGenerationResult("deterministic-grounded", "grounded-interview-v1",
                    compactInterview.grounded(compactInterview.context(setup.context())), 0, 0)
                : vietnameseGenerationPolicy.generate(provider, request, setup.prompt().getTemplateCode(), fallback);
    }

    private AiTask createTask(UUID matchId, String type, String correlationId) {
        CurrentUser user = currentUser();
        return transaction().execute(status -> {
            matchRepository.lockById(matchId).orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
            if (activeTask(matchId, type).isPresent()) throw new BusinessException(ErrorCode.GENERATION_RUNNING);
            AiTask task = new AiTask(); task.setTaskType(type); task.setStatus(AiTaskStatus.RUNNING);
            task.setRequestedBy(user.getUserId()); task.setSubjectType("JOB_MATCH_RESULT"); task.setSubjectId(matchId);
            task.setCorrelationId(correlationId); task.setProgress(10); task.setStartedAt(LocalDateTime.now());
            return taskRepository.saveAndFlush(task);
        });
    }

    private void applyMetadata(AiMatchExplanation entity, UUID taskId, GenerationSetup setup,
                               StructuredGenerationResult generated, JsonNode data, long duration) {
        entity.setAiTask(taskRepository.getReferenceById(taskId)); entity.setPromptTemplateVersion(setup.prompt());
        entity.setModelDeployment(setup.model()); entity.setProviderName(generated.providerName());
        entity.setModelName(generated.model()); entity.setPromptVersion(version(setup.prompt()));
        entity.setExplanationData(write(data)); entity.setInputTokens(generated.inputTokens());
        entity.setOutputTokens(generated.outputTokens()); entity.setGenerationDurationMs(duration);
        entity.setCorrelationId(setup.correlationId());
    }

    private void applyMetadata(InterviewQuestionSet entity, UUID taskId, GenerationSetup setup,
                               StructuredGenerationResult generated, JsonNode data, long duration) {
        entity.setAiTask(taskRepository.getReferenceById(taskId)); entity.setPromptTemplateVersion(setup.prompt());
        entity.setModelDeployment(setup.model()); entity.setProviderName(generated.providerName());
        entity.setModelName(generated.model()); entity.setPromptVersion(version(setup.prompt()));
        entity.setQuestionData(write(data)); entity.setInputTokens(generated.inputTokens());
        entity.setOutputTokens(generated.outputTokens()); entity.setGenerationDurationMs(duration);
        entity.setCorrelationId(setup.correlationId());
    }

    private void completeTask(UUID taskId, StructuredGenerationResult generated, PromptTemplateVersion prompt, UUID resultId) {
        AiTask task = taskRepository.findById(taskId).orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        task.setStatus(AiTaskStatus.COMPLETED); task.setProgress(100); task.setProviderName(generated.providerName());
        task.setModelName(generated.model()); task.setPromptVersion(version(prompt));
        task.setResultReference(resultId == null ? null : resultId.toString()); task.setCompletedAt(LocalDateTime.now());
        task.setInputPayload(null);
        taskRepository.save(task);
    }

    private void failed(UUID taskId, RuntimeException exception) {
        transaction().executeWithoutResult(status -> taskRepository.findById(taskId).ifPresent(task -> {
            if (task.getStatus() != AiTaskStatus.RUNNING && task.getStatus() != AiTaskStatus.PENDING) return;
            task.setStatus(AiTaskStatus.FAILED); task.setProgress(100);
            task.setErrorCode(exception instanceof BusinessException b ? b.getErrorCode().getCode() : ErrorCode.INTERNAL_SERVER_ERROR.getCode());
            task.setErrorMessage(exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
            task.setRetryable(exception instanceof BusinessException b && b.getErrorCode().isRetryable());
            task.setInputPayload(null); task.setCompletedAt(LocalDateTime.now()); taskRepository.save(task);
        }));
    }

    private MatchExplanationResponse explanationResponse(AiMatchExplanation value) {
        return new MatchExplanationResponse(value.getId(), value.getMatchResult().getId(), value.getAiTask().getId(),
                value.getPromptTemplateVersion().getId(), value.getModelDeployment().getId(), value.getProviderName(),
                value.getModelName(), value.getPromptVersion(), read(value.getExplanationData()), value.getInputTokens(),
                value.getOutputTokens(), value.getGenerationDurationMs(), value.getCorrelationId(), value.getCreatedAt(), value.getUpdatedAt());
    }

    private InterviewPreparationResponse interviewResponse(InterviewQuestionSet value) {
        return new InterviewPreparationResponse(value.getId(), value.getMatchResult().getId(), value.getAiTask().getId(),
                value.getPromptTemplateVersion().getId(), value.getModelDeployment().getId(), value.getProviderName(),
                value.getModelName(), value.getPromptVersion(), read(value.getQuestionData()), value.getInputTokens(),
                value.getOutputTokens(), value.getGenerationDurationMs(), value.getCorrelationId(), value.getCreatedAt(), value.getUpdatedAt());
    }

    private void record(StructuredGenerationResult result, String operation, long duration, boolean success, String correlationId) {
        usageRecorder.record(new ProviderUsage(result.providerName(), result.model(), operation,
                result.inputTokens(), result.outputTokens(), duration, success, correlationId));
    }

    private void recordFailure(StructuredGenerationProvider provider, GenerationSetup setup, String operation, long started) {
        usageRecorder.record(new ProviderUsage(provider.descriptor().providerName(), setup.model().getModelName(),
                operation, 0, 0, elapsed(started), false, setup.correlationId()));
        log.warn("AI generation failed operation={} matchId={} correlationId={}", operation, setup.matchId(), setup.correlationId());
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
    private String version(PromptTemplateVersion prompt) { return prompt.getTemplateCode() + ":v" + prompt.getVersionNumber(); }
    private String correlationId() { String value = CorrelationIds.current(); return value == null || value.isBlank() ? UUID.randomUUID().toString() : value; }
    private long elapsed(long started) { return Math.max(0, (System.nanoTime() - started) / 1_000_000); }
    private String write(Object value) { try { return objectMapper.writeValueAsString(value); } catch (Exception e) { throw new IllegalStateException(e); } }
    private JsonNode read(String value) { try { return objectMapper.readTree(value); } catch (Exception e) { throw new IllegalStateException(e); } }

    private record GenerationSetup(UUID matchId, String context, PromptTemplateVersion prompt,
                                   ModelDeployment model, String correlationId) { }
}
