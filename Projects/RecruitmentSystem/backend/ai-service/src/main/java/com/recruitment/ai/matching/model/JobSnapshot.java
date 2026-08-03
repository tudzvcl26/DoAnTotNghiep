package com.recruitment.ai.matching.model;

import java.util.UUID;

public record JobSnapshot(
        UUID id,
        String title,
        String description,
        String requirements,
        String responsibilities,
        String experienceLevel,
        String status,
        boolean active,
        UUID companyId,
        UUID companyOwnerId
) {
    public String searchableText() {
        return String.join("\n", safe(title), safe(description), safe(requirements), safe(responsibilities));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
