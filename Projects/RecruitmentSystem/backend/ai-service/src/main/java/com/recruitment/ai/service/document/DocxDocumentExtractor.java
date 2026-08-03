package com.recruitment.ai.service.document;

import com.recruitment.ai.entity.enums.ResumeFileType;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
public class DocxDocumentExtractor implements DocumentExtractor {

    @Override
    public ResumeFileType supportedType() {
        return ResumeFileType.DOCX;
    }

    @Override
    public String extract(byte[] documentBytes) throws Exception {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(documentBytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
}
