package com.recruitment.ai.dto.request;

import com.recruitment.ai.assistant.CandidateAssistantTask;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CandidateAssistantRequest(
        @NotNull CandidateAssistantTask task,
        @NotNull UUID resumeId,
        UUID matchId
) { }
