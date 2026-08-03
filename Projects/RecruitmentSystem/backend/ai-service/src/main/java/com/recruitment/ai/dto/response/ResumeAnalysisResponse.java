package com.recruitment.ai.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ResumeAnalysisResponse(
        UUID id,
        UUID resumeDocumentId,
        UUID aiTaskId,
        UUID promptTemplateVersionId,
        UUID modelDeploymentId,
        String providerName,
        String modelName,
        String promptVersion,
        JsonNode structuredData,
        int qualityScore,
        Map<String, ScoreDimensionResponse> scoreBreakdown,
        List<AnalysisSkillItemResponse> skills,
        List<AnalysisKeywordItemResponse> keywords,
        long inputTokens,
        long outputTokens,
        long analysisDurationMs,
        String correlationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
