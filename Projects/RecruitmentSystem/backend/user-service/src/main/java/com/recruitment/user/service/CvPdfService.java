package com.recruitment.user.service;

import com.recruitment.user.dto.cv.CvDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class CvPdfService {

    private static final Map<String, TemplateStyle> TEMPLATES = Map.of(
            "classic", new TemplateStyle(20, 111, 84, false, true),
            "modern", new TemplateStyle(5, 150, 105, true, true),
            "ats", new TemplateStyle(35, 45, 48, false, false),
            "student", new TemplateStyle(37, 99, 180, true, false),
            "professional", new TemplateStyle(17, 47, 74, true, true)
    );

    public byte[] render(String templateId, CvDocument document) {
        TemplateStyle style = TEMPLATES.getOrDefault(templateId, TEMPLATES.get("classic"));
        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFont regular = loadFont(pdf, false);
            PDFont bold = loadFont(pdf, true);
            try (Writer writer = new Writer(pdf, regular, bold, style)) {
                writeHeader(writer, document, style);
                writer.section("GIỚI THIỆU");
                writer.paragraph(document.summary());
                writer.section("KINH NGHIỆM LÀM VIỆC");
                for (CvDocument.CvExperience item : safe(document.experiences())) {
                    writer.item(join(item.position(), item.company()), period(item.startDate(), item.endDate()), item.description());
                }
                writer.section("HỌC VẤN");
                for (CvDocument.CvEducation item : safe(document.education())) {
                    writer.item(join(item.degree(), item.school()), period(item.startDate(), item.endDate()), item.description());
                }
                writer.section("KỸ NĂNG");
                writer.bullets(safe(document.skills()));
                writer.section("DỰ ÁN");
                for (CvDocument.CvProject item : safe(document.projects())) {
                    writer.item(item.name(), item.url(), item.description());
                }
                writer.section("CHỨNG CHỈ");
                for (CvDocument.CvCertification item : safe(document.certifications())) {
                    writer.item(item.name(), join(item.issuer(), item.date()), "");
                }
                writer.section("GIẢI THƯỞNG");
                for (CvDocument.CvNamedItem item : safe(document.awards())) {
                    writer.item(item.name(), item.date(), item.description());
                }
                writer.section("HOẠT ĐỘNG");
                for (CvDocument.CvNamedItem item : safe(document.activities())) {
                    writer.item(item.name(), item.date(), item.description());
                }
            }
            pdf.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate CV PDF", exception);
        }
    }

    private void writeHeader(Writer writer, CvDocument document, TemplateStyle style) throws IOException {
        CvDocument.CvPersonalInfo info = document.personalInfo() == null
                ? CvDocument.CvPersonalInfo.empty() : document.personalInfo();
        if (style.banner()) writer.banner();
        writer.title(info.fullName());
        writer.subtitle(info.headline());
        writer.contact(List.of(info.email(), info.phone(), info.location(), info.website()));
        writer.rule();
    }

    private PDFont loadFont(PDDocument pdf, boolean bold) throws IOException {
        List<Path> candidates = bold
                ? List.of(Path.of("/usr/share/fonts/dejavu/DejaVuSans-Bold.ttf"),
                        Path.of("/usr/share/fonts/TTF/DejaVuSans-Bold.ttf"),
                        Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"),
                        Path.of("C:/Windows/Fonts/arialbd.ttf"))
                : List.of(Path.of("/usr/share/fonts/dejavu/DejaVuSans.ttf"),
                        Path.of("/usr/share/fonts/TTF/DejaVuSans.ttf"),
                        Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
                        Path.of("C:/Windows/Fonts/arial.ttf"));
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                try (InputStream stream = Files.newInputStream(candidate)) {
                    return PDType0Font.load(pdf, stream, true);
                }
            }
        }
        throw new IllegalStateException("A Unicode TrueType font is required to export CV PDF");
    }

    private static String join(String first, String second) {
        if (blank(first)) return text(second);
        if (blank(second)) return text(first);
        return first + " · " + second;
    }
    private static String period(String start, String end) { return join(start, end); }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static String text(String value) { return value == null ? "" : value.trim(); }
    private static <T> List<T> safe(List<T> value) { return value == null ? List.of() : value; }

    private record TemplateStyle(int red, int green, int blue, boolean banner, boolean strongHeadings) {
        PDColor accent() { return new PDColor(new float[]{red / 255f, green / 255f, blue / 255f}, PDDeviceRGB.INSTANCE); }
    }

    private static final class Writer implements AutoCloseable {
        private static final float WIDTH = PDRectangle.A4.getWidth();
        private static final float HEIGHT = PDRectangle.A4.getHeight();
        private static final float MARGIN = 46;
        private static final float CONTENT_WIDTH = WIDTH - MARGIN * 2;
        private final PDDocument pdf;
        private final PDFont regular;
        private final PDFont bold;
        private final TemplateStyle style;
        private PDPageContentStream stream;
        private float y;

        Writer(PDDocument pdf, PDFont regular, PDFont bold, TemplateStyle style) throws IOException {
            this.pdf = pdf;
            this.regular = regular;
            this.bold = bold;
            this.style = style;
            newPage();
        }

        void banner() throws IOException {
            stream.setNonStrokingColor(style.accent());
            stream.addRect(0, HEIGHT - 150, WIDTH, 150);
            stream.fill();
        }

        void title(String value) throws IOException { line(text(value).isBlank() ? "CV ỨNG VIÊN" : value, 24, true, 30); }
        void subtitle(String value) throws IOException { if (!blank(value)) line(value, 12, true, 20); }

        void contact(List<String> values) throws IOException {
            String joined = values.stream().filter(value -> !blank(value)).map(CvPdfService::text)
                    .reduce((left, right) -> left + "  •  " + right).orElse("");
            if (!joined.isBlank()) wrapped(joined, 9.5f, false, 15);
        }

        void rule() throws IOException {
            ensure(18);
            stream.setStrokingColor(style.accent());
            stream.setLineWidth(1.4f);
            stream.moveTo(MARGIN, y);
            stream.lineTo(WIDTH - MARGIN, y);
            stream.stroke();
            y -= 18;
        }

        void section(String label) throws IOException {
            ensure(32);
            y -= 7;
            line(label, style.strongHeadings() ? 11.5f : 10.5f, true, 18);
        }

        void paragraph(String value) throws IOException {
            if (!blank(value)) wrapped(value, 10, false, 14);
        }

        void item(String heading, String meta, String description) throws IOException {
            if (blank(heading) && blank(description)) return;
            ensure(42);
            if (!blank(heading)) wrapped(heading, 10.5f, true, 14);
            if (!blank(meta)) wrapped(meta, 9, false, 13);
            if (!blank(description)) wrapped(description, 9.5f, false, 14);
            y -= 4;
        }

        void bullets(List<String> items) throws IOException {
            for (String item : items) if (!blank(item)) wrapped("• " + item, 10, false, 14);
        }

        void line(String value, float size, boolean isBold, float leading) throws IOException {
            ensure(leading);
            show(value, size, isBold);
            y -= leading;
        }

        void wrapped(String value, float size, boolean isBold, float leading) throws IOException {
            for (String paragraph : text(value).replace('\r', '\n').split("\\n+")) {
                for (String line : wrap(paragraph, isBold ? bold : regular, size)) {
                    ensure(leading);
                    show(line, size, isBold);
                    y -= leading;
                }
            }
        }

        private List<String> wrap(String value, PDFont font, float size) throws IOException {
            java.util.ArrayList<String> lines = new java.util.ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (String word : value.trim().split("\\s+")) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                if (!current.isEmpty() && font.getStringWidth(candidate) / 1000f * size > CONTENT_WIDTH) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else current = new StringBuilder(candidate);
            }
            if (!current.isEmpty()) lines.add(current.toString());
            return lines.isEmpty() ? List.of("") : lines;
        }

        private void show(String value, float size, boolean isBold) throws IOException {
            stream.beginText();
            stream.setFont(isBold ? bold : regular, size);
            stream.setNonStrokingColor(style.banner() && y > HEIGHT - 150
                    ? new PDColor(new float[]{1, 1, 1}, PDDeviceRGB.INSTANCE)
                    : new PDColor(new float[]{0.12f, 0.16f, 0.17f}, PDDeviceRGB.INSTANCE));
            stream.newLineAtOffset(MARGIN, y);
            stream.showText(text(value));
            stream.endText();
        }

        private void ensure(float required) throws IOException {
            if (y - required < MARGIN) newPage();
        }

        private void newPage() throws IOException {
            if (stream != null) stream.close();
            PDPage page = new PDPage(PDRectangle.A4);
            pdf.addPage(page);
            stream = new PDPageContentStream(pdf, page);
            y = HEIGHT - MARGIN;
        }

        @Override public void close() throws IOException { if (stream != null) stream.close(); }
    }
}
