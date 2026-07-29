package com.recruitment.recruitmentservice.dto.jobbenefit;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateJobBenefitRequest {

    @NotNull
    private UUID benefitId;

}