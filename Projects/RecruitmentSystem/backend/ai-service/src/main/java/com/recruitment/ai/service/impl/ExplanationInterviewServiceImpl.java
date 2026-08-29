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
            {"technicalQuestions":[{"question":"Bạn đã vận dụng kỹ năng chuyên môn nổi bật trong CV như thế nào?","expectedAnswerOutline":"Nêu bối cảnh, hành động, công nghệ đã dùng và kết quả đo lường được.","whyInterviewerAsks":"Để xác minh năng lực được trình bày trong CV.","relatedResumeSection":"Kỹ năng chuyên môn","difficulty":"MEDIUM"}],"behavioralQuestions":[{"question":"Hãy kể về một tình huống bạn phối hợp với đồng đội để giải quyết vấn đề.","expectedAnswerOutline":"Trình bày theo bối cảnh, nhiệm vụ, hành động và kết quả.","whyInterviewerAsks":"Để đánh giá cách phối hợp và xử lý tình huống.","relatedResumeSection":"Kinh nghiệm","difficulty":"MEDIUM"}],"hrQuestions":[{"question":"Mục tiêu nghề nghiệp tiếp theo của bạn là gì?","expectedAnswerOutline":"Liên hệ mục tiêu với kinh nghiệm hiện có và vị trí ứng tuyển.","whyInterviewerAsks":"Để hiểu định hướng và mức độ phù hợp lâu dài.","relatedResumeSection":"Tóm tắt nghề nghiệp","difficulty":"EASY"}],"projectQuestions":[{"question":"Hãy mô tả dự án thể hiện rõ nhất năng lực của bạn.","expectedAnswerOutline":"Nêu vai trò, công nghệ, thách thức, cách giải quyết và kết quả.","whyInterviewerAsks":"Để làm rõ minh chứng thực tế trong dự án.","relatedResumeSection":"Dự án","difficulty":"MEDIUM"}]}
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
        StructuredGenerationProvider provider = modelRouter.structuredGenerationProvider();
        AiTask task = createTask(setup.matchId(), "MATCH_EXPLANATION", setup.correlationId());
        long started = System.nanoTime();
        try {
            StructuredGenerationResult generated = generate(provider, setup);
            JsonNode data = explanationValidator.validate(generated.structuredOutput());
            long duration = elapsed(started);
            AiMatchExplanation saved = transaction().execute(status -> {
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
            failed(task.getId(), exception); recordFailure(provider, setup, "MATCH_EXPLANATION", started);
            throw exception;
        }
    }

    private InterviewPreparationResponse generateInterview(GenerationSetup setup) {
        StructuredGenerationProvider provider = modelRouter.structuredGenerationProvider();
        AiTask task = createTask(setup.matchId(), "INTERVIEW_PREPARATION", setup.correlationId());
        long started = System.nanoTime();
        try {
            StructuredGenerationResult generated = generate(provider, setup);
            JsonNode data = interviewValidator.validate(generated.structuredOutput());
            long duration = elapsed(started);
            InterviewQuestionSet saved = transaction().execute(status -> {
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
        StructuredGenerationRequest request = new StructuredGenerationRequest(setup.model().getModelName(),
                vietnameseGenerationPolicy.applyContract(setup.prompt().getSystemPrompt(), setup.prompt().getOutputSchema()),
                setup.prompt().getUserPromptTemplate().replace("{{context}}", setup.context()),
                setup.prompt().getOutputSchema(), setup.correlationId());
        String fallback = EXPLANATION_PROMPT.equals(setup.prompt().getTemplateCode())
                ? EXPLANATION_FALLBACK : INTERVIEW_FALLBACK;
        return vietnameseGenerationPolicy.generate(provider, request, setup.prompt().getTemplateCode(), fallback);
    }

    private AiTask createTask(UUID matchId, String type, String correlationId) {
        CurrentUser user = currentUser();
        return transaction().execute(status -> {
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
        taskRepository.save(task);
    }

    private void failed(UUID taskId, RuntimeException exception) {
        transaction().executeWithoutResult(status -> taskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(AiTaskStatus.FAILED); task.setProgress(100);
            task.setErrorCode(exception instanceof BusinessException b ? b.getErrorCode().getCode() : ErrorCode.INTERNAL_SERVER_ERROR.getCode());
            task.setErrorMessage(exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
            task.setRetryable(exception instanceof BusinessException b && b.getErrorCode().isRetryable());
            task.setCompletedAt(LocalDateTime.now()); taskRepository.save(task);
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
