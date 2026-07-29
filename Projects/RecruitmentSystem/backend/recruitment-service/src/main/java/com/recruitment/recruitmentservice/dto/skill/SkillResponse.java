package com.recruitment.recruitmentservice.dto.skill;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SkillResponse {

    private UUID id;

    private String name;

    private String slug;

    private String description;

    private String icon;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}