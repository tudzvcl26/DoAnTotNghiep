package com.recruitment.application.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobClientDto {

    private UUID id;

    private String title;

    private String jobCode;

    private String status;

    private UUID companyId;

    private LocalDate applicationDeadline;

    private String description;

    private String requirements;

    private String responsibilities;

    private String employmentType;

    private String experienceLevel;

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    private String currency;

    private String rawJsonData;

}
