package com.recruitment.recruitmentservice.dto.jobskill;

import com.recruitment.recruitmentservice.entity.enums.SkillLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateJobSkillRequest {

    private SkillLevel skillLevel;

    private Boolean required;

}