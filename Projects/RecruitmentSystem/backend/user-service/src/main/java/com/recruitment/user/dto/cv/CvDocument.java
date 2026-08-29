package com.recruitment.user.dto.cv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CvDocument(
        @Valid CvPersonalInfo personalInfo,
        @Size(max = 5000) String summary,
        @Size(max = 30) List<@Valid CvExperience> experiences,
        @Size(max = 30) List<@Valid CvEducation> education,
        @Size(max = 50) List<@Size(max = 120) String> skills,
        @Size(max = 30) List<@Valid CvProject> projects,
        @Size(max = 30) List<@Valid CvCertification> certifications,
        @Size(max = 30) List<@Valid CvNamedItem> awards,
        @Size(max = 30) List<@Valid CvNamedItem> activities
) {
    public static CvDocument empty() {
        return new CvDocument(CvPersonalInfo.empty(), "", List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of());
    }

    public record CvPersonalInfo(
            @Size(max = 150) String fullName,
            @Size(max = 255) String headline,
            @Size(max = 255) String email,
            @Size(max = 30) String phone,
            @Size(max = 255) String location,
            @Size(max = 2048) String website
    ) {
        public static CvPersonalInfo empty() {
            return new CvPersonalInfo("", "", "", "", "", "");
        }
    }

    public record CvExperience(
            @Size(max = 200) String position,
            @Size(max = 255) String company,
            @Size(max = 100) String startDate,
            @Size(max = 100) String endDate,
            @Size(max = 5000) String description
    ) {}

    public record CvEducation(
            @Size(max = 255) String school,
            @Size(max = 255) String degree,
            @Size(max = 100) String startDate,
            @Size(max = 100) String endDate,
            @Size(max = 2000) String description
    ) {}

    public record CvProject(
            @Size(max = 200) String name,
            @Size(max = 2048) String url,
            @Size(max = 5000) String description
    ) {}

    public record CvCertification(
            @Size(max = 255) String name,
            @Size(max = 255) String issuer,
            @Size(max = 100) String date
    ) {}

    public record CvNamedItem(
            @Size(max = 255) String name,
            @Size(max = 100) String date,
            @Size(max = 3000) String description
    ) {}
}
