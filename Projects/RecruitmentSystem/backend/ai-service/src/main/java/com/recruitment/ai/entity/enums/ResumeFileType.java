package com.recruitment.ai.entity.enums;

public enum ResumeFileType {
    PDF("application/pdf", ".pdf"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"),
    TXT("text/plain", ".txt");

    private final String contentType;
    private final String extension;

    ResumeFileType(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() {
        return contentType;
    }

    public String extension() {
        return extension;
    }
}
