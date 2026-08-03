package com.recruitment.ai.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record JobRecommendationResponse(
        UUID id, UUID matchId, UUID resumeId, UUID jobId, int overallScore,
        List<MatchScoreBreakdownResponse> scoreBreakdown, List<String> strengths,
        List<String> weaknesses, List<String> missingSkills, JsonNode recommendation,
        String providerName, String modelName, String promptVersion, long inputTokens,
        long outputTokens, long generationDurationMs, String correlationId,
        LocalDateTime createdAt, LocalDateTime updatedAt
) { }
