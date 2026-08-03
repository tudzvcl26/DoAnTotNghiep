package com.recruitment.application.event;

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
public class ApplicationCreatedEvent {

    private UUID applicationId;

    private UUID candidateId;

    private UUID companyId;

    private UUID jobId;

    private LocalDateTime appliedAt;

}
