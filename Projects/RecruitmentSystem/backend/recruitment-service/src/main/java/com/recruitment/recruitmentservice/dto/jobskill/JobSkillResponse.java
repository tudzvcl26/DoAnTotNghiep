package com.recruitment.recruitmentservice.dto.jobskill;

import com.recruitment.recruitmentservice.entity.enums.SkillLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class JobSkillResponse {

    private UUID id;

    private UUID jobId;

    private UUID skillId;

    private String skillName;

    private String skillSlug;

    private SkillLevel skillLevel;

    private Boolean required;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}