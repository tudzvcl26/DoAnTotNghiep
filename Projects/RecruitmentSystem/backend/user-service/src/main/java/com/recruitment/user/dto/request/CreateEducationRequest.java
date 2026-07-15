package com.recruitment.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class CreateEducationRequest {
    @NotBlank @Size(max = 255) private String institutionName;
    @NotBlank @Size(max = 150) private String qualification;
    @Size(max = 200) private String fieldOfStudy;
    @PastOrPresent private LocalDate startDate;
    private LocalDate endDate;
    @Size(max = 50) private String grade;
    @Size(max = 5000) private String description;
}
