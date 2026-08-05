package com.recruitment.ai.service.document;

import com.recruitment.ai.config.ResumePipelineProperties;
import com.recruitment.ai.entity.enums.ResumeFileType;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayOutputStream;

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
    void acceptsRealPdfWithCaseInsensitiveExtensionAndOctetStreamMime() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "Gia-Bao-Nguyen-CV.PDF", "application/octet-stream", pdfBytes()
        );

        ValidatedResumeFile result = validator.validate(file);

        assertThat(result.fileType()).isEqualTo(ResumeFileType.PDF);
        assertThat(result.contentType()).isEqualTo("application/pdf");
    }

    @Test
    void acceptsReadablePdfWhoseHeaderFollowsAShortPreamble() throws Exception {
        byte[] pdf = pdfBytes();
        byte[] bytes = new byte[pdf.length + 3];
        bytes[0] = (byte) 0xEF;
        bytes[1] = (byte) 0xBB;
        bytes[2] = (byte) 0xBF;
        System.arraycopy(pdf, 0, bytes, 3, pdf.length);
        try (PDDocument ignored = Loader.loadPDF(bytes)) {
            assertThat(ignored.getNumberOfPages()).isEqualTo(1);
        }
        MockMultipartFile file = new MockMultipartFile(
                "file", "Gia-Bao-Nguyen-CV.pdf", "application/pdf", bytes
        );

        assertThat(validator.validate(file).fileType()).isEqualTo(ResumeFileType.PDF);
    }

    @Test
    void acceptsRealDocxWithCaseInsensitiveExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "candidate.DoCx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxBytes()
        );

        assertThat(validator.validate(file).fileType()).isEqualTo(ResumeFileType.DOCX);
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

    @Test
    void rejectsExecutableJpegAndPngPayloads() {
        assertUnsupported("candidate.exe", "application/octet-stream",
                new byte[]{'M', 'Z', 0, 1, 2, 3});
        assertUnsupported("candidate.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0});
        assertUnsupported("candidate.png", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});
    }

    private byte[] pdfBytes() throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] docxBytes() throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText("Java Spring Boot resume");
            document.write(output);
            return output.toByteArray();
        }
    }

    private void assertUnsupported(String filename, String contentType, byte[] bytes) {
        MockMultipartFile file = new MockMultipartFile("file", filename, contentType, bytes);
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.RESUME_FILE_TYPE_UNSUPPORTED));
    }
}
