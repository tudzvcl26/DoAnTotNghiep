package com.recruitment.ai.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record MatchingResultResponse(
        UUID id,
        UUID jobId,
        UUID resumeId,
        UUID resumeAnalysisResultId,
        int overallScore,
        List<MatchScoreBreakdownResponse> scoreBreakdown,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations,
        List<String> gapAnalysis,
        String matchedExperience,
        String matchedEducation,
        String ruleVersion,
        String weightsVersion,
        Map<String, Integer> weights,
        long matchingDurationMs,
        String correlationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
