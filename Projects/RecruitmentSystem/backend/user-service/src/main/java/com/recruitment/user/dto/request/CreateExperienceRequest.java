package com.recruitment.user.dto.request;

import com.recruitment.user.entity.EmploymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class CreateExperienceRequest {
    @NotBlank @Size(max = 255) private String employerName;
    @NotBlank @Size(max = 200) private String jobTitle;
    private EmploymentType employmentType;
    @Size(max = 255) private String location;
    @PastOrPresent private LocalDate startDate;
    private LocalDate endDate;
    private Boolean current;
    @Size(max = 5000) private String description;
    @Size(max = 5000) private String achievements;
}
