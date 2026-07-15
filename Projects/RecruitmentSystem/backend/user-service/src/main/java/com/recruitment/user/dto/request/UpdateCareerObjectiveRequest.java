package com.recruitment.user.dto.request;

import com.recruitment.user.entity.AvailabilityStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateCareerObjectiveRequest {
    @Size(max = 5000) private String objectiveText;
    @Size(max = 100) private String targetSeniority;
    private AvailabilityStatus availabilityStatus;
    private Long version;
}
