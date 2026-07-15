package com.recruitment.user.dto.response;
import com.recruitment.user.entity.EmploymentType; import lombok.Builder; import lombok.Getter; import java.time.LocalDate; import java.util.UUID;
@Getter @Builder public class ExperienceResponse { private UUID id; private String employerName; private String jobTitle; private EmploymentType employmentType; private String location; private LocalDate startDate; private LocalDate endDate; private Boolean current; private String description; private String achievements; private Long version; }
