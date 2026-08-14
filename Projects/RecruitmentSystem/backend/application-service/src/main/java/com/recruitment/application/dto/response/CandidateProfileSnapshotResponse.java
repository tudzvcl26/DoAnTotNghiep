package com.recruitment.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CandidateProfileSnapshotResponse {

    private UUID id;
    private UUID applicationId;
    private UUID candidateId;
    private UUID profileId;
    private String displayName;
    private String headline;
    private String contactEmail;
    private String contactPhone;
    private Long profileVersion;
    private LocalDateTime capturedAt;
}
