package com.recruitment.user.service;

import com.recruitment.user.entity.ProfileAssetKind;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileAssetFileValidatorTest {

    private final ProfileAssetFileValidator validator = new ProfileAssetFileValidator(32, 64);

    @Test
    void acceptsValidPdfResumeAndUsesDetectedType() {
        MockMultipartFile file = file("resume.pdf", "application/pdf", "%PDF-1.7\n");

        ValidatedProfileAssetFile result = validator.validate(file, ProfileAssetKind.RESUME);

        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.extension()).isEqualTo("pdf");
    }

    @Test
    void acceptsValidPngAvatar() {
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", png);

        assertThat(validator.validate(file, ProfileAssetKind.AVATAR).contentType()).isEqualTo("image/png");
    }

    @Test
    void rejectsEmptyAndOversizedFiles() {
        assertThatThrownBy(() -> validator.validate(
                new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[0]),
                ProfileAssetKind.RESUME)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> validator.validate(
                new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[65]),
                ProfileAssetKind.RESUME)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsFakeExtensionWrongMimeAndInvalidMagicBytes() {
        assertThatThrownBy(() -> validator.validate(
                file("resume.exe", "application/pdf", "%PDF-1.7"), ProfileAssetKind.RESUME))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> validator.validate(
                file("resume.pdf", "text/plain", "%PDF-1.7"), ProfileAssetKind.RESUME))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> validator.validate(
                file("resume.pdf", "application/pdf", "not a pdf"), ProfileAssetKind.RESUME))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsTraversalFilename() {
        assertThatThrownBy(() -> validator.validate(
                file("../resume.pdf", "application/pdf", "%PDF-1.7"), ProfileAssetKind.RESUME))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private MockMultipartFile file(String filename, String contentType, String content) {
        return new MockMultipartFile("file", filename, contentType, content.getBytes(StandardCharsets.US_ASCII));
    }
}
