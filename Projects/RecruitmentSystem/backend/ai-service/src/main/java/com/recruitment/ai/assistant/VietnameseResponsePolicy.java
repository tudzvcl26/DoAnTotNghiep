package com.recruitment.ai.assistant;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

@Component
public class VietnameseResponsePolicy {

    private static final Set<String> VIETNAMESE_WORDS = Set.of(
            "ban", "minh", "toi", "nen", "can", "khong", "cua", "va", "de", "trong", "voi",
            "duoc", "hay", "la", "khi", "cho", "nhung", "nhu", "theo", "se", "da", "co",
            "mot", "cac", "viec", "ho-so", "ky-nang"
    );
    private static final Set<String> ENGLISH_WORDS = Set.of(
            "the", "you", "your", "should", "based", "improve", "this", "that", "because", "with",
            "from", "have", "has", "need", "recommend", "focus", "career", "resume", "skills", "experience",
            "candidate", "their", "working", "projects", "creating", "additionally", "taking", "courses",
            "knowledge", "latest", "technologies", "finally", "mentorship", "community", "engineers"
    );

    public boolean isVietnameseNaturalLanguage(String answer) {
        if (answer == null || answer.isBlank() || containsCjk(answer)) return false;
        String lower = answer.toLowerCase(Locale.ROOT);
        boolean hasVietnameseCharacters = lower.matches(".*[ăâđêôơưáàảãạấầẩẫậắằẳẵặéèẻẽẹếềểễệíìỉĩịóòỏõọốồổỗộớờởỡợúùủũụứừửữựýỳỷỹỵ].*");
        String plain = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd');
        int vietnamese = 0;
        int english = 0;
        for (String token : plain.split("[^a-z0-9+#.-]+")) {
            if (VIETNAMESE_WORDS.contains(token)) vietnamese++;
            if (ENGLISH_WORDS.contains(token)) english++;
        }
        if (english >= 4 && vietnamese < 2) return false;
        return (hasVietnameseCharacters && (vietnamese > 0 || english == 0))
                || vietnamese >= 2
                || (vietnamese > 0 && english == 0);
    }

    private boolean containsCjk(String value) {
        return value.codePoints().anyMatch(codePoint ->
                (codePoint >= 0x3040 && codePoint <= 0x30ff)
                        || (codePoint >= 0x3400 && codePoint <= 0x9fff)
                        || (codePoint >= 0xac00 && codePoint <= 0xd7af));
    }
}
