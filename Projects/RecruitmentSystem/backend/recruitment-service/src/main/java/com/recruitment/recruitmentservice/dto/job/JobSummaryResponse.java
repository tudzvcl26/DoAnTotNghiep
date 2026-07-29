package com.recruitment.recruitmentservice.dto.job;

import com.recruitment.recruitmentservice.entity.enums.EmploymentType;
import com.recruitment.recruitmentservice.entity.enums.ExperienceLevel;
import com.recruitment.recruitmentservice.entity.enums.JobStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class JobSummaryResponse {

    private UUID id;

    private String title;

    private String jobCode;

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    private String currency;

    private EmploymentType employmentType;

    private ExperienceLevel experienceLevel;

    private JobStatus status;

    private Boolean remoteAllowed;

    private Integer quantity;

    private UUID companyId;

    private UUID categoryId;

    private String categoryName;

    private String categorySlug;

    private LocalDate applicationDeadline;

    private LocalDateTime publishedAt;

    private Boolean active;

}