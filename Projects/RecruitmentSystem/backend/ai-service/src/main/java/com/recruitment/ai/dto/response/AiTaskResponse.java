package com.recruitment.ai.dto.response;

import com.recruitment.ai.entity.enums.AiTaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AiTaskResponse {

    private UUID id;
    private String taskType;
    private AiTaskStatus status;
    private UUID requestedBy;
    private String subjectType;
    private UUID subjectId;
    private String correlationId;
    private Integer progress;
    private Integer retryCount;
    private String providerName;
    private String modelName;
    private String promptVersion;
    private String resultReference;
    private String errorCode;
    private String errorMessage;
    private Boolean retryable;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
