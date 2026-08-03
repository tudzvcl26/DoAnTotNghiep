package com.recruitment.ai.service.document;

import com.recruitment.ai.config.ResumePipelineProperties;
import com.recruitment.ai.entity.enums.ResumeFileType;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DocumentExtractorFactory {

    private final Map<ResumeFileType, DocumentExtractor> extractors;
    private final ResumePipelineProperties properties;

    public DocumentExtractorFactory(List<DocumentExtractor> extractors, ResumePipelineProperties properties) {
        this.extractors = new EnumMap<>(ResumeFileType.class);
        extractors.forEach(extractor -> this.extractors.put(extractor.supportedType(), extractor));
        this.properties = properties;
    }

    public String extract(ResumeFileType fileType, byte[] documentBytes) {
        DocumentExtractor extractor = extractors.get(fileType);
        if (extractor == null) {
            throw new BusinessException(ErrorCode.RESUME_FILE_TYPE_UNSUPPORTED);
        }
        try {
            String normalized = normalize(extractor.extract(documentBytes));
            if (normalized.isBlank()) {
                throw new BusinessException(ErrorCode.RESUME_TEXT_EMPTY);
            }
            if (normalized.length() > properties.getMaxExtractedCharacters()) {
                normalized = normalized.substring(0, properties.getMaxExtractedCharacters());
            }
            return normalized;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("Resume extraction failed type={} cause={}", fileType, exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.RESUME_EXTRACTION_FAILED);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\u0000', ' ')
                .replaceAll("[\\t\\x0B\\f\\r]+", " ")
                .replaceAll(" +", " ")
                .replaceAll(" *\\n *", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
