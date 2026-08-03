package com.recruitment.ai.service.document;

import com.recruitment.ai.config.ResumePipelineProperties;
import com.recruitment.ai.entity.enums.ResumeFileType;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@RequiredArgsConstructor
public class ResumeFileValidator {

    private static final long ABSOLUTE_MAX_BYTES = 10L * 1024 * 1024;

    private final ResumePipelineProperties properties;

    public ValidatedResumeFile validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.RESUME_FILE_REQUIRED);
        }
        if (file.getSize() > maximumBytes()) {
            throw new BusinessException(ErrorCode.RESUME_FILE_TOO_LARGE);
        }
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length == 0) {
                throw new BusinessException(ErrorCode.RESUME_FILE_REQUIRED);
            }
            if (bytes.length > maximumBytes()) {
                throw new BusinessException(ErrorCode.RESUME_FILE_TOO_LARGE);
            }
            String filename = safeFilename(file.getOriginalFilename());
            ResumeFileType type = detect(filename, file.getContentType(), bytes);
            return new ValidatedResumeFile(
                    filename,
                    type.contentType(),
                    type,
                    bytes,
                    HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.RESUME_FILE_READ_FAILED);
        }
    }

    private long maximumBytes() {
        return Math.min(ABSOLUTE_MAX_BYTES, properties.getMaxFileSize().toBytes());
    }

    private ResumeFileType detect(String filename, String suppliedContentType, byte[] bytes) {
        String lowerName = filename.toLowerCase(Locale.ROOT);
        String normalizedType = suppliedContentType == null ? "" : suppliedContentType.toLowerCase(Locale.ROOT);
        for (ResumeFileType type : ResumeFileType.values()) {
            if (lowerName.endsWith(type.extension())
                    && (normalizedType.isBlank() || normalizedType.equals(type.contentType()))
                    && signatureMatches(type, bytes)) {
                return type;
            }
        }
        throw new BusinessException(ErrorCode.RESUME_FILE_TYPE_UNSUPPORTED);
    }

    private boolean signatureMatches(ResumeFileType type, byte[] bytes) {
        return switch (type) {
            case PDF -> bytes.length >= 5
                    && new String(bytes, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-");
            case DOCX -> isDocxPackage(bytes);
            case TXT -> isText(bytes);
        };
    }

    private boolean isDocxPackage(byte[] bytes) {
        if (bytes.length < 4 || bytes[0] != 'P' || bytes[1] != 'K') {
            return false;
        }
        boolean contentTypes = false;
        boolean documentXml = false;
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            int inspected = 0;
            while ((entry = input.getNextEntry()) != null && inspected++ < 2_000) {
                contentTypes |= "[Content_Types].xml".equals(entry.getName());
                documentXml |= "word/document.xml".equals(entry.getName());
                if (contentTypes && documentXml) {
                    return true;
                }
            }
            return false;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean isText(byte[] bytes) {
        int controlCharacters = 0;
        int inspected = Math.min(bytes.length, 8_192);
        for (int index = 0; index < inspected; index++) {
            int value = bytes[index] & 0xff;
            if (value == 0) {
                return false;
            }
            if (value < 0x09 || (value > 0x0D && value < 0x20)) {
                controlCharacters++;
            }
        }
        return inspected == 0 || controlCharacters * 100 / inspected < 2;
    }

    private String safeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(ErrorCode.RESUME_FILE_TYPE_UNSUPPORTED);
        }
        String normalized = originalFilename.replace('\\', '/');
        String basename = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (basename.isBlank()) {
            throw new BusinessException(ErrorCode.RESUME_FILE_TYPE_UNSUPPORTED);
        }
        basename = basename.replaceAll("[\\p{Cntrl}]", "_");
        return basename.length() <= 255 ? basename : basename.substring(basename.length() - 255);
    }
}
