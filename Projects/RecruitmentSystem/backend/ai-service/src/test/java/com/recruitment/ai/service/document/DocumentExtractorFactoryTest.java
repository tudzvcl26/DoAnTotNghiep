package com.recruitment.ai.service.document;

import com.recruitment.ai.config.ResumePipelineProperties;
import com.recruitment.ai.entity.enums.ResumeFileType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentExtractorFactoryTest {

    private DocumentExtractorFactory factory;

    @BeforeEach
    void setUp() {
        ResumePipelineProperties properties = new ResumePipelineProperties();
        properties.setMaxExtractedCharacters(200_000);
        factory = new DocumentExtractorFactory(
                List.of(new PdfDocumentExtractor(), new DocxDocumentExtractor(), new TxtDocumentExtractor()),
                properties
        );
    }

    @Test
    void extractsPdfDocxAndTxt() throws Exception {
        assertThat(factory.extract(ResumeFileType.PDF, pdf("PDF Resume Java")))
                .contains("PDF Resume Java");
        assertThat(factory.extract(ResumeFileType.DOCX, docx("DOCX Resume Spring")))
                .contains("DOCX Resume Spring");
        assertThat(factory.extract(ResumeFileType.TXT, "TXT Resume PostgreSQL".getBytes(StandardCharsets.UTF_8)))
                .contains("TXT Resume PostgreSQL");
    }

    private byte[] pdf(String text) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(50, 700);
                content.showText(text);
                content.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] docx(String text) throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText(text);
            document.write(output);
            return output.toByteArray();
        }
    }
}
