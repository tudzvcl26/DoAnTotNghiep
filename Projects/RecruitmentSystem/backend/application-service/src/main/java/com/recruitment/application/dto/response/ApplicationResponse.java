package com.recruitment.application.dto.response;

import com.recruitment.application.entity.enums.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ApplicationResponse {

    private UUID id;

    private UUID candidateId;

    private UUID companyId;

    private UUID jobId;

    private UUID resumeSnapshotId;

    private UUID jobSnapshotId;

    private UUID candidateProfileSnapshotId;

    private ApplicationStatus status;

    private BigDecimal matchingScore;

    private String matchingVersion;

    private String coverLetter;

    private LocalDateTime appliedAt;

    private Instant appliedAtInstant;

    private Boolean active;

    private ResumeSnapshotResponse resumeSnapshot;

    private JobSnapshotResponse jobSnapshot;

    private CandidateProfileSnapshotResponse candidateProfileSnapshot;

    private List<ApplicationStatusHistoryResponse> statusHistory;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Instant updatedAtInstant;

}
