package com.recruitment.user.dto.cv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record CvDocument(
        @Valid CvPersonalInfo personalInfo,
        @Size(max = 5000) String summary,
        @Size(max = 30) List<@Valid CvExperience> experiences,
        @Size(max = 30) List<@Valid CvEducation> education,
        @Size(max = 50) List<@Size(max = 120) String> skills,
        @Size(max = 30) List<@Valid CvProject> projects,
        @Size(max = 30) List<@Valid CvCertification> certifications,
        @Size(max = 30) List<@Valid CvNamedItem> awards,
        @Size(max = 30) List<@Valid CvNamedItem> activities,
        @Valid CvDesignConfig designConfig,
        @Size(max = 20) List<@Valid CvCustomSection> customSections
) {
    public CvDocument(CvPersonalInfo personalInfo, String summary, List<CvExperience> experiences,
                      List<CvEducation> education, List<String> skills, List<CvProject> projects,
                      List<CvCertification> certifications, List<CvNamedItem> awards,
                      List<CvNamedItem> activities) {
        this(personalInfo, summary, experiences, education, skills, projects, certifications, awards,
                activities, CvDesignConfig.defaults(), List.of());
    }

    public static CvDocument empty() {
        return new CvDocument(CvPersonalInfo.empty(), "", List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), CvDesignConfig.defaults(), List.of());
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

    public record CvThemeConfig(
            @NotBlank @Size(max = 30) String id,
            @NotBlank @Pattern(regexp = "#[0-9A-Fa-f]{6}") String primaryColor,
            @NotBlank @Pattern(regexp = "#[0-9A-Fa-f]{6}") String secondaryColor,
            @NotBlank @Pattern(regexp = "#[0-9A-Fa-f]{6}") String textColor,
            @NotBlank @Pattern(regexp = "#[0-9A-Fa-f]{6}") String mutedColor,
            @NotBlank @Pattern(regexp = "#[0-9A-Fa-f]{6}") String backgroundColor
    ) {
        public static CvThemeConfig defaults() {
            return new CvThemeConfig("emerald", "#146F54", "#DDF5EA", "#1F2937", "#667085", "#FFFFFF");
        }
    }

    public record CvDesignConfig(
            @NotBlank @Pattern(regexp = "Roboto|Inter|Arial|Times New Roman|Georgia|Open Sans") String fontFamily,
            @DecimalMin("0.85") @DecimalMax("1.15") double fontScale,
            @Valid CvThemeConfig theme,
            @NotBlank @Pattern(regexp = "compact|normal|comfortable") String density,
            @NotBlank @Pattern(regexp = "single|header|sidebar-left|sidebar-right") String layout,
            @Size(max = 40) List<@Size(max = 100) String> sectionOrder,
            @Size(max = 40) Map<@Size(max = 100) String, Boolean> sectionVisibility
    ) {
        public static CvDesignConfig defaults() {
            return new CvDesignConfig("Inter", 1.0, CvThemeConfig.defaults(), "normal", "header",
                    List.of("summary", "experience", "education", "skills", "projects", "certifications", "awards", "activities"),
                    Map.of());
        }
    }

    public record CvCustomSection(
            @NotBlank @Size(max = 80) String id,
            @NotBlank @Size(max = 150) String title,
            @Size(max = 30) List<@Valid CvNamedItem> items,
            boolean visible
    ) {}
}
