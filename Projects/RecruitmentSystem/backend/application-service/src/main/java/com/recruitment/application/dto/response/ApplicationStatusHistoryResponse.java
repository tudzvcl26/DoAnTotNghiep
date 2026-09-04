package com.recruitment.application.dto.response;

import com.recruitment.application.entity.enums.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ApplicationStatusHistoryResponse {

    private UUID id;

    private UUID applicationId;

    private ApplicationStatus fromStatus;

    private ApplicationStatus toStatus;

    private String reasonCode;

    private String reasonDetail;

    private UUID changedBy;

    private LocalDateTime changedAt;

    private Instant changedAtInstant;

    private LocalDateTime createdAt;

}
