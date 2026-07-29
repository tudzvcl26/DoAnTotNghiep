package com.recruitment.recruitmentservice.dto.job;

import com.recruitment.recruitmentservice.entity.enums.EmploymentType;
import com.recruitment.recruitmentservice.entity.enums.ExperienceLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateJobRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 50)
    private String jobCode;

    private String description;

    private String requirements;

    private String responsibilities;

    @DecimalMin(value = "0.0")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.0")
    private BigDecimal salaryMax;

    @Size(max = 10)
    private String currency = "VND";

    @NotNull
    private EmploymentType employmentType;

    @NotNull
    private ExperienceLevel experienceLevel;

    @Min(1)
    private Integer quantity = 1;

    private LocalDate applicationDeadline;

    private Boolean remoteAllowed = false;

    private Boolean active = true;

    @NotNull
    private UUID companyId;

    @NotNull
    private UUID categoryId;

}