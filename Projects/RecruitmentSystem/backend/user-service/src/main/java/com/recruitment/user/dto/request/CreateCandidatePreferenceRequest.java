package com.recruitment.user.dto.request;

import com.recruitment.user.entity.AvailabilityStatus;
import com.recruitment.user.entity.SalaryPeriod;
import com.recruitment.user.entity.WorkArrangement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateCandidatePreferenceRequest {

    @DecimalMin("0.0")
    private BigDecimal salaryMinimum;

    @DecimalMin("0.0")
    private BigDecimal salaryMaximum;

    @Size(min = 3, max = 3)
    private String salaryCurrency;

    private SalaryPeriod salaryPeriod;

    private AvailabilityStatus availabilityStatus;

    private WorkArrangement workArrangement;

    private Boolean recommendationConsent;

}