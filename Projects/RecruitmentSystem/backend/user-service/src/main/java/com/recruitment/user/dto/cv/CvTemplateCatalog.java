package com.recruitment.user.dto.cv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Server-side defaults for profile-created CVs; saved designConfig remains authoritative.
public final class CvTemplateCatalog {
    private CvTemplateCatalog() {}
    public static final String ID_PATTERN = "classic|professional|modern|ats|student|modern-professional|corporate|minimal|clean-grid|sidebar-emerald|sidebar-navy|developer|software-engineer|data-professional|ai-engineer|creative-accent|portfolio|marketing|graduate|intern|fresher|academic|classic-formal|elegant";
    private record Preset(String font, String density, String layout, String theme, String arrangement) {}
    private static final Map<String, Preset> PRESETS = Map.ofEntries(
            Map.entry("classic", new Preset("Arial", "normal", "single", "emerald", "experience")),
            Map.entry("professional", new Preset("Georgia", "compact", "header", "navy", "experience")),
            Map.entry("modern", new Preset("Inter", "comfortable", "header", "teal", "experience")),
            Map.entry("ats", new Preset("Arial", "compact", "single", "gray", "experience")),
            Map.entry("student", new Preset("Inter", "normal", "header", "blue", "education")),
            Map.entry("modern-professional", new Preset("Roboto", "normal", "sidebar-right", "teal", "experience")),
            Map.entry("corporate", new Preset("Arial", "compact", "single", "navy", "experience")),
            Map.entry("minimal", new Preset("Inter", "comfortable", "single", "gray", "experience")),
            Map.entry("clean-grid", new Preset("Open Sans", "normal", "sidebar-right", "blue", "experience")),
            Map.entry("sidebar-emerald", new Preset("Inter", "normal", "sidebar-left", "emerald", "experience")),
            Map.entry("sidebar-navy", new Preset("Open Sans", "compact", "sidebar-left", "navy", "experience")),
            Map.entry("developer", new Preset("Roboto", "normal", "sidebar-left", "teal", "projects")),
            Map.entry("software-engineer", new Preset("Inter", "normal", "single", "blue", "experience")),
            Map.entry("data-professional", new Preset("Open Sans", "compact", "sidebar-right", "purple", "projects")),
            Map.entry("ai-engineer", new Preset("Roboto", "normal", "header", "navy", "projects")),
            Map.entry("creative-accent", new Preset("Inter", "comfortable", "header", "purple", "projects")),
            Map.entry("portfolio", new Preset("Georgia", "comfortable", "sidebar-right", "burgundy", "projects")),
            Map.entry("marketing", new Preset("Open Sans", "normal", "header", "orange", "experience")),
            Map.entry("graduate", new Preset("Roboto", "comfortable", "sidebar-left", "blue", "education")),
            Map.entry("intern", new Preset("Arial", "comfortable", "single", "teal", "education")),
            Map.entry("fresher", new Preset("Open Sans", "normal", "header", "emerald", "education")),
            Map.entry("academic", new Preset("Times New Roman", "comfortable", "single", "navy", "education")),
            Map.entry("classic-formal", new Preset("Times New Roman", "normal", "single", "burgundy", "experience")),
            Map.entry("elegant", new Preset("Georgia", "comfortable", "header", "burgundy", "experience")));
    private static final Map<String, CvDocument.CvThemeConfig> THEMES = Map.ofEntries(
            Map.entry("emerald", new CvDocument.CvThemeConfig("emerald", "#146F54", "#DDF5EA", "#1F2937", "#667085", "#FFFFFF")),
            Map.entry("teal", new CvDocument.CvThemeConfig("teal", "#0F766E", "#CCFBF1", "#16302E", "#5F7471", "#FFFFFF")),
            Map.entry("blue", new CvDocument.CvThemeConfig("blue", "#2563EB", "#DBEAFE", "#172554", "#64748B", "#FFFFFF")),
            Map.entry("navy", new CvDocument.CvThemeConfig("navy", "#173B66", "#E8EEF6", "#172033", "#667085", "#FFFFFF")),
            Map.entry("purple", new CvDocument.CvThemeConfig("purple", "#7C3AED", "#EDE9FE", "#2E1065", "#746B86", "#FFFFFF")),
            Map.entry("burgundy", new CvDocument.CvThemeConfig("burgundy", "#7A1F3D", "#F7E9EE", "#21181B", "#75666B", "#FFFFFF")),
            Map.entry("orange", new CvDocument.CvThemeConfig("orange", "#C2410C", "#FFEDD5", "#431407", "#7C6A62", "#FFFFFF")),
            Map.entry("gray", new CvDocument.CvThemeConfig("gray", "#475467", "#EAECF0", "#1D2939", "#667085", "#FFFFFF")));

    public static CvDocument.CvDesignConfig design(String templateId) {
        Preset preset = PRESETS.getOrDefault(templateId, PRESETS.get("classic"));
        List<String> first = switch (preset.arrangement()) {
            case "education" -> List.of("summary", "education", "projects");
            case "projects" -> List.of("summary", "projects", "experience");
            default -> List.of("summary", "experience");
        };
        var order = new ArrayList<>(first);
        CvDocument.CvDesignConfig.defaults().sectionOrder().stream().filter(id -> !order.contains(id)).forEach(order::add);
        return new CvDocument.CvDesignConfig(preset.font(), 1, THEMES.get(preset.theme()), preset.density(), preset.layout(), List.copyOf(order), Map.of());
    }
}
