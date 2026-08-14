package com.recruitment.ai.messaging;

import java.util.UUID;

public record RecommendationRefreshMessage(UUID taskId, UUID candidateId, UUID resumeId, String correlationId) { }
