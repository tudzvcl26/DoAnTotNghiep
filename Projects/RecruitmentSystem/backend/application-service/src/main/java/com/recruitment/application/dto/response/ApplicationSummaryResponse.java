package com.recruitment.application.dto.response;

import com.recruitment.application.entity.enums.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ApplicationSummaryResponse {

    private UUID id;

    private UUID candidateId;

    private UUID companyId;

    private UUID jobId;

    private CandidateProfileSnapshotResponse candidateProfileSnapshot;

    private JobSnapshotResponse jobSnapshot;

    private ApplicationStatus status;

    private BigDecimal matchingScore;

    private String matchingVersion;

    private LocalDateTime appliedAt;

    private Instant appliedAtInstant;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Instant updatedAtInstant;

}
