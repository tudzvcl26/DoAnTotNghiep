package com.recruitment.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "candidate_profile_snapshots")
public class CandidateProfileSnapshot extends BaseEntity {

    @Column(name = "application_id", nullable = false, unique = true)
    private UUID applicationId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "profile_id")
    private UUID profileId;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(length = 255)
    private String headline;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "profile_version")
    private Long profileVersion;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;
}
