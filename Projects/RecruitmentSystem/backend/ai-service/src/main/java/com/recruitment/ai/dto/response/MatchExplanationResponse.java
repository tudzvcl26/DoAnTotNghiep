package com.recruitment.ai.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.UUID;

public record MatchExplanationResponse(
        UUID id, UUID matchId, UUID aiTaskId, UUID promptTemplateVersionId, UUID modelDeploymentId,
        String providerName, String modelName, String promptVersion, JsonNode explanation,
        long inputTokens, long outputTokens, long generationDurationMs, String correlationId,
        LocalDateTime createdAt, LocalDateTime updatedAt
) { }
