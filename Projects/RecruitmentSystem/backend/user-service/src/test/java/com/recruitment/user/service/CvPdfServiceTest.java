package com.recruitment.user.service;

import com.recruitment.user.dto.cv.CvDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
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
            var continuation = new PDFRenderer(pdf).renderImageWithDPI(1, 72);
            java.nio.file.Files.createDirectories(java.nio.file.Path.of("target/qa"));
            javax.imageio.ImageIO.write(continuation, "png", new java.io.File("target/qa/pdf-continuation.png"));
            int darkPixels = 0;
            for (int y = 35; y < 120; y++) for (int x = 45; x < 540; x++) {
                var color = new java.awt.Color(continuation.getRGB(x, y));
                if (color.getRed() < 180 && color.getGreen() < 180 && color.getBlue() < 180) darkPixels++;
            }
            assertThat(darkPixels).as("Continuation-page text must be visible on its background").isGreaterThan(100);
        }
    }

    @Test
    void exportsPastedUnicodeControlsAndMissingOptionalContactFields() throws Exception {
        CvDocument cv = new CvDocument(new CvDocument.CvPersonalInfo("Nguyễn Văn An", null, null, null, null, null),
                "Phát triển phần mềm\tJava\nKỹ năng 🚀 và tiếng Việt e\u0302\u0301.", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        try (PDDocument pdf = PDDocument.load(new CvPdfService().render("classic", cv))) {
            assertThat(new PDFTextStripper().getText(pdf)).contains("Nguyễn Văn An", "Phát triển phần mềm", "Java");
        }
    }

    @Test
    void appliesDesignVisibilityOrderAndCustomSectionsToPdf() throws Exception {
        CvDocument.CvDesignConfig design = new CvDocument.CvDesignConfig("Georgia", .9,
                new CvDocument.CvThemeConfig("burgundy", "#7A1F3D", "#F7E9EE", "#21181B", "#75666B", "#FFFFFF"),
                "compact", "sidebar-left", List.of("skills", "custom:references", "summary"), Map.of("summary", false));
        CvDocument cv = new CvDocument(
                new CvDocument.CvPersonalInfo("Nguyễn Đình Tuấn Tú", "Java Developer", "tu@example.test", "", "Hà Nội", ""),
                "Mục này phải được ẩn", List.of(), List.of(), List.of("Spring Boot"), List.of(), List.of(), List.of(), List.of(), design,
                List.of(new CvDocument.CvCustomSection("references", "Người tham chiếu",
                        List.of(new CvDocument.CvNamedItem("Trần Minh Anh", "Tech Lead", "anh@example.test")), true)));

        byte[] bytes = new CvPdfService().render("modern", "vi", cv);
        try (PDDocument pdf = PDDocument.load(bytes)) {
            String text = new PDFTextStripper().getText(pdf);
            assertThat(text).contains("Nguyễn Đình Tuấn Tú", "KỸ NĂNG", "NGƯỜI THAM CHIẾU", "Trần Minh Anh")
                    .doesNotContain("Mục này phải được ẩn");
            var positions = new java.util.HashMap<String, Float>();
            new PDFTextStripper() {
                @Override protected void writeString(String value, List<org.apache.pdfbox.text.TextPosition> glyphs) throws java.io.IOException {
                    if (value.contains("Spring Boot")) positions.put("sidebar", glyphs.get(0).getXDirAdj());
                    if (value.contains("Trần Minh Anh")) positions.put("main", glyphs.get(0).getXDirAdj());
                    super.writeString(value, glyphs);
                }
            }.getText(pdf);
            assertThat(positions.get("sidebar")).isLessThan(100f);
            assertThat(positions.get("main")).isGreaterThan(200f);
            java.nio.file.Files.createDirectories(java.nio.file.Path.of("target/qa"));
            javax.imageio.ImageIO.write(new PDFRenderer(pdf).renderImageWithDPI(0, 96), "png", new java.io.File("target/qa/pdf-sidebar.png"));
        }
    }

    private static org.assertj.core.data.Offset<Float> within(float value) { return org.assertj.core.data.Offset.offset(value); }
}
