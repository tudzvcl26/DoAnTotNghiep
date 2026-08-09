package com.recruitment.user.service;

import com.recruitment.user.entity.ProfileAssetKind;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ProfileAssetFileValidator {

    private static final Set<String> DOCUMENT_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final Set<String> IMAGE_MIME_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp"
    );

    private final long avatarMaxBytes;
    private final long documentMaxBytes;

    public ProfileAssetFileValidator(
            @Value("${profile-assets.avatar-max-bytes:5242880}") long avatarMaxBytes,
            @Value("${profile-assets.document-max-bytes:10485760}") long documentMaxBytes
    ) {
        this.avatarMaxBytes = avatarMaxBytes;
        this.documentMaxBytes = documentMaxBytes;
    }

    public ValidatedProfileAssetFile validate(MultipartFile file, ProfileAssetKind kind) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required.");
        }
        if (kind == null) {
            throw new IllegalArgumentException("Asset kind is required.");
        }
        long maxBytes = kind == ProfileAssetKind.AVATAR ? avatarMaxBytes : documentMaxBytes;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("File exceeds the allowed size.");
        }

        String filename = normalizeFilename(file.getOriginalFilename());
        String extension = extension(filename);
        byte[] content = read(file);
        String detectedType = detect(content);
        validateKind(kind, extension, detectedType);
        validateClaimedType(file.getContentType(), detectedType);
        return new ValidatedProfileAssetFile(content, filename, detectedType, extension);
    }

    private String normalizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Filename is required.");
        }
        String normalized = Normalizer.normalize(originalFilename.trim(), Normalizer.Form.NFKC);
        if (normalized.contains("/") || normalized.contains("\\") || normalized.contains("..")
                || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Filename is invalid.");
        }
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("Filename is too long.");
        }
        return normalized;
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot <= 0 || dot == filename.length() - 1) {
            throw new IllegalArgumentException("File extension is required.");
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read uploaded file.");
        }
    }

    private String detect(byte[] content) {
        if (startsWith(content, new int[]{0x25, 0x50, 0x44, 0x46, 0x2D})) {
            return "application/pdf";
        }
        if (startsWith(content, new int[]{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})) {
            return "image/png";
        }
        if (startsWith(content, new int[]{0xFF, 0xD8, 0xFF})) {
            return "image/jpeg";
        }
        if (content.length >= 12 && ascii(content, 0, "RIFF") && ascii(content, 8, "WEBP")) {
            return "image/webp";
        }
        if (startsWith(content, new int[]{0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1})) {
            return "application/msword";
        }
        if (isDocx(content)) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        throw new IllegalArgumentException("File content is not an allowed type.");
    }

    private boolean isDocx(byte[] content) {
        if (!startsWith(content, new int[]{0x50, 0x4B, 0x03, 0x04})) {
            return false;
        }
        boolean contentTypes = false;
        boolean document = false;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            int entries = 0;
            while ((entry = zip.getNextEntry()) != null && entries++ < 2048) {
                String name = entry.getName();
                contentTypes |= "[Content_Types].xml".equals(name);
                document |= "word/document.xml".equals(name);
                if (contentTypes && document) {
                    return true;
                }
            }
            return false;
        } catch (IOException exception) {
            return false;
        }
    }

    private void validateKind(ProfileAssetKind kind, String extension, String contentType) {
        if (kind == ProfileAssetKind.AVATAR) {
            if (!IMAGE_MIME_TYPES.contains(contentType) || !matchesExtension(extension, contentType)) {
                throw new IllegalArgumentException("Avatar must be a valid PNG, JPEG, or WebP image.");
            }
            return;
        }
        Set<String> allowedTypes = DOCUMENT_MIME_TYPES;
        if (kind == ProfileAssetKind.PORTFOLIO || kind == ProfileAssetKind.CERTIFICATE_ATTACHMENT) {
            allowedTypes = Set.of(
                    "application/pdf", "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "image/png", "image/jpeg", "image/webp"
            );
        }
        if (!allowedTypes.contains(contentType) || !matchesExtension(extension, contentType)) {
            throw new IllegalArgumentException("File type is not allowed for this asset kind.");
        }
    }

    private void validateClaimedType(String claimedType, String detectedType) {
        if (claimedType == null || claimedType.isBlank()) {
            throw new IllegalArgumentException("Content type is required.");
        }
        String normalized = claimedType.toLowerCase(Locale.ROOT).split(";", 2)[0].trim();
        boolean jpegAlias = detectedType.equals("image/jpeg") && normalized.equals("image/jpg");
        if (!normalized.equals(detectedType) && !jpegAlias) {
            throw new IllegalArgumentException("Declared content type does not match file content.");
        }
    }

    private boolean matchesExtension(String extension, String contentType) {
        return switch (contentType) {
            case "application/pdf" -> extension.equals("pdf");
            case "application/msword" -> extension.equals("doc");
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> extension.equals("docx");
            case "image/png" -> extension.equals("png");
            case "image/jpeg" -> extension.equals("jpg") || extension.equals("jpeg");
            case "image/webp" -> extension.equals("webp");
            default -> false;
        };
    }

    private boolean startsWith(byte[] content, int[] signature) {
        if (content.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if ((content[index] & 0xFF) != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean ascii(byte[] content, int offset, String expected) {
        if (content.length < offset + expected.length()) {
            return false;
        }
        for (int index = 0; index < expected.length(); index++) {
            if (content[offset + index] != (byte) expected.charAt(index)) {
                return false;
            }
        }
        return true;
    }
}
