package com.recruitment.ai.dto.response;

import com.recruitment.ai.entity.enums.ResumeDocumentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResumeDocumentResponse(
        UUID id,
        UUID ownerUserId,
        String bucket,
        String objectKey,
        String originalFilename,
        String contentType,
        long fileSize,
        String checksumSha256,
        ResumeDocumentStatus status,
        long extractionDurationMs,
        LocalDateTime uploadTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
