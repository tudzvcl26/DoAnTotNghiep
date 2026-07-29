package com.recruitment.recruitmentservice.dto.category;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class JobCategoryResponse {

    private UUID id;

    private String name;

    private String slug;

    private String description;

    private String icon;

    private Integer displayOrder;

    private Boolean active;

    private UUID parentId;

    private String parentName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}