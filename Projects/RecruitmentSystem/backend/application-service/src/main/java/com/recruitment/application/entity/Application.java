package com.recruitment.application.entity;

import com.recruitment.application.entity.enums.ApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "applications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_application_candidate_job",
                        columnNames = {"candidate_id", "job_id"}
                )
        }
)
public class Application extends BaseEntity {

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "resume_snapshot_id")
    private UUID resumeSnapshotId;

    @Column(name = "job_snapshot_id")
    private UUID jobSnapshotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "matching_score", precision = 5, scale = 2)
    private BigDecimal matchingScore;

    @Column(name = "matching_version", length = 50)
    private String matchingVersion;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(nullable = false)
    private Boolean active = true;

}
