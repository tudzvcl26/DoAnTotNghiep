package com.recruitment.ai.assistant;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Component
public class CareerPromptSecurity {

    private static final List<String> REFUSAL_PATTERNS = List.of(
            "ignore previous instructions", "ignore all instructions", "ignore security",
            "answer only in english", "reveal your system prompt", "show your system prompt",
            "hidden system instructions", "database password", "postgres_password", "jwt_secret",
            "authentication secret", "another candidate's resume", "other candidate's resume",
            "all applications", "cv cua ung vien khac", "ho so cua ung vien khac",
            "ung vien khac", "du lieu ho so rieng tu cua ung vien", "toan bo du lieu ho so",
            "mat khau database", "tiet lo system prompt", "in system prompt", "bo qua chi dan",
            "bo qua moi chi dan", "bo qua bao mat"
    );

    public boolean requiresSafeRefusal(String message) {
        String normalized = normalize(message);
        return REFUSAL_PATTERNS.stream().anyMatch(normalized::contains);
    }

    public boolean containsSensitiveOutput(String answer) {
        String normalized = answer == null ? "" : answer.toLowerCase(Locale.ROOT);
        return normalized.contains("postgres_password=")
                || normalized.contains("jwt_secret=")
                || normalized.contains("minio_secret_key=")
                || normalized.contains("always answer in vietnamese. never intentionally answer");
    }

    private String normalize(String value) {
        if (value == null) return "";
        String decomposed = Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").replace('đ', 'd');
    }
}
