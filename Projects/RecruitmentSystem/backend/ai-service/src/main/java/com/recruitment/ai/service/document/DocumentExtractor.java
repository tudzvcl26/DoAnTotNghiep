package com.recruitment.ai.service.document;

import com.recruitment.ai.entity.enums.ResumeFileType;

public interface DocumentExtractor {

    ResumeFileType supportedType();

    String extract(byte[] documentBytes) throws Exception;
}
