package com.recruitment.recruitmentservice.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateJobCategoryRequest {

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

    private Integer displayOrder = 0;

    private Boolean active = true;

    private UUID parentId;

}