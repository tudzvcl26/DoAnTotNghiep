package com.recruitment.application.dto.response;

public record ApplicationResumeDownload(byte[] content, String filename, String contentType) {
}
