package com.recruitment.ai.service.document;

import com.recruitment.ai.entity.enums.ResumeFileType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
public class PdfDocumentExtractor implements DocumentExtractor {

    @Override
    public ResumeFileType supportedType() {
        return ResumeFileType.PDF;
    }

    @Override
    public String extract(byte[] documentBytes) throws Exception {
        try (PDDocument document = Loader.loadPDF(documentBytes)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
