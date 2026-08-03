package com.recruitment.application.event;

import com.recruitment.application.entity.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusChangedEvent {

    private UUID applicationId;

    private UUID candidateId;

    private UUID companyId;

    private UUID jobId;

    private ApplicationStatus fromStatus;

    private ApplicationStatus toStatus;

    private UUID changedBy;

    private LocalDateTime changedAt;

}
