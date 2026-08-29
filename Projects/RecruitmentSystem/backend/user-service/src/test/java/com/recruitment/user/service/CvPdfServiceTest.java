package com.recruitment.user.service;

import com.recruitment.user.dto.cv.CvDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class CvPdfServiceTest {
    @Test
    void exportsSelectableVietnameseA4MultipagePdf() throws Exception {
        List<CvDocument.CvExperience> experiences = IntStream.range(0, 30)
                .mapToObj(index -> new CvDocument.CvExperience("Kỹ sư phần mềm " + index, "Công ty Việt Nam", "2020", "2026", "Thiết kế và phát triển hệ thống tuyển dụng, cải thiện hiệu năng và bảo mật cho người dùng."))
                .toList();
        CvDocument cv = new CvDocument(new CvDocument.CvPersonalInfo("Nguyễn Thị Ánh", "Kỹ sư phần mềm", "anh@example.test", "0900000000", "Thành phố Hồ Chí Minh", "https://example.test"),
                "Mục tiêu xây dựng sản phẩm hữu ích cho cộng đồng.", experiences, List.of(), List.of("Java", "Spring Boot", "Tiếng Việt"), List.of(), List.of(), List.of(), List.of());

        byte[] bytes = new CvPdfService().render("professional", cv);
        try (PDDocument pdf = PDDocument.load(bytes)) {
            assertThat(pdf.getNumberOfPages()).isGreaterThan(1);
            assertThat(pdf.getPage(0).getMediaBox().getWidth()).isCloseTo(595.28f, within(0.2f));
            assertThat(pdf.getPage(0).getMediaBox().getHeight()).isCloseTo(841.89f, within(0.2f));
            assertThat(new PDFTextStripper().getText(pdf)).contains("Nguyễn Thị Ánh", "KỸ NĂNG", "Spring Boot");
        }
    }

    private static org.assertj.core.data.Offset<Float> within(float value) { return org.assertj.core.data.Offset.offset(value); }
}
