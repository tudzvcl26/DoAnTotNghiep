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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

@Service
public class CvPdfService {

    private static final List<String> DEFAULT_ORDER = List.of(
            "summary", "experience", "education", "skills", "projects", "certifications", "awards", "activities");
    private static final Map<String, TemplateStyle> TEMPLATES = Map.of(
            "classic", new TemplateStyle(20, 111, 84, false, true),
            "modern", new TemplateStyle(5, 150, 105, true, true),
            "ats", new TemplateStyle(35, 45, 48, false, false),
            "student", new TemplateStyle(37, 99, 180, true, false),
            "professional", new TemplateStyle(17, 47, 74, true, true)
    );

    public byte[] render(String templateId, CvDocument document) {
        return render(templateId, "vi", document);
    }

    public byte[] render(String templateId, String language, CvDocument document) {
        CvDocument safeDocument = document == null ? CvDocument.empty() : document;
        CvDocument.CvDesignConfig design = safeDocument.designConfig() == null
                ? CvDocument.CvDesignConfig.defaults() : safeDocument.designConfig();
        TemplateStyle template = TEMPLATES.getOrDefault(templateId, TEMPLATES.get("classic"));
        Palette palette = Palette.from(design.theme(), template);
        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFont regular = loadFont(pdf, false, design.fontFamily());
            PDFont bold = loadFont(pdf, true, design.fontFamily());
            try (Writer writer = new Writer(pdf, regular, bold, template, palette, design)) {
                writeHeader(writer, safeDocument, template, design);
                Map<String, CvDocument.CvCustomSection> customSections = new LinkedHashMap<>();
                safe(safeDocument.customSections()).forEach(section -> customSections.put("custom:" + section.id(), section));
                List<String> order = safe(design.sectionOrder()).isEmpty() ? DEFAULT_ORDER : design.sectionOrder();
                boolean sidebar = design.layout().startsWith("sidebar-");
                float bodyTop = writer.y;
                // Match the shared preview's section assignment; each column
                // paginates independently, reusing rather than repainting pages.
                var sidebarIds = java.util.Set.of("education", "skills", "certifications");
                for (int column = 0; column < (sidebar ? 2 : 1); column++) {
                if (sidebar) writer.column(column == 0, bodyTop);
                for (String sectionId : order) {
                    if (sidebar && sidebarIds.contains(sectionId) != (column == 0)) continue;
                    if (Boolean.FALSE.equals(safeMap(design.sectionVisibility()).get(sectionId))) continue;
                    if (sectionId.startsWith("custom:")) {
                        CvDocument.CvCustomSection section = customSections.get(sectionId);
                        if (section != null && section.visible()) writeCustomSection(writer, section);
                    } else writeBuiltInSection(writer, safeDocument, sectionId, language);
                }
                }
            }
            pdf.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate CV PDF", exception);
        }
    }

    private void writeBuiltInSection(Writer writer, CvDocument document, String sectionId, String language) throws IOException {
        switch (sectionId) {
            case "summary" -> {
                if (!blank(document.summary())) { writer.section(label(sectionId, language)); writer.paragraph(document.summary()); }
            }
            case "experience" -> {
                if (!safe(document.experiences()).isEmpty()) writer.section(label(sectionId, language));
                for (CvDocument.CvExperience item : safe(document.experiences())) writer.item(join(item.position(), item.company()), period(item.startDate(), item.endDate()), item.description());
            }
            case "education" -> {
                if (!safe(document.education()).isEmpty()) writer.section(label(sectionId, language));
                for (CvDocument.CvEducation item : safe(document.education())) writer.item(join(item.degree(), item.school()), period(item.startDate(), item.endDate()), item.description());
            }
            case "skills" -> {
                if (!safe(document.skills()).isEmpty()) writer.section(label(sectionId, language));
                writer.bullets(safe(document.skills()));
            }
            case "projects" -> {
                if (!safe(document.projects()).isEmpty()) writer.section(label(sectionId, language));
                for (CvDocument.CvProject item : safe(document.projects())) writer.item(item.name(), item.url(), item.description());
            }
            case "certifications" -> {
                if (!safe(document.certifications()).isEmpty()) writer.section(label(sectionId, language));
                for (CvDocument.CvCertification item : safe(document.certifications())) writer.item(item.name(), join(item.issuer(), item.date()), "");
            }
            case "awards" -> writeNamedItems(writer, label(sectionId, language), document.awards());
            case "activities" -> writeNamedItems(writer, label(sectionId, language), document.activities());
            default -> { }
        }
    }

    private void writeCustomSection(Writer writer, CvDocument.CvCustomSection section) throws IOException {
        if (safe(section.items()).isEmpty()) return;
        writer.section(section.title().toUpperCase());
        for (CvDocument.CvNamedItem item : safe(section.items())) writer.item(item.name(), item.date(), item.description());
    }

    private void writeNamedItems(Writer writer, String title, List<CvDocument.CvNamedItem> items) throws IOException {
        if (safe(items).isEmpty()) return;
        writer.section(title);
        for (CvDocument.CvNamedItem item : safe(items)) writer.item(item.name(), item.date(), item.description());
    }

    private void writeHeader(Writer writer, CvDocument document, TemplateStyle style, CvDocument.CvDesignConfig design) throws IOException {
        CvDocument.CvPersonalInfo info = document.personalInfo() == null ? CvDocument.CvPersonalInfo.empty() : document.personalInfo();
        if (!"single".equals(design.layout()) && (style.banner() || "header".equals(design.layout())
                || design.layout().startsWith("sidebar-"))) {
            writer.banner();
        }
        writer.title(info.fullName()); writer.subtitle(info.headline());
        writer.contact(List.of(text(info.email()), text(info.phone()), text(info.location()), text(info.website()))); writer.rule();
    }

    private PDFont loadFont(PDDocument pdf, boolean bold, String family) throws IOException {
        boolean serif = "Times New Roman".equals(family) || "Georgia".equals(family);
        List<Path> preferred = serif
                ? bold ? List.of(Path.of("/usr/share/fonts/dejavu/DejaVuSerif-Bold.ttf"), Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf"), Path.of("C:/Windows/Fonts/timesbd.ttf"), Path.of("C:/Windows/Fonts/georgiab.ttf"))
                       : List.of(Path.of("/usr/share/fonts/dejavu/DejaVuSerif.ttf"), Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf"), Path.of("C:/Windows/Fonts/times.ttf"), Path.of("C:/Windows/Fonts/georgia.ttf"))
                : bold ? List.of(Path.of("/usr/share/fonts/dejavu/DejaVuSans-Bold.ttf"), Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"), Path.of("/usr/share/fonts/TTF/DejaVuSans-Bold.ttf"), Path.of("C:/Windows/Fonts/arialbd.ttf"))
                       : List.of(Path.of("/usr/share/fonts/dejavu/DejaVuSans.ttf"), Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"), Path.of("/usr/share/fonts/TTF/DejaVuSans.ttf"), Path.of("C:/Windows/Fonts/arial.ttf"));
        for (Path candidate : preferred) if (Files.isRegularFile(candidate)) {
            try (InputStream stream = Files.newInputStream(candidate)) { return PDType0Font.load(pdf, stream, true); }
        }
        throw new IllegalStateException("A Unicode TrueType font is required to export CV PDF");
    }

    private static String label(String id, String language) {
        boolean english = "en".equalsIgnoreCase(language);
        return switch (id) {
            case "summary" -> english ? "PROFESSIONAL SUMMARY" : "GIỚI THIỆU";
            case "experience" -> english ? "WORK EXPERIENCE" : "KINH NGHIỆM LÀM VIỆC";
            case "education" -> english ? "EDUCATION" : "HỌC VẤN";
            case "skills" -> english ? "SKILLS" : "KỸ NĂNG";
            case "projects" -> english ? "PROJECTS" : "DỰ ÁN";
            case "certifications" -> english ? "CERTIFICATIONS" : "CHỨNG CHỈ";
            case "awards" -> english ? "AWARDS" : "GIẢI THƯỞNG";
            case "activities" -> english ? "ACTIVITIES" : "HOẠT ĐỘNG";
            default -> id.toUpperCase();
        };
    }

    private static String join(String first, String second) { if (blank(first)) return text(second); if (blank(second)) return text(first); return first + " · " + second; }
    private static String period(String start, String end) { return join(start, end); }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static String text(String value) { return value == null ? "" : value.trim(); }
    private static <T> List<T> safe(List<T> value) { return value == null ? List.of() : value; }
    private static <K, V> Map<K, V> safeMap(Map<K, V> value) { return value == null ? Map.of() : value; }

    private record TemplateStyle(int red, int green, int blue, boolean banner, boolean strongHeadings) {}
    private record Palette(PDColor primary, PDColor secondary, PDColor text, PDColor muted, PDColor background) {
        static Palette from(CvDocument.CvThemeConfig theme, TemplateStyle template) {
            CvDocument.CvThemeConfig safeTheme = theme == null ? CvDocument.CvThemeConfig.defaults() : theme;
            return new Palette(color(safeTheme.primaryColor(), template.red(), template.green(), template.blue()),
                    color(safeTheme.secondaryColor(), 232, 245, 240), color(safeTheme.textColor(), 31, 41, 55),
                    color(safeTheme.mutedColor(), 102, 112, 133), color(safeTheme.backgroundColor(), 255, 255, 255));
        }
        private static PDColor color(String hex, int fallbackRed, int fallbackGreen, int fallbackBlue) {
            try {
                String value = hex == null ? "" : hex.replace("#", "");
                if (value.length() != 6) throw new NumberFormatException();
                return rgb(Integer.parseInt(value.substring(0, 2), 16), Integer.parseInt(value.substring(2, 4), 16), Integer.parseInt(value.substring(4, 6), 16));
            } catch (NumberFormatException ignored) { return rgb(fallbackRed, fallbackGreen, fallbackBlue); }
        }
        private static PDColor rgb(int red, int green, int blue) { return new PDColor(new float[]{red / 255f, green / 255f, blue / 255f}, PDDeviceRGB.INSTANCE); }
    }

    private static final class Writer implements AutoCloseable {
        private static final float WIDTH = PDRectangle.A4.getWidth();
        private static final float HEIGHT = PDRectangle.A4.getHeight();
        private static final float BASE_MARGIN = 46;
        private final PDDocument pdf; private final PDFont regular; private final PDFont bold;
        private final TemplateStyle style; private final Palette palette; private final double fontScale;
        private final double densityScale; private final String layout; private float leftMargin;
        private float rightMargin; private float contentWidth;
        private PDPageContentStream stream; private float y;
        private boolean pageHasBanner;
        private int pageIndex = -1;

        Writer(PDDocument pdf, PDFont regular, PDFont bold, TemplateStyle style, Palette palette, CvDocument.CvDesignConfig design) throws IOException {
            this.pdf = pdf; this.regular = regular; this.bold = bold; this.style = style; this.palette = palette;
            this.fontScale = design.fontScale() < 0.85 || design.fontScale() > 1.15 ? 1 : design.fontScale();
            this.densityScale = switch (design.density()) { case "compact" -> .82; case "comfortable" -> 1.18; default -> 1; };
            this.layout = design.layout(); this.leftMargin = BASE_MARGIN;
            this.rightMargin = BASE_MARGIN; this.contentWidth = WIDTH - leftMargin - rightMargin; newPage();
        }

        void column(boolean secondary, float firstPageTop) throws IOException {
            float gap = 22, sideWidth = 148, mainWidth = WIDTH - 2 * BASE_MARGIN - gap - sideWidth;
            boolean left = "sidebar-left".equals(layout);
            leftMargin = secondary ? (left ? BASE_MARGIN : BASE_MARGIN + mainWidth + gap)
                    : (left ? BASE_MARGIN + sideWidth + gap : BASE_MARGIN);
            contentWidth = secondary ? sideWidth : mainWidth;
            rightMargin = WIDTH - leftMargin - contentWidth;
            pageIndex = -1;
            newPage();
            y = firstPageTop;
        }

        void banner() throws IOException { pageHasBanner = true; stream.setNonStrokingColor(palette.primary()); stream.addRect(0, HEIGHT - 150, WIDTH, 150); stream.fill(); }
        void title(String value) throws IOException { wrapped(text(value).isBlank() ? "CV ỨNG VIÊN" : value, 24, true, 30); }
        void subtitle(String value) throws IOException { if (!blank(value)) wrapped(value, 12, true, 20); }
        void contact(List<String> values) throws IOException {
            String joined = values.stream().filter(value -> !blank(value)).map(CvPdfService::text).reduce((left, right) -> left + "  •  " + right).orElse("");
            if (!joined.isBlank()) wrapped(joined, 9.5f, false, 15);
        }
        void rule() throws IOException { if (pageHasBanner) y = Math.min(y, HEIGHT - 158); ensure(spacing(18)); stream.setStrokingColor(palette.primary()); stream.setLineWidth(1.4f); stream.moveTo(leftMargin, y); stream.lineTo(WIDTH - rightMargin, y); stream.stroke(); y -= spacing(18); }
        void section(String label) throws IOException { ensure(spacing(70)); y -= spacing(7); wrapped(label, style.strongHeadings() ? 11.5f : 10.5f, true, 18); }
        void paragraph(String value) throws IOException { if (!blank(value)) wrapped(value, 10, false, 14); }
        void item(String heading, String meta, String description) throws IOException {
            if (blank(heading) && blank(description)) return; ensure(spacing(42));
            if (!blank(heading)) wrapped(heading, 10.5f, true, 14); if (!blank(meta)) wrapped(meta, 9, false, 13);
            if (!blank(description)) wrapped(description, 9.5f, false, 14); y -= spacing(4);
        }
        void bullets(List<String> items) throws IOException { for (String item : items) if (!blank(item)) wrapped("• " + item, 10, false, 14); }
        void line(String value, float size, boolean isBold, float leading) throws IOException { float spaced = spacing(leading); ensure(spaced); show(value, scaled(size), isBold); y -= spaced; }
        void wrapped(String value, float size, boolean isBold, float leading) throws IOException {
            float scaledSize = scaled(size); float spaced = spacing(leading);
            for (String paragraph : text(value).replace("\r\n", "\n").replace('\r', '\n').split("\\n", -1)) for (String line : wrap(paragraph, isBold ? bold : regular, scaledSize)) { ensure(spaced); show(line, scaledSize, isBold); y -= spaced; }
        }
        private List<String> wrap(String value, PDFont font, float size) throws IOException {
            java.util.ArrayList<String> lines = new java.util.ArrayList<>(); StringBuilder current = new StringBuilder();
            for (String word : printable(value, font).trim().split("\\s+")) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                if (font.getStringWidth(candidate) / 1000f * size <= contentWidth) { current = new StringBuilder(candidate); continue; }
                if (!current.isEmpty()) { lines.add(current.toString()); current.setLength(0); }
                for (int point : word.codePoints().toArray()) {
                    String glyph = new String(Character.toChars(point));
                    if (!current.isEmpty() && font.getStringWidth(current + glyph) / 1000f * size > contentWidth) {
                        lines.add(current.toString()); current.setLength(0);
                    }
                    current.append(glyph);
                }
            }
            if (!current.isEmpty()) lines.add(current.toString()); return lines.isEmpty() ? List.of("") : lines;
        }
        private void show(String value, float size, boolean isBold) throws IOException {
            stream.beginText(); stream.setFont(isBold ? bold : regular, size);
            stream.setNonStrokingColor(pageHasBanner && y > HEIGHT - 150
                    ? Palette.rgb(255, 255, 255)
                    : palette.text());
            stream.newLineAtOffset(leftMargin, y); stream.showText(printable(text(value), isBold ? bold : regular)); stream.endText();
        }
        private String printable(String value, PDFont font) throws IOException {
            StringBuilder output = new StringBuilder();
            for (int point : Normalizer.normalize(text(value), Normalizer.Form.NFC).codePoints().toArray()) {
                if (Character.isISOControl(point) || Character.isWhitespace(point)) { output.append(' '); continue; }
                // Joiners and variation selectors have no standalone glyph in these fonts.
                if (Character.getType(point) == Character.FORMAT || point == 0xFE0F) continue;
                String glyph = new String(Character.toChars(point));
                try { font.getStringWidth(glyph); output.append(glyph); }
                catch (IllegalArgumentException unsupported) { output.append('?'); }
            }
            return output.toString();
        }
        private float scaled(float value) { return (float) (value * fontScale); }
        private float spacing(float value) { return (float) (value * densityScale * fontScale); }
        private void ensure(float required) throws IOException { if (y - required < BASE_MARGIN) newPage(); }
        private void newPage() throws IOException {
            pageHasBanner = false;
            if (stream != null) stream.close();
            pageIndex++;
            if (pageIndex < pdf.getNumberOfPages()) {
                stream = new PDPageContentStream(pdf, pdf.getPage(pageIndex), PDPageContentStream.AppendMode.APPEND, true, true);
                y = HEIGHT - BASE_MARGIN;
                return;
            }
            PDPage page = new PDPage(PDRectangle.A4); pdf.addPage(page); stream = new PDPageContentStream(pdf, page);
            stream.setNonStrokingColor(palette.background()); stream.addRect(0, 0, WIDTH, HEIGHT); stream.fill();
            if ("sidebar-left".equals(layout)) { stream.setNonStrokingColor(palette.secondary()); stream.addRect(BASE_MARGIN - 10, BASE_MARGIN, 168, HEIGHT - BASE_MARGIN * 2); stream.fill(); }
            if ("sidebar-right".equals(layout)) { stream.setNonStrokingColor(palette.secondary()); stream.addRect(WIDTH - BASE_MARGIN - 158, BASE_MARGIN, 168, HEIGHT - BASE_MARGIN * 2); stream.fill(); }
            y = HEIGHT - BASE_MARGIN;
        }
        @Override public void close() throws IOException { if (stream != null) stream.close(); }
    }
}
