package com.recruitment.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.config.MatchingProperties;
import com.recruitment.ai.dto.response.MatchScoreBreakdownResponse;
import com.recruitment.ai.dto.response.MatchingResultResponse;
import com.recruitment.ai.entity.JobMatchResult;
import com.recruitment.ai.entity.MatchScoreBreakdown;
import com.recruitment.ai.entity.ResumeAnalysisResult;
import com.recruitment.ai.entity.ResumeDocument;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.matching.client.JobGateway;
import com.recruitment.ai.matching.engine.RuleBasedMatchingEngine;
import com.recruitment.ai.matching.model.JobRequirements;
import com.recruitment.ai.matching.model.JobSnapshot;
import com.recruitment.ai.matching.model.MatchingComputation;
import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.model.ScoreResult;
import com.recruitment.ai.matching.rule.JobRequirementsParser;
import com.recruitment.ai.repository.JobMatchResultRepository;
import com.recruitment.ai.repository.ResumeAnalysisResultRepository;
import com.recruitment.ai.repository.ResumeDocumentRepository;
import com.recruitment.ai.security.CurrentUser;
import com.recruitment.ai.security.SecurityUtils;
import com.recruitment.ai.service.MatchingService;
import com.recruitment.ai.util.CorrelationIds;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MatchingServiceImpl implements MatchingService {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() { };
    private static final TypeReference<Map<String, Integer>> WEIGHT_MAP = new TypeReference<>() { };

    private final ResumeAnalysisResultRepository analysisRepository;
    private final ResumeDocumentRepository documentRepository;
    private final JobMatchResultRepository matchRepository;
    private final JobGateway jobGateway;
    private final JobRequirementsParser requirementsParser;
    private final RuleBasedMatchingEngine matchingEngine;
    private final MatchingProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public MatchingResultResponse match(UUID jobId, UUID resumeId) {
        long started = System.nanoTime();
        CurrentUser user = authenticatedUser();
        ResumeAnalysisResult analysis = analysisRepository.findByResumeDocumentId(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_RESUME_NOT_ANALYZED));
        ResumeDocument document = analysis.getResumeDocument();
        if (!user.isAdmin() && user.hasRole("CANDIDATE")
                && !document.getOwnerUserId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.RESUME_NOT_FOUND);
        }

        JobSnapshot job = jobGateway.getJob(jobId, accessToken());
        if (!job.active() || !"PUBLISHED".equalsIgnoreCase(job.status())) {
            throw new BusinessException(ErrorCode.MATCH_JOB_NOT_PUBLISHED);
        }
        if (!user.isAdmin() && user.hasRole("EMPLOYER")
                && !job.companyOwnerId().equals(user.getUserId())) {
            throw new AccessDeniedException("You do not own this job.");
        }

        JsonNode resumeFacts = readTree(analysis.getStructuredData());
        JobRequirements requirements = requirementsParser.parse(job);
        MatchingComputation computation = matchingEngine.match(new MatchingContext(resumeFacts, job, requirements));
        long durationMs = elapsedMs(started);
        String correlationId = CorrelationIds.current();

        JobMatchResult result = matchRepository.findByJobIdAndResumeAnalysisResultId(jobId, analysis.getId())
                .orElseGet(JobMatchResult::new);
        result.setResumeAnalysisResult(analysis);
        result.setResumeDocumentId(document.getId());
        result.setResumeOwnerUserId(document.getOwnerUserId());
        result.setJobId(job.id());
        result.setJobCompanyId(job.companyId());
        result.setJobOwnerUserId(job.companyOwnerId());
        applyComputation(result, computation, durationMs, correlationId);
        java.util.Set<String> currentDimensions = computation.breakdown().stream()
                .map(ScoreResult::dimension).collect(java.util.stream.Collectors.toSet());
        result.getBreakdowns().removeIf(item -> !currentDimensions.contains(item.getDimensionCode()));
        int position = 0;
        for (ScoreResult score : computation.breakdown()) {
            MatchScoreBreakdown breakdown = result.getBreakdowns().stream()
                    .filter(item -> item.getDimensionCode().equals(score.dimension()))
                    .findFirst().orElseGet(() -> {
                        MatchScoreBreakdown created = new MatchScoreBreakdown();
                        created.setMatchResult(result);
                        result.getBreakdowns().add(created);
                        return created;
                    });
            breakdown.setDimensionCode(score.dimension());
            breakdown.setMaximumScore(score.maximumScore());
            breakdown.setActualScore(score.actualScore());
            breakdown.setReason(score.reason());
            breakdown.setOrdinalPosition(position++);
        }
        JobMatchResult saved = matchRepository.saveAndFlush(result);
        log.info("Rule match completed matchId={} resumeId={} jobId={} score={} durationMs={} ruleVersion={} weightsVersion={} correlationId={}",
                saved.getId(), resumeId, jobId, saved.getOverallScore(), durationMs, saved.getRuleVersion(),
                saved.getWeightsVersion(), correlationId);
        return response(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchingResultResponse getById(UUID id) {
        JobMatchResult result = matchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
        assertCanRead(result);
        return response(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MatchingResultResponse> getByJob(UUID jobId, Pageable pageable) {
        CurrentUser user = authenticatedUser();
        Page<JobMatchResult> page;
        if (user.isAdmin()) {
            page = matchRepository.findByJobId(jobId, pageable);
        } else if (user.hasRole("EMPLOYER")) {
            JobSnapshot job = jobGateway.getJob(jobId, accessToken());
            if (!job.companyOwnerId().equals(user.getUserId())) {
                throw new AccessDeniedException("You do not own this job.");
            }
            page = matchRepository.findByJobId(jobId, pageable);
        } else {
            page = matchRepository.findByJobIdAndResumeOwnerUserId(jobId, user.getUserId(), pageable);
        }
        return PageResponse.from(page, this::response);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MatchingResultResponse> getByResume(UUID resumeId, Pageable pageable) {
        CurrentUser user = authenticatedUser();
        Page<JobMatchResult> page;
        if (user.isAdmin()) {
            page = matchRepository.findByResumeDocumentId(resumeId, pageable);
        } else if (user.hasRole("EMPLOYER")) {
            page = matchRepository.findByResumeDocumentIdAndJobOwnerUserId(resumeId, user.getUserId(), pageable);
        } else {
            ResumeDocument document = documentRepository.findByIdAndOwnerUserIdAndDeletedAtIsNull(resumeId, user.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));
            page = matchRepository.findByResumeDocumentId(document.getId(), pageable);
        }
        return PageResponse.from(page, this::response);
    }

    private void applyComputation(JobMatchResult result, MatchingComputation computation, long duration, String correlationId) {
        result.setOverallScore(computation.overallScore());
        result.setMatchedSkills(write(computation.matchedSkills()));
        result.setMissingSkills(write(computation.missingSkills()));
        result.setMatchedKeywords(write(computation.matchedKeywords()));
        result.setMissingKeywords(write(computation.missingKeywords()));
        result.setStrengths(write(computation.strengths()));
        result.setWeaknesses(write(computation.weaknesses()));
        result.setRecommendations(write(computation.recommendations()));
        result.setGapAnalysis(write(computation.gapAnalysis()));
        result.setMatchedExperience(computation.matchedExperience());
        result.setMatchedEducation(computation.matchedEducation());
        result.setRuleVersion(properties.getRuleVersion());
        result.setWeightsVersion(properties.getWeightsVersion());
        result.setWeightsSnapshot(write(properties.getWeights().asMap()));
        result.setMatchingDurationMs(duration);
        result.setCorrelationId(correlationId);
    }

    private void assertCanRead(JobMatchResult result) {
        CurrentUser user = authenticatedUser();
        if (user.isAdmin()) return;
        if (user.hasRole("CANDIDATE") && result.getResumeOwnerUserId().equals(user.getUserId())) return;
        if (user.hasRole("EMPLOYER") && result.getJobOwnerUserId().equals(user.getUserId())) return;
        throw new BusinessException(ErrorCode.MATCH_NOT_FOUND);
    }

    private MatchingResultResponse response(JobMatchResult result) {
        List<MatchScoreBreakdownResponse> breakdowns = result.getBreakdowns().stream()
                .sorted(java.util.Comparator.comparing(MatchScoreBreakdown::getOrdinalPosition))
                .map(item -> new MatchScoreBreakdownResponse(item.getDimensionCode(), item.getMaximumScore(),
                        item.getActualScore(), item.getReason())).toList();
        return new MatchingResultResponse(result.getId(), result.getJobId(), result.getResumeDocumentId(),
                result.getResumeAnalysisResult().getId(), result.getOverallScore(), breakdowns,
                readList(result.getMatchedSkills()), readList(result.getMissingSkills()),
                readList(result.getMatchedKeywords()), readList(result.getMissingKeywords()),
                readList(result.getStrengths()), readList(result.getWeaknesses()),
                readList(result.getRecommendations()), readList(result.getGapAnalysis()),
                result.getMatchedExperience(), result.getMatchedEducation(), result.getRuleVersion(),
                result.getWeightsVersion(), readWeights(result.getWeightsSnapshot()), result.getMatchingDurationMs(),
                result.getCorrelationId(), result.getCreatedAt(), result.getUpdatedAt());
    }

    private CurrentUser authenticatedUser() {
        CurrentUser user = SecurityUtils.getCurrentUser();
        if (user == null || user.getUserId() == null) throw new AccessDeniedException("User is not authenticated.");
        return user;
    }

    private String accessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            throw new AccessDeniedException("Access token is unavailable.");
        }
        return authentication.getCredentials().toString();
    }

    private JsonNode readTree(String value) {
        try { return objectMapper.readTree(value); }
        catch (JsonProcessingException exception) { throw new BusinessException(ErrorCode.RESUME_ANALYSIS_INVALID); }
    }

    private String write(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Could not serialize matching result.", exception); }
    }

    private List<String> readList(String value) {
        try { return objectMapper.readValue(value, STRING_LIST); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Could not read matching result.", exception); }
    }

    private Map<String, Integer> readWeights(String value) {
        try { return objectMapper.readValue(value, WEIGHT_MAP); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Could not read matching weights.", exception); }
    }

    private long elapsedMs(long started) {
        return Math.max(0L, java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
    }
}
