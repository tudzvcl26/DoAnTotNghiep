package com.recruitment.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class JobSnapshotResponse {

    private UUID id;

    private UUID applicationId;

    private UUID jobId;

    private String snapshotData;

    private String jobVersion;

    private LocalDateTime createdAt;

}
