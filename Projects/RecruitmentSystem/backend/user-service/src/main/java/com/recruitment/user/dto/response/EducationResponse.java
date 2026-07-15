package com.recruitment.user.dto.response;
import lombok.Builder; import lombok.Getter; import java.time.LocalDate; import java.util.UUID;
@Getter @Builder public class EducationResponse { private UUID id; private String institutionName; private String qualification; private String fieldOfStudy; private LocalDate startDate; private LocalDate endDate; private String grade; private String description; private Long version; }
