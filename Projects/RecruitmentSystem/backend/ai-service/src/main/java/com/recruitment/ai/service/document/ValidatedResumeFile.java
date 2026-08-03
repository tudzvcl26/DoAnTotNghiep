package com.recruitment.ai.service.document;

import com.recruitment.ai.entity.enums.ResumeFileType;

public record ValidatedResumeFile(
        String originalFilename,
        String contentType,
        ResumeFileType fileType,
        byte[] bytes,
        String checksumSha256
) {
    public long size() {
        return bytes.length;
    }
}
