package com.recruitment.user.dto.response;

import com.recruitment.user.dto.cv.CvDocument;

import java.time.LocalDateTime;
import java.util.UUID;

public record CandidateCvResponse(
        UUID id,
        String title,
        String templateId,
        String language,
        CvDocument content,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
