package com.recruitment.ai.service.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recruitment.ai.config.MatchingProperties;
import com.recruitment.ai.matching.util.MatchingText;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ResumeAnalysisJsonValidator {

    private static final Map<String, Pattern> FACT_SECTION_HEADERS = factSectionHeaders();
    private static final Pattern ANY_SECTION_HEADER = Pattern.compile(
            "(?iu)^(?:học vấn|education|kinh nghiệm(?: làm việc)?|work experience|experience|dự án(?: học tập)?|(?:course )?projects?|"
                    + "ngoại ngữ|languages?|chứng chỉ|certificates?|thành tích|giải thưởng|achievements?|awards?|"
                    + "kỹ năng|skills?|technical skills?|soft skills?|tóm tắt|summary|mục tiêu(?: nghề nghiệp)?|objective)\\s*:?[ \\t]*(.*)$");

    private static final List<String> SCALAR_FIELDS = List.of(
            "fullName", "email", "phone", "location", "linkedIn", "portfolio", "summary"
    );
    private static final List<String> ARRAY_FIELDS = List.of(
            "education", "experience", "projects", "skills", "technicalSkills", "softSkills",
            "languages", "certificates", "achievements", "keywords"
    );

    private final ObjectMapper objectMapper;
    private final MatchingProperties matchingProperties;

    public JsonNode parseAndValidate(String output, String source) {
        ObjectNode facts = (ObjectNode) parseAndValidate(output);
        // Copy an explicit source name/header, not a guessed reconstruction of a truncated name.
        String proposed = facts.path("fullName").asText("").strip();
        List<String> lines = source == null ? List.of() : source.lines().map(String::strip).filter(s -> !s.isBlank()).toList();
        for (int index = 0; index < Math.min(lines.size(), 8); index++) {
            String line = lines.get(index);
            var named = java.util.regex.Pattern.compile("(?iu)^(?:họ và tên|full name|name)\\s*:\\s*(.+)$").matcher(line);
            if (named.matches()) { facts.put("fullName", named.group(1).strip()); break; }
            if (index == 0 && proposed.split("\\s+").length >= 2 && line.startsWith(proposed + " ")
                    && line.matches("[\\p{L}\\p{M}]+(?: [\\p{L}\\p{M}]+){1,5}")) {
                facts.put("fullName", line); break;
            }
        }
        // Contact details are identifiers: a plausible invented URL is never evidence.
        if (source != null) {
            // Preserve an explicitly labelled employment duration even when the
            // model only returns dates/tasks. Do not infer duration from dates.
            ArrayNode experience = (ArrayNode) facts.path("experience");
            for (String line : lines) {
                if (line.matches("(?iu)^(?:kinh nghiệm|experience)\\s*:\\s*.*\\b\\d+(?:[.,]\\d+)?\\s*(?:năm|years?)\\b.*")
                        && !MatchingText.isExplicitAbsence(line)) {
                    String detail = line.substring(line.indexOf(':') + 1).strip();
                    boolean present = java.util.stream.StreamSupport.stream(experience.spliterator(), false)
                            .anyMatch(item -> MatchingText.normalize(item.toString()).contains(MatchingText.normalize(detail)));
                    if (!present) experience.add(detail);
                }
            }
            Map<String, List<String>> sourceFacts = extractLabelledFacts(lines);
            for (String field : List.of("education", "experience", "projects", "languages", "certificates", "achievements")) {
                groundFactualArray(facts, field, source, sourceFacts);
            }
            for (String field : List.of("email", "phone", "linkedIn", "portfolio", "location")) {
                String value = facts.path(field).asText("");
                boolean supported = scalarSupported(field, value, source, lines);
                if (!value.isBlank() && !supported) facts.putNull(field);
            }
            for (String field : List.of("skills", "technicalSkills", "softSkills", "keywords")) {
                ArrayNode supported = objectMapper.createArrayNode();
                facts.path(field).forEach(item -> {
                    String name = skillName(item);
                    String bare = name.replaceAll("\\s*\\([^)]*\\)", "").strip();
                    boolean negativeItem = isNegativeSkillStatement(name);
                    boolean negativeSource = lines.stream().anyMatch(line -> isNegativeSkillStatement(line) && MatchingText.contains(line, bare));
                    boolean positiveSource = lines.stream().anyMatch(line -> !isNegativeSkillStatement(line) && MatchingText.contains(line, bare));
                    if (!negativeItem && positiveSource && !(negativeSource && !positiveSource)) supported.add(item.deepCopy());
                });
                facts.set(field, supported);
            }
        }
        return facts;
    }

    private boolean scalarSupported(String field, String value, String source, List<String> lines) {
        if (value.isBlank()) return false;
        if (field.equals("phone")) {
            String digits = value.replaceAll("\\D", "");
            return !digits.isBlank() && source.replaceAll("\\D", "").contains(digits);
        }
        if (field.equals("location")) {
            // A school, employer or date fragment can be copied verbatim by a
            // model and still not be a location. Require an explicit contact
            // label so substring equality cannot turn those facts into an
            // invented address.
            Pattern labelledLocation = Pattern.compile(
                    "(?iu)^(?:địa chỉ|địa điểm|nơi ở|location|address)\\s*:\\s*(.+)$");
            return lines.stream().map(labelledLocation::matcher)
                    .anyMatch(matcher -> matcher.matches()
                            && MatchingText.normalize(matcher.group(1))
                            .equals(MatchingText.normalize(value)));
        }
        return source.toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT));
    }

    private boolean isNegativeSkillStatement(String text) {
        String normalized = MatchingText.normalize(text);
        return normalized.matches(".*\\b(?:khong|chua|no|without|never)\\b.*")
                || normalized.contains("not experienced") || normalized.contains("not used");
    }

    private void groundFactualArray(ObjectNode facts, String field, String source,
                                    Map<String, List<String>> sourceFacts) {
        ArrayNode grounded = objectMapper.createArrayNode();
        Set<String> seen = new LinkedHashSet<>();
        sourceFacts.getOrDefault(field, List.of()).forEach(value -> addUnique(grounded, seen, value));
        // An explicit source section is authoritative, including an explicitly
        // empty/negative section. One section block is one fact, so bullet points
        // belonging to a job cannot become separate experience entries.
        if (sourceFacts.containsKey(field)) {
            facts.set(field, grounded);
            return;
        }
        // Languages, education, certificates and achievements need an explicit
        // source section. The document language, a date, or a technology name is
        // not evidence for one of these facts.
        if (Set.of("education", "languages", "certificates", "achievements").contains(field)) {
            facts.set(field, grounded);
            return;
        }
        String eligibleSource = sourceWithoutOtherFactSections(source, field);
        facts.path(field).forEach(item -> {
            if (nodeSupported(item, eligibleSource)) {
                String key = factKey(item.toString());
                if (seen.add(key)) grounded.add(item.deepCopy());
            }
        });
        facts.set(field, grounded);
    }

    private Map<String, List<String>> extractLabelledFacts(List<String> lines) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        String activeField = null;
        StringBuilder activeFact = null;
        for (String line : lines) {
            String matchedField = null;
            String initialValue = "";
            for (var entry : FACT_SECTION_HEADERS.entrySet()) {
                var matcher = entry.getValue().matcher(line);
                if (matcher.matches()) {
                    matchedField = entry.getKey();
                    initialValue = matcher.group(1).strip();
                    break;
                }
            }
            if (matchedField != null) {
                appendFact(result, activeField, activeFact);
                activeField = matchedField;
                activeFact = new StringBuilder();
                result.computeIfAbsent(activeField, ignored -> new java.util.ArrayList<>());
                if (!initialValue.isBlank()) {
                    activeFact.append(initialValue);
                    appendFact(result, activeField, activeFact);
                    activeField = null;
                    activeFact = null;
                }
                continue;
            }
            if (ANY_SECTION_HEADER.matcher(line).matches()) {
                appendFact(result, activeField, activeFact);
                activeField = null;
                activeFact = null;
                continue;
            }
            if (activeFact != null) {
                String content = line.replaceFirst("^[\\s•*\\-–—]+", "").strip();
                if (!content.isBlank()) {
                    if (!activeFact.isEmpty()) activeFact.append("; ");
                    activeFact.append(content);
                }
            }
        }
        appendFact(result, activeField, activeFact);
        return result;
    }

    private void appendFact(Map<String, List<String>> target, String field, StringBuilder value) {
        if (field == null || value == null) return;
        String fact = value.toString().strip();
        if (!fact.isBlank() && !MatchingText.isExplicitAbsence(fact)) target.get(field).add(fact);
    }

    private String sourceWithoutOtherFactSections(String source, String requestedField) {
        List<String> kept = new java.util.ArrayList<>();
        String activeField = null;
        for (String line : source.lines().toList()) {
            String nextField = null;
            for (var entry : FACT_SECTION_HEADERS.entrySet()) {
                if (entry.getValue().matcher(line.strip()).matches()) { nextField = entry.getKey(); break; }
            }
            if (nextField != null) activeField = nextField;
            else if (ANY_SECTION_HEADER.matcher(line.strip()).matches()) activeField = "other";
            if (activeField == null || activeField.equals(requestedField)) kept.add(line);
        }
        return String.join("\n", kept);
    }

    private static Map<String, Pattern> factSectionHeaders() {
        Map<String, Pattern> patterns = new LinkedHashMap<>();
        patterns.put("education", Pattern.compile("(?iu)^(?:học vấn|education)\\s*:?[ \\t]*(.*)$"));
        patterns.put("experience", Pattern.compile("(?iu)^(?:kinh nghiệm(?: làm việc)?|work experience|experience)\\s*:?[ \\t]*(.*)$"));
        patterns.put("projects", Pattern.compile("(?iu)^(?:dự án(?: học tập)?|(?:course )?projects?)\\s*:?[ \\t]*(.*)$"));
        patterns.put("languages", Pattern.compile("(?iu)^(?:ngoại ngữ|languages?)\\s*:?[ \\t]*(.*)$"));
        patterns.put("certificates", Pattern.compile("(?iu)^(?:chứng chỉ|certificates?)\\s*:?[ \\t]*(.*)$"));
        patterns.put("achievements", Pattern.compile("(?iu)^(?:thành tích|giải thưởng|achievements?|awards?)\\s*:?[ \\t]*(.*)$"));
        return Map.copyOf(patterns);
    }

    private boolean claimSupported(String claim, String source) {
        String normalizedClaim = MatchingText.normalize(claim);
        String normalizedSource = MatchingText.normalize(source);
        if (normalizedClaim.isBlank() || MatchingText.isExplicitAbsence(claim)) return false;
        if (normalizedSource.contains(normalizedClaim)) return true;
        List<String> evidenceTokens = Arrays.stream(normalizedClaim.split("\\s+"))
                .filter(token -> token.length() >= 3)
                .filter(token -> !Set.of("the", "and", "with", "for", "cua", "voi", "trong", "bang", "nam").contains(token))
                .toList();
        return !evidenceTokens.isEmpty() && evidenceTokens.stream().allMatch(token ->
                Arrays.asList(normalizedSource.split("\\s+")).contains(token));
    }

    private boolean nodeSupported(JsonNode node, String source) {
        if (node.isTextual()) return claimSupported(node.asText(), source);
        if (node.isObject() || node.isArray()) {
            List<JsonNode> leaves = new java.util.ArrayList<>();
            collectTextLeaves(node, leaves);
            return !leaves.isEmpty() && leaves.stream().allMatch(value -> claimSupported(value.asText(), source));
        }
        return false;
    }

    private void collectTextLeaves(JsonNode node, List<JsonNode> target) {
        if (node.isTextual() && !node.asText().isBlank()) target.add(node);
        else if (node.isContainerNode()) node.elements().forEachRemaining(child -> collectTextLeaves(child, target));
    }

    private void addUnique(ArrayNode target, Set<String> seen, String value) {
        String key = factKey(value);
        if (!key.isBlank() && seen.add(key)) target.add(value);
    }

    private String factKey(String value) {
        return MatchingText.normalize(value).toLowerCase(Locale.ROOT).replaceAll("[.]+$", "");
    }

    public JsonNode parseAndValidate(String structuredOutput) {
        try {
            JsonNode root = objectMapper.readTree(structuredOutput);
            if (root == null || !root.isObject()) {
                throw invalid();
            }
            for (String field : SCALAR_FIELDS) {
                JsonNode value = root.get(field);
                if (value == null || !(value.isTextual() || value.isNull())) {
                    throw invalid();
                }
            }
            for (String field : ARRAY_FIELDS) {
                JsonNode value = root.get(field);
                if (value == null || !value.isArray()) {
                    throw invalid();
                }
            }
            ObjectNode cleaned = (ObjectNode) clean(root);
            ArrayNode certificates = objectMapper.createArrayNode();
            cleaned.path("certificates").forEach(item -> {
                if (MatchingText.hasCertificateEvidence(item)) certificates.add(item.deepCopy());
            });
            cleaned.set("certificates", certificates);
            // A known technical skill remains technical when the model places
            // it only in the general skills list. This never adds unseen skills.
            Set<String> catalog = MatchingText.normalizedSkills(matchingProperties.getTechnicalSkillCatalog());
            ArrayNode technical = (ArrayNode) cleaned.path("technicalSkills");
            Set<String> seen = new LinkedHashSet<>();
            technical.forEach(item -> seen.add(MatchingText.canonicalSkill(skillName(item))));
            cleaned.path("skills").forEach(item -> {
                String key = MatchingText.canonicalSkill(skillName(item));
                if (catalog.contains(key) && seen.add(key)) technical.add(item.deepCopy());
            });
            return cleaned;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalid();
        }
    }

    private BusinessException invalid() {
        return new BusinessException(ErrorCode.RESUME_ANALYSIS_INVALID);
    }

    private String skillName(JsonNode value) { return value.isTextual() ? value.asText() : value.path("name").asText(""); }

    private JsonNode clean(JsonNode node) {
        if (node.isTextual() && (Set.of("", "null", "undefined", "[object object]").contains(node.asText().trim().toLowerCase(java.util.Locale.ROOT))
                || MatchingText.normalize(node.asText()).matches("^(?:(?:not provided|not specified|not available)(?: in (?:cv|resume))?|(?:khong co|chua co) (?:thong tin|lien ket linkedin|trang web ca nhan)(?: trong cv)?)$"))) return NullNode.instance;
        if (node.isArray()) {
            ArrayNode result = objectMapper.createArrayNode();
            Set<String> seen = new LinkedHashSet<>();
            node.forEach(item -> { JsonNode value = clean(item); if (!value.isNull() && seen.add(value.toString().toLowerCase(java.util.Locale.ROOT))) result.add(value); });
            return result;
        }
        if (node.isObject()) {
            ObjectNode result = objectMapper.createObjectNode();
            node.fields().forEachRemaining(entry -> result.set(entry.getKey(), clean(entry.getValue())));
            return result;
        }
        return node;
    }
}
