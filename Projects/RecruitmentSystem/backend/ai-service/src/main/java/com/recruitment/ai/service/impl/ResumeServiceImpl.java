package com.recruitment.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.dto.response.AnalysisKeywordItemResponse;
import com.recruitment.ai.dto.response.AnalysisSkillItemResponse;
import com.recruitment.ai.dto.response.ResumeAnalysisResponse;
import com.recruitment.ai.dto.response.ResumeDocumentResponse;
import com.recruitment.ai.dto.response.ScoreDimensionResponse;
import com.recruitment.ai.entity.AiTask;
import com.recruitment.ai.entity.AnalysisKeywordItem;
import com.recruitment.ai.entity.AnalysisSkillItem;
import com.recruitment.ai.entity.ModelDeployment;
import com.recruitment.ai.entity.PromptTemplateVersion;
import com.recruitment.ai.entity.ResumeAnalysisResult;
import com.recruitment.ai.entity.ResumeDocument;
import com.recruitment.ai.entity.enums.AiTaskStatus;
import com.recruitment.ai.entity.enums.ModelCapability;
import com.recruitment.ai.entity.enums.ResumeDocumentStatus;
import com.recruitment.ai.entity.enums.SkillCategory;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.provider.ModelRouter;
import com.recruitment.ai.provider.ProviderUsage;
import com.recruitment.ai.provider.ProviderUsageRecorder;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationRequest;
import com.recruitment.ai.provider.llm.StructuredGenerationResult;
import com.recruitment.ai.repository.AiTaskRepository;
import com.recruitment.ai.repository.AnalysisKeywordItemRepository;
import com.recruitment.ai.repository.AnalysisSkillItemRepository;
import com.recruitment.ai.repository.ModelDeploymentRepository;
import com.recruitment.ai.repository.PromptTemplateVersionRepository;
import com.recruitment.ai.repository.ResumeAnalysisResultRepository;
import com.recruitment.ai.repository.ResumeDocumentRepository;
import com.recruitment.ai.security.CurrentUser;
import com.recruitment.ai.security.SecurityUtils;
import com.recruitment.ai.service.ResumeService;
import com.recruitment.ai.service.analysis.ResumeAnalysisJsonValidator;
import com.recruitment.ai.service.document.DocumentExtractorFactory;
import com.recruitment.ai.service.document.ResumeFileValidator;
import com.recruitment.ai.service.document.ValidatedResumeFile;
import com.recruitment.ai.service.scoring.ResumeQualityScore;
import com.recruitment.ai.service.scoring.ResumeQualityScorer;
import com.recruitment.ai.storage.AiStorageService;
import com.recruitment.ai.util.CorrelationIds;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private static final String PROMPT_CODE = "RESUME_FACT_EXTRACTION";
    private static final String TASK_TYPE = "RESUME_ANALYSIS";
    private static final String SUBJECT_TYPE = "RESUME_DOCUMENT";

    private final ResumeDocumentRepository documentRepository;
    private final ResumeAnalysisResultRepository analysisRepository;
    private final AnalysisSkillItemRepository skillRepository;
    private final AnalysisKeywordItemRepository keywordRepository;
    private final PromptTemplateVersionRepository promptRepository;
    private final ModelDeploymentRepository modelRepository;
    private final AiTaskRepository taskRepository;
    private final ResumeFileValidator fileValidator;
    private final DocumentExtractorFactory extractorFactory;
    private final ResumeAnalysisJsonValidator jsonValidator;
    private final ResumeQualityScorer qualityScorer;
    private final AiStorageService storageService;
    private final ModelRouter modelRouter;
    private final ProviderUsageRecorder usageRecorder;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    public ResumeDocumentResponse upload(MultipartFile file) {
        CurrentUser user = authenticatedUser();
        long uploadStarted = System.nanoTime();
        ValidatedResumeFile validated = fileValidator.validate(file);
        long extractionStarted = System.nanoTime();
        String extractedText = extractorFactory.extract(validated.fileType(), validated.bytes());
        long extractionDuration = elapsedMillis(extractionStarted);
        String objectKey = objectKey(user.getUserId(), validated.originalFilename());

        storageService.upload(
                objectKey,
                new ByteArrayInputStream(validated.bytes()),
                validated.size(),
                validated.contentType()
        );
        try {
            ResumeDocument saved = transaction().execute(status -> {
                ResumeDocument document = new ResumeDocument();
                document.setOwnerUserId(user.getUserId());
                document.setBucketName(storageService.bucketName());
                document.setObjectKey(objectKey);
                document.setOriginalFilename(validated.originalFilename());
                document.setContentType(validated.contentType());
                document.setFileSize(validated.size());
                document.setChecksumSha256(validated.checksumSha256());
                document.setExtractedText(extractedText);
                document.setStatus(ResumeDocumentStatus.READY);
                document.setExtractionDurationMs(extractionDuration);
                document.setUploadTime(LocalDateTime.now());
                return documentRepository.saveAndFlush(document);
            });
            log.info("Resume uploaded resumeId={} owner={} size={} uploadDurationMs={} extractionDurationMs={} correlationId={}",
                    saved.getId(), user.getUserId(), validated.size(), elapsedMillis(uploadStarted),
                    extractionDuration, correlationId());
            return toDocumentResponse(saved);
        } catch (RuntimeException exception) {
            try {
                storageService.delete(objectKey);
            } catch (RuntimeException cleanupException) {
                log.error("Resume upload compensation failed objectKey={}", objectKey, cleanupException);
            }
            throw exception;
        }
    }

    @Override
    public PageResponse<ResumeDocumentResponse> getResumes(Pageable pageable) {
        CurrentUser user = authenticatedUser();
        return PageResponse.from(
                user.isAdmin()
                        ? documentRepository.findAllByDeletedAtIsNull(pageable)
                        : documentRepository.findByOwnerUserIdAndDeletedAtIsNull(user.getUserId(), pageable),
                this::toDocumentResponse
        );
    }

    @Override
    public ResumeDocumentResponse getResume(UUID resumeId) {
        return toDocumentResponse(ownedDocument(resumeId));
    }

    @Override
    public ResumeAnalysisResponse analyze(UUID resumeId) {
        CurrentUser user = authenticatedUser();
        ResumeDocument document = ownedDocument(resumeId);
        PromptTemplateVersion prompt = promptRepository.findByTemplateCodeAndActiveTrue(PROMPT_CODE)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_PROMPT_NOT_CONFIGURED));
        ModelDeployment model = modelRepository
                .findByCapabilityAndEnabledTrueAndDefaultForCapabilityTrue(ModelCapability.STRUCTURED_GENERATION)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_MODEL_NOT_CONFIGURED));
        StructuredGenerationProvider provider = modelRouter.structuredGenerationProvider();
        String correlationId = correlationId();
        AiTask task = createTask(document, user, correlationId);
        long started = System.nanoTime();

        try {
            StructuredGenerationResult generated = provider.generate(new StructuredGenerationRequest(
                    model.getModelName(),
                    prompt.getSystemPrompt() + "\nRequired JSON Schema: " + prompt.getOutputSchema(),
                    prompt.getUserPromptTemplate().replace("{{resumeText}}", document.getExtractedText()),
                    prompt.getOutputSchema(),
                    correlationId
            ));
            JsonNode facts = jsonValidator.parseAndValidate(generated.structuredOutput());
            ResumeQualityScore score = qualityScorer.score(facts, document.getExtractedText());
            long duration = elapsedMillis(started);
            ResumeAnalysisResult result = persistSuccessfulAnalysis(
                    document.getId(), task.getId(), prompt.getId(), model.getId(), generated,
                    facts, score, duration, correlationId
            );
            usageRecorder.record(new ProviderUsage(
                    generated.providerName(), generated.model(), TASK_TYPE,
                    generated.inputTokens(), generated.outputTokens(), duration, true, correlationId
            ));
            log.info("Resume analysis completed resumeId={} analysisId={} score={} model={} promptVersion={} durationMs={} correlationId={}",
                    document.getId(), result.getId(), score.total(), generated.model(),
                    promptVersion(prompt), duration, correlationId);
            return toAnalysisResponse(result);
        } catch (RuntimeException exception) {
            long duration = elapsedMillis(started);
            failTask(task.getId(), exception);
            usageRecorder.record(new ProviderUsage(
                    provider.descriptor().providerName(), model.getModelName(), TASK_TYPE,
                    0, 0, duration, false, correlationId
            ));
            log.warn("Resume analysis failed resumeId={} taskId={} durationMs={} correlationId={} cause={}",
                    document.getId(), task.getId(), duration, correlationId, exception.getClass().getSimpleName());
            throw exception;
        }
    }

    @Override
    public ResumeAnalysisResponse getAnalysis(UUID resumeId) {
        ownedDocument(resumeId);
        return analysisRepository.findByResumeDocumentId(resumeId)
                .map(this::toAnalysisResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_ANALYSIS_NOT_FOUND));
    }

    @Override
    public void delete(UUID resumeId) {
        ResumeDocument document = ownedDocument(resumeId);
        transaction().executeWithoutResult(status -> {
            document.setDeletedAt(LocalDateTime.now());
            documentRepository.save(document);
        });
        log.info("Resume deleted resumeId={} owner={} correlationId={}",
                document.getId(), document.getOwnerUserId(), correlationId());
    }

    private AiTask createTask(ResumeDocument document, CurrentUser user, String correlationId) {
        return transaction().execute(status -> {
            AiTask task = new AiTask();
            task.setTaskType(TASK_TYPE);
            task.setStatus(AiTaskStatus.RUNNING);
            task.setRequestedBy(user.getUserId());
            task.setSubjectType(SUBJECT_TYPE);
            task.setSubjectId(document.getId());
            task.setCorrelationId(correlationId);
            task.setInputChecksum(document.getChecksumSha256());
            task.setProgress(10);
            task.setStartedAt(LocalDateTime.now());
            return taskRepository.saveAndFlush(task);
        });
    }

    private ResumeAnalysisResult persistSuccessfulAnalysis(
            UUID documentId,
            UUID taskId,
            UUID promptId,
            UUID modelId,
            StructuredGenerationResult generated,
            JsonNode facts,
            ResumeQualityScore score,
            long duration,
            String correlationId
    ) {
        return transaction().execute(status -> {
            ResumeDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));
            AiTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
            PromptTemplateVersion prompt = promptRepository.getReferenceById(promptId);
            ModelDeployment model = modelRepository.getReferenceById(modelId);
            ResumeAnalysisResult result = analysisRepository.findByResumeDocumentId(documentId)
                    .orElseGet(ResumeAnalysisResult::new);
            if (result.getId() != null) {
                skillRepository.deleteByAnalysisResultId(result.getId());
                keywordRepository.deleteByAnalysisResultId(result.getId());
            }
            result.setResumeDocument(document);
            result.setAiTask(task);
            result.setPromptTemplateVersion(prompt);
            result.setModelDeployment(model);
            result.setProviderName(generated.providerName());
            result.setModelName(generated.model());
            result.setPromptVersion(promptVersion(prompt));
            result.setStructuredData(writeJson(facts));
            result.setQualityScore(score.total());
            result.setScoreBreakdown(writeJson(score.dimensions()));
            result.setInputTokens(generated.inputTokens());
            result.setOutputTokens(generated.outputTokens());
            result.setAnalysisDurationMs(duration);
            result.setCorrelationId(correlationId);
            result = analysisRepository.saveAndFlush(result);
            document.setAnalysisResult(result);
            saveSkills(result, facts);
            saveKeywords(result, facts, document.getExtractedText());

            document.setStatus(ResumeDocumentStatus.ANALYZED);
            documentRepository.save(document);
            task.setStatus(AiTaskStatus.COMPLETED);
            task.setProgress(100);
            task.setProviderName(generated.providerName());
            task.setModelName(generated.model());
            task.setPromptVersion(promptVersion(prompt));
            task.setResultReference(result.getId().toString());
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);
            return result;
        });
    }

    private void failTask(UUID taskId, RuntimeException exception) {
        transaction().executeWithoutResult(status -> taskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(AiTaskStatus.FAILED);
            task.setProgress(100);
            task.setErrorCode(exception instanceof BusinessException business
                    ? business.getErrorCode().getCode() : ErrorCode.INTERNAL_SERVER_ERROR.getCode());
            task.setErrorMessage(safeErrorMessage(exception));
            task.setRetryable(exception instanceof BusinessException business
                    && business.getErrorCode().isRetryable());
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);
        }));
    }

    private void saveSkills(ResumeAnalysisResult result, JsonNode facts) {
        List<AnalysisSkillItem> items = new ArrayList<>();
        addSkills(items, result, facts.path("skills"), SkillCategory.GENERAL);
        addSkills(items, result, facts.path("technicalSkills"), SkillCategory.TECHNICAL);
        addSkills(items, result, facts.path("softSkills"), SkillCategory.SOFT);
        skillRepository.saveAll(items);
    }

    private void addSkills(
            List<AnalysisSkillItem> target,
            ResumeAnalysisResult result,
            JsonNode values,
            SkillCategory category
    ) {
        if (!values.isArray()) {
            return;
        }
        Set<String> seen = new LinkedHashSet<>();
        for (JsonNode value : values) {
            String name = itemText(value, "name");
            if (!name.isBlank() && seen.add(name.toLowerCase(Locale.ROOT)) && target.size() < 300) {
                AnalysisSkillItem item = new AnalysisSkillItem();
                item.setAnalysisResult(result);
                item.setSkillName(limit(name, 255));
                item.setSkillCategory(category);
                item.setOrdinalPosition(target.size());
                target.add(item);
            }
        }
    }

    private void saveKeywords(ResumeAnalysisResult result, JsonNode facts, String text) {
        JsonNode values = facts.path("keywords");
        if (!values.isArray()) {
            return;
        }
        List<AnalysisKeywordItem> items = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (JsonNode value : values) {
            String keyword = itemText(value, "keyword");
            String normalized = keyword.toLowerCase(Locale.ROOT);
            if (!keyword.isBlank() && seen.add(normalized) && items.size() < 200) {
                AnalysisKeywordItem item = new AnalysisKeywordItem();
                item.setAnalysisResult(result);
                item.setKeyword(limit(keyword, 255));
                int suppliedFrequency = value.isObject() ? value.path("frequency").asInt(0) : 0;
                item.setFrequency(Math.max(1, suppliedFrequency > 0
                        ? suppliedFrequency : occurrences(text, keyword)));
                item.setOrdinalPosition(items.size());
                items.add(item);
            }
        }
        keywordRepository.saveAll(items);
    }

    private ResumeDocument ownedDocument(UUID resumeId) {
        CurrentUser user = authenticatedUser();
        return user.isAdmin()
                ? documentRepository.findByIdAndDeletedAtIsNull(resumeId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND))
                : documentRepository.findByIdAndOwnerUserIdAndDeletedAtIsNull(resumeId, user.getUserId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));
    }

    private ResumeDocumentResponse toDocumentResponse(ResumeDocument document) {
        return new ResumeDocumentResponse(
                document.getId(), document.getOwnerUserId(), document.getBucketName(), document.getObjectKey(),
                document.getOriginalFilename(), document.getContentType(), document.getFileSize(),
                document.getChecksumSha256(), document.getStatus(), document.getExtractionDurationMs(),
                document.getUploadTime(), document.getCreatedAt(), document.getUpdatedAt()
        );
    }

    private ResumeAnalysisResponse toAnalysisResponse(ResumeAnalysisResult result) {
        try {
            Map<String, ScoreDimensionResponse> breakdown = objectMapper.readValue(
                    result.getScoreBreakdown(),
                    new TypeReference<LinkedHashMap<String, ScoreDimensionResponse>>() { }
            );
            List<AnalysisSkillItemResponse> skills = skillRepository
                    .findByAnalysisResultIdOrderByOrdinalPositionAsc(result.getId()).stream()
                    .map(item -> new AnalysisSkillItemResponse(item.getSkillName(), item.getSkillCategory()))
                    .toList();
            List<AnalysisKeywordItemResponse> keywords = keywordRepository
                    .findByAnalysisResultIdOrderByOrdinalPositionAsc(result.getId()).stream()
                    .map(item -> new AnalysisKeywordItemResponse(item.getKeyword(), item.getFrequency()))
                    .toList();
            return new ResumeAnalysisResponse(
                    result.getId(), result.getResumeDocument().getId(), result.getAiTask().getId(),
                    result.getPromptTemplateVersion().getId(), result.getModelDeployment().getId(),
                    result.getProviderName(), result.getModelName(), result.getPromptVersion(),
                    objectMapper.readTree(result.getStructuredData()), result.getQualityScore(), breakdown,
                    skills, keywords, result.getInputTokens(), result.getOutputTokens(),
                    result.getAnalysisDurationMs(), result.getCorrelationId(),
                    result.getCreatedAt(), result.getUpdatedAt()
            );
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.RESUME_ANALYSIS_INVALID);
        }
    }

    private CurrentUser authenticatedUser() {
        CurrentUser currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new AccessDeniedException("User is not authenticated.");
        }
        return currentUser;
    }

    private TransactionTemplate transaction() {
        return new TransactionTemplate(transactionManager);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.RESUME_ANALYSIS_INVALID);
        }
    }

    private String promptVersion(PromptTemplateVersion prompt) {
        return prompt.getTemplateCode() + ":v" + prompt.getVersionNumber();
    }

    private String objectKey(UUID userId, String originalFilename) {
        return userId + "/resumes/" + UUID.randomUUID() + "/" + originalFilename;
    }

    private String correlationId() {
        String current = CorrelationIds.current();
        return current == null || current.isBlank() ? UUID.randomUUID().toString() : current;
    }

    private long elapsedMillis(long startedNanos) {
        return Math.max(0, (System.nanoTime() - startedNanos) / 1_000_000);
    }

    private String safeErrorMessage(RuntimeException exception) {
        String message = exception.getMessage();
        return limit(message == null || message.isBlank() ? exception.getClass().getSimpleName() : message, 2_000);
    }

    private String itemText(JsonNode value, String objectField) {
        String text = value.isTextual() ? value.asText() : value.path(objectField).asText("");
        return text == null ? "" : text.trim();
    }

    private String limit(String value, int maximumLength) {
        return value.length() <= maximumLength ? value : value.substring(0, maximumLength);
    }

    private int occurrences(String text, String keyword) {
        if (text == null || keyword.isBlank()) {
            return 0;
        }
        return (int) Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                .matcher(text).results().count();
    }
}
