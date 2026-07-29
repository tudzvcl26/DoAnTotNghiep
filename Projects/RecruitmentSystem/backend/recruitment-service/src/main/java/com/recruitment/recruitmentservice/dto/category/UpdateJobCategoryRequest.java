package com.recruitment.recruitmentservice.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Job category update request.
 *
 * The slug is immutable after category creation and is intentionally omitted.
 */
@Getter
@Setter
public class UpdateJobCategoryRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @Size(max = 255)
    private String icon;

    private Integer displayOrder;

    private Boolean active;

    private UUID parentId;

}
