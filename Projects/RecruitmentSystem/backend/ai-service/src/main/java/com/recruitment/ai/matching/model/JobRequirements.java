package com.recruitment.ai.matching.model;

import java.util.List;

public record JobRequirements(
        List<String> requiredSkills,
        List<String> preferredSkills,
        List<String> softSkills,
        List<String> languages,
        List<String> keywords,
        int minimumExperienceYears,
        boolean degreeRequired,
        boolean certificateRequired
) {
}
