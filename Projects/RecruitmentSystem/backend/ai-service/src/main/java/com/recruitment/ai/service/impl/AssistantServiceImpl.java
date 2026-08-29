package com.recruitment.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recruitment.ai.assistant.AssistantJsonValidator;
import com.recruitment.ai.assistant.VietnameseGenerationPolicy;
import com.recruitment.ai.assistant.RecruiterAssistantTask;
import com.recruitment.ai.dto.request.CandidateAssistantRequest;
import com.recruitment.ai.dto.request.RecruiterAssistantRequest;
import com.recruitment.ai.dto.response.AssistantResponseDto;
import com.recruitment.ai.dto.response.MatchingResultResponse;
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
import com.recruitment.ai.repository.*;
import com.recruitment.ai.security.CurrentUser;
import com.recruitment.ai.security.SecurityUtils;
import com.recruitment.ai.service.AssistantService;
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
public class AssistantServiceImpl implements AssistantService {
    private static final String VIETNAMESE_FALLBACK = """
            {"summary":"Mô hình chưa tạo được nội dung tư vấn tiếng Việt đáng tin cậy.","recommendations":["Vui lòng thử lại sau hoặc bổ sung thông tin CV cụ thể hơn."],"risks":["Kết quả hiện tại chưa đáp ứng yêu cầu ngôn ngữ."],"nextSteps":["Kiểm tra dữ liệu CV rồi gửi lại yêu cầu."]}
            """;
    private final AssistantSessionRepository sessionRepository;
    private final AssistantResponseRepository responseRepository;
    private final ResumeAnalysisResultRepository analysisRepository;
    private final JobMatchResultRepository matchRepository;
    private final PromptTemplateVersionRepository promptRepository;
    private final ModelDeploymentRepository modelRepository;
    private final AiTaskRepository taskRepository;
    private final MatchingService matchingService;
    private final JobGateway jobGateway;
    private final GenerationContextBuilder contextBuilder;
    private final AssistantJsonValidator validator;
    private final VietnameseGenerationPolicy vietnameseGenerationPolicy;
    private final ModelRouter modelRouter;
    private final ProviderUsageRecorder usageRecorder;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    public AssistantResponseDto assistCandidate(CandidateAssistantRequest request) {
        CurrentUser user = currentUser();
        if (!user.isAdmin() && !user.hasRole("CANDIDATE")) throw new AccessDeniedException("Chức năng này chỉ dành cho ứng viên.");
        ResumeAnalysisResult analysis = analysisRepository.findByResumeDocumentId(request.resumeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_ANALYSIS_NOT_FOUND));
        if (!user.isAdmin() && !analysis.getResumeDocument().getOwnerUserId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.RESUME_NOT_FOUND);
        }
        JobMatchResult match = optionalMatch(request.matchId(), request.resumeId());
        JobSnapshot job = match == null ? null : jobGateway.getJob(match.getJobId(), accessToken());
        String context = assistantContext(request.task().name(), analysis, match, job);
        return generate("CANDIDATE", request.task().name(), "CANDIDATE_ASSISTANT",
                job == null ? null : job.id(), request.resumeId(), match, context);
    }

    @Override
    public AssistantResponseDto assistRecruiter(RecruiterAssistantRequest request) {
        CurrentUser user = currentUser();
        if (!user.isAdmin() && !user.hasRole("EMPLOYER")) throw new AccessDeniedException("Chức năng này chỉ dành cho nhà tuyển dụng.");
        JobSnapshot job = jobGateway.getJob(request.jobId(), accessToken());
        if (!job.active() || !"PUBLISHED".equalsIgnoreCase(job.status())) {
            throw new BusinessException(ErrorCode.MATCH_JOB_NOT_PUBLISHED);
        }
        if (!user.isAdmin() && !job.companyOwnerId().equals(user.getUserId())) {
            throw new AccessDeniedException("Bạn không có quyền quản lý việc làm này.");
        }

        JobMatchResult match = null;
        ResumeAnalysisResult analysis = null;
        if (request.matchId() != null) {
            matchingService.getById(request.matchId());
            match = matchRepository.findDetailedById(request.matchId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
            if (!match.getJobId().equals(request.jobId())) throw new BusinessException(ErrorCode.ASSISTANT_CONTEXT_INVALID);
            analysis = match.getResumeAnalysisResult();
        } else if (request.resumeId() != null) {
            MatchingResultResponse calculated = matchingService.match(request.jobId(), request.resumeId());
            match = matchRepository.findDetailedById(calculated.id())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
            analysis = match.getResumeAnalysisResult();
        } else if (request.task() != RecruiterAssistantTask.SUMMARIZE_JOB) {
            throw new BusinessException(ErrorCode.ASSISTANT_CONTEXT_INVALID);
        }
        String context = assistantContext(request.task().name(), analysis, match, job);
        return generate("RECRUITER", request.task().name(), "RECRUITER_ASSISTANT",
                job.id(), analysis == null ? null : analysis.getResumeDocument().getId(), match, context);
    }

    private JobMatchResult optionalMatch(UUID matchId, UUID resumeId) {
        if (matchId == null) return null;
        matchingService.getById(matchId);
        JobMatchResult match = matchRepository.findDetailedById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
        if (!match.getResumeDocumentId().equals(resumeId)) throw new BusinessException(ErrorCode.ASSISTANT_CONTEXT_INVALID);
        return match;
    }

    private String assistantContext(String task, ResumeAnalysisResult analysis, JobMatchResult match, JobSnapshot job) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("requestedTask", task);
            if (analysis != null) root.set("resumeFacts", objectMapper.readTree(analysis.getStructuredData()));
            if (match != null && job != null) {
                root.set("deterministicMatchContext", objectMapper.readTree(contextBuilder.build(match, job)));
            } else if (job != null) {
                ObjectNode jobNode = root.putObject("publishedJob");
                jobNode.put("id", job.id().toString()); jobNode.put("title", job.title());
                jobNode.put("description", job.description()); jobNode.put("requirements", job.requirements());
                jobNode.put("responsibilities", job.responsibilities()); jobNode.put("experienceLevel", job.experienceLevel());
                jobNode.put("status", job.status());
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not build assistant context.", exception);
        }
    }

    private AssistantResponseDto generate(String assistantType, String taskType, String promptCode,
                                          UUID jobId, UUID resumeId, JobMatchResult match, String context) {
        PromptTemplateVersion prompt = promptRepository.findByTemplateCodeAndActiveTrue(promptCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSISTANT_PROMPT_NOT_CONFIGURED));
        ModelDeployment model = modelRepository.findByCapabilityAndEnabledTrueAndDefaultForCapabilityTrue(
                ModelCapability.STRUCTURED_GENERATION)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_MODEL_NOT_CONFIGURED));
        String correlationId = correlationId();
        SessionTask setup = createSessionAndTask(assistantType, taskType, jobId, resumeId, match, correlationId);
        StructuredGenerationProvider provider = modelRouter.structuredGenerationProvider();
        long started = System.nanoTime();
        try {
            StructuredGenerationRequest generationRequest = new StructuredGenerationRequest(
                    model.getModelName(), vietnameseGenerationPolicy.applyContract(prompt.getSystemPrompt(), prompt.getOutputSchema()),
                    prompt.getUserPromptTemplate().replace("{{task}}", taskType).replace("{{context}}", context),
                    prompt.getOutputSchema(), correlationId);
            StructuredGenerationResult generated = vietnameseGenerationPolicy.generate(
                    provider, generationRequest, promptCode, VIETNAMESE_FALLBACK);
            JsonNode data = validator.validate(generated.structuredOutput());
            long duration = elapsed(started);
            AssistantResponse saved = transaction().execute(status -> {
                AssistantResponse response = new AssistantResponse();
                response.setSession(sessionRepository.getReferenceById(setup.sessionId()));
                response.setAiTask(taskRepository.getReferenceById(setup.taskId())); response.setPromptTemplateVersion(prompt);
                response.setModelDeployment(model); response.setProviderName(generated.providerName());
                response.setModelName(generated.model()); response.setPromptVersion(version(prompt));
                response.setResponseData(write(data)); response.setInputTokens(generated.inputTokens());
                response.setOutputTokens(generated.outputTokens()); response.setGenerationDurationMs(duration);
                response.setCorrelationId(correlationId); response = responseRepository.saveAndFlush(response);
                completeTask(setup.taskId(), generated, prompt, response.getId()); return response;
            });
            usageRecorder.record(new ProviderUsage(generated.providerName(), generated.model(), promptCode,
                    generated.inputTokens(), generated.outputTokens(), duration, true, correlationId));
            log.info("Assistant completed assistantType={} taskType={} sessionId={} model={} promptVersion={} durationMs={} tokens={} correlationId={}",
                    assistantType, taskType, setup.sessionId(), generated.model(), version(prompt), duration,
                    generated.inputTokens() + generated.outputTokens(), correlationId);
            return response(saved, setup.sessionId(), assistantType, taskType, jobId, resumeId,
                    match == null ? null : match.getId());
        } catch (RuntimeException exception) {
            long duration = elapsed(started); failTask(setup.taskId(), exception);
            usageRecorder.record(new ProviderUsage(provider.descriptor().providerName(), model.getModelName(),
                    promptCode, 0, 0, duration, false, correlationId));
            log.warn("Assistant failed assistantType={} taskType={} sessionId={} durationMs={} correlationId={}",
                    assistantType, taskType, setup.sessionId(), duration, correlationId);
            throw exception;
        }
    }

    private SessionTask createSessionAndTask(String type, String taskType, UUID jobId, UUID resumeId,
                                             JobMatchResult match, String correlationId) {
        return transaction().execute(status -> {
            CurrentUser user = currentUser();
            AssistantSession session = new AssistantSession(); session.setRequestedBy(user.getUserId());
            session.setAssistantType(type); session.setTaskType(taskType); session.setJobId(jobId);
            session.setResumeDocumentId(resumeId); session.setMatchResult(match); session.setCorrelationId(correlationId);
            session = sessionRepository.saveAndFlush(session);
            AiTask task = new AiTask(); task.setTaskType(type + "_ASSISTANT"); task.setStatus(AiTaskStatus.RUNNING);
            task.setRequestedBy(user.getUserId()); task.setSubjectType("ASSISTANT_SESSION"); task.setSubjectId(session.getId());
            task.setCorrelationId(correlationId); task.setProgress(10); task.setStartedAt(LocalDateTime.now());
            task = taskRepository.saveAndFlush(task); return new SessionTask(session.getId(), task.getId());
        });
    }

    private void completeTask(UUID id, StructuredGenerationResult generated, PromptTemplateVersion prompt, UUID resultId) {
        AiTask task = taskRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        task.setStatus(AiTaskStatus.COMPLETED); task.setProgress(100); task.setProviderName(generated.providerName());
        task.setModelName(generated.model()); task.setPromptVersion(version(prompt)); task.setResultReference(resultId.toString());
        task.setCompletedAt(LocalDateTime.now()); taskRepository.save(task);
    }

    private void failTask(UUID id, RuntimeException exception) {
        transaction().executeWithoutResult(status -> taskRepository.findById(id).ifPresent(task -> {
            task.setStatus(AiTaskStatus.FAILED); task.setProgress(100);
            task.setErrorCode(exception instanceof BusinessException business ? business.getErrorCode().getCode()
                    : ErrorCode.INTERNAL_SERVER_ERROR.getCode());
            task.setErrorMessage(exception.getMessage()); task.setCompletedAt(LocalDateTime.now()); taskRepository.save(task);
        }));
    }

    private AssistantResponseDto response(AssistantResponse value, UUID sessionId, String assistantType,
                                          String taskType, UUID jobId, UUID resumeId, UUID matchId) {
        return new AssistantResponseDto(sessionId, value.getId(), assistantType, taskType,
                jobId, resumeId, matchId,
                read(value.getResponseData()), value.getProviderName(), value.getModelName(), value.getPromptVersion(),
                value.getInputTokens(), value.getOutputTokens(), value.getGenerationDurationMs(), value.getCorrelationId(), value.getCreatedAt());
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
    private String correlationId() { String value = CorrelationIds.current(); return value == null || value.isBlank() ? UUID.randomUUID().toString() : value; }
    private String version(PromptTemplateVersion prompt) { return prompt.getTemplateCode() + ":v" + prompt.getVersionNumber(); }
    private long elapsed(long started) { return Math.max(0, (System.nanoTime() - started) / 1_000_000); }
    private String write(JsonNode value) { try { return objectMapper.writeValueAsString(value); } catch (Exception e) { throw new IllegalStateException(e); } }
    private JsonNode read(String value) { try { return objectMapper.readTree(value); } catch (Exception e) { throw new IllegalStateException(e); } }
    private record SessionTask(UUID sessionId, UUID taskId) { }
}
