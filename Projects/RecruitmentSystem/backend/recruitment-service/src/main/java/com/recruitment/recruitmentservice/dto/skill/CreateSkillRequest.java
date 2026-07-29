package com.recruitment.recruitmentservice.dto.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSkillRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 120)
    private String slug;

    @Size(max = 500)
    private String description;

    @Size(max = 255)
    private String icon;

    private Boolean active = true;

}