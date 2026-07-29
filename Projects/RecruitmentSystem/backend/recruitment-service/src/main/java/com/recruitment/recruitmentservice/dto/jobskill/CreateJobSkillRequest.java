package com.recruitment.recruitmentservice.dto.jobskill;

import com.recruitment.recruitmentservice.entity.enums.SkillLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateJobSkillRequest {

    @NotNull
    private UUID jobId;

    @NotNull
    private UUID skillId;

    @NotNull
    private SkillLevel skillLevel = SkillLevel.BEGINNER;

    private Boolean required = true;

}