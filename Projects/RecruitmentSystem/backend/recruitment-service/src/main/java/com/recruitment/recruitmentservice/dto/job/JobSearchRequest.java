package com.recruitment.recruitmentservice.dto.job;

import com.recruitment.recruitmentservice.entity.enums.EmploymentType;
import com.recruitment.recruitmentservice.entity.enums.ExperienceLevel;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class JobSearchRequest {

    @Size(max = 120)
    private String keyword;

    private UUID categoryId;

    private UUID companyId;

    private UUID skillId;

    private EmploymentType employmentType;

    private ExperienceLevel experienceLevel;

    private Boolean remoteAllowed;

    @Size(max = 100)
    private String location;

    @PositiveOrZero
    private BigDecimal minSalary;

    @PositiveOrZero
    private BigDecimal maxSalary;

    @AssertTrue(message = "Minimum salary must not exceed maximum salary.")
    public boolean isSalaryRangeValid() {
        return minSalary == null || maxSalary == null || minSalary.compareTo(maxSalary) <= 0;
    }
}
