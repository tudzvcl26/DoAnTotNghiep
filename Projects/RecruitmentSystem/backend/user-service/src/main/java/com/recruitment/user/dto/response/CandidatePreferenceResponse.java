package com.recruitment.user.dto.response;
import com.recruitment.user.entity.*; import lombok.Builder; import lombok.Getter; import java.math.BigDecimal; import java.util.UUID;
@Getter @Builder public class CandidatePreferenceResponse { private UUID id; private BigDecimal salaryMinimum; private BigDecimal salaryMaximum; private String salaryCurrency; private SalaryPeriod salaryPeriod; private AvailabilityStatus availabilityStatus; private WorkArrangement workArrangement; private Boolean recommendationConsent; private Long version; }
