package com.recruitment.ai.dto.request;

import com.recruitment.ai.assistant.RecruiterAssistantTask;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RecruiterAssistantRequest(
        @NotNull RecruiterAssistantTask task,
        @NotNull UUID jobId,
        UUID resumeId,
        UUID matchId
) { }
