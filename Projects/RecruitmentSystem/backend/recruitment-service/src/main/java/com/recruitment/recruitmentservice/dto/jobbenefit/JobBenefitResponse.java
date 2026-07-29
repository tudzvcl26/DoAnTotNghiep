package com.recruitment.recruitmentservice.dto.jobbenefit;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class JobBenefitResponse {

    private UUID id;

    private UUID jobId;

    private UUID benefitId;

    private String benefitName;

    private String benefitSlug;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}