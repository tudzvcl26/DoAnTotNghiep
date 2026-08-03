package com.recruitment.ai.service.document;

import com.recruitment.ai.config.ResumePipelineProperties;
import com.recruitment.ai.entity.enums.ResumeFileType;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumeFileValidatorTest {

    private ResumeFileValidator validator;

    @BeforeEach
    void setUp() {
        ResumePipelineProperties properties = new ResumePipelineProperties();
        properties.setMaxFileSize(DataSize.ofMegabytes(10));
        validator = new ResumeFileValidator(properties);
    }

    @Test
    void acceptsUtf8TextAndCalculatesChecksum() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "candidate.txt", "text/plain", "Java Spring Boot".getBytes()
        );

        ValidatedResumeFile result = validator.validate(file);

        assertThat(result.fileType()).isEqualTo(ResumeFileType.TXT);
        assertThat(result.contentType()).isEqualTo("text/plain");
        assertThat(result.checksumSha256()).hasSize(64);
    }

    @Test
    void rejectsExtensionAndSignatureSpoofing() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "candidate.pdf", "application/pdf", "not-a-pdf".getBytes()
        );

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.RESUME_FILE_TYPE_UNSUPPORTED));
    }

    @Test
    void rejectsFilesOverTenMegabytes() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "candidate.txt", "text/plain", new byte[10 * 1024 * 1024 + 1]
        );

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.RESUME_FILE_TOO_LARGE));
    }
}
