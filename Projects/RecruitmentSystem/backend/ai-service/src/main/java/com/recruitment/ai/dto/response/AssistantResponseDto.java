package com.recruitment.ai.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

public record AssistantResponseDto(
        UUID sessionId, UUID responseId, String assistantType, String taskType,
        UUID jobId, UUID resumeId, UUID matchId, JsonNode response,
        String providerName, String modelName, String promptVersion,
        long inputTokens, long outputTokens, long generationDurationMs,
        String correlationId, LocalDateTime createdAt
) { }
