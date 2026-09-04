package com.recruitment.ai.interview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;

/** Four short practice questions; rubric metadata is deterministic, not generated. */
@Component
@RequiredArgsConstructor
public class CompactInterview {
    public static final String ANSWER_CONTRACT = """
            Dàn ý phải hướng dẫn bạn tự trả lời: bắt đầu bằng 'Nêu', 'Mô tả', 'Giải thích' hoặc 'Nếu'.
            Không viết câu trả lời mẫu ở ngôi 'tôi', không tự nhận đã học hay đã làm điều gì.
            Chỉ nhắc đúng dự án đã cung cấp; thiếu dự án thì hỏi có điều kiện.
            Mỗi câu hỏi kết thúc bằng dấu hỏi; mỗi dàn ý kết thúc bằng dấu chấm và không bỏ dở câu.
            """;
    private static final List<String> CATEGORIES = List.of("technicalQuestions", "behavioralQuestions", "hrQuestions", "projectQuestions");
    private static final List<String> SECTIONS = List.of("Kỹ năng", "Kinh nghiệm", "Định hướng", "Dự án");
    private static final List<String> REASONS = List.of("Xác minh hiểu biết kỹ thuật.", "Đánh giá cách xử lý tình huống.", "Tìm hiểu mục tiêu nghề nghiệp.", "Xác minh vai trò thực tế; nếu chưa có dự án, nói rõ chưa có minh chứng.");
    private final ObjectMapper mapper;

    public String grounded(String groundedContext) {
        try {
            JsonNode context = mapper.readTree(groundedContext);
            String skill = firstText(context.path("matchedSkills"));
            if (skill.isBlank()) skill = firstText(context.path("missingSkills"));
            if (skill.isBlank()) skill = "kỹ năng liên quan vị trí";
            String project = firstText(context.path("resumeFacts").path("projects"));
            if (project.length() > 100) project = project.substring(0, 100).strip();
            ObjectNode root = mapper.createObjectNode();
            addQuestion(root, "technicalQuestions", "Bạn giải thích cách áp dụng " + skill + " cho một bài toán nhỏ?",
                    "Giải thích khái niệm, cách áp dụng và cách tự kiểm tra kết quả.");
            addQuestion(root, "behavioralQuestions", "Bạn xử lý thế nào khi một nhiệm vụ chưa đủ thông tin?",
                    "Nêu cách làm rõ yêu cầu, chọn bước ưu tiên và kiểm tra kết quả.");
            addQuestion(root, "hrQuestions", "Mục tiêu nghề nghiệp tiếp theo của bạn là gì?",
                    "Nêu mục tiêu thực tế, kỹ năng cần củng cố và tiêu chí đánh giá tiến bộ.");
            if (project.isBlank()) {
                addQuestion(root, "projectQuestions", "Nếu bạn từng thực hiện dự án, vai trò và phần việc của bạn là gì?",
                        "Nếu có dự án, nêu vai trò và minh chứng thật; nếu chưa có, nói rõ chưa có minh chứng.");
            } else {
                addQuestion(root, "projectQuestions", "Trong dự án " + project + ", vai trò và phần việc của bạn là gì?",
                        "Mô tả vai trò, việc đã làm và kết quả có thể kiểm tra từ dự án này.");
            }
            return mapper.writeValueAsString(root);
        } catch (Exception error) { throw invalid(); }
    }

    private void addQuestion(ObjectNode root, String category, String question, String outline) {
        root.putArray(category).addObject().put("question", question).put("expectedAnswerOutline", outline);
    }

    private String firstText(JsonNode node) {
        if (node == null || node.isNull()) return "";
        if (node.isTextual()) return node.asText("").strip();
        if (node.isArray()) for (JsonNode item : node) { String value = firstText(item); if (!value.isBlank()) return value; }
        if (node.isObject()) {
            for (String field : List.of("name", "title", "description")) {
                String value = firstText(node.path(field)); if (!value.isBlank()) return value;
            }
        }
        return "";
    }

    public String context(String original) {
        try {
            JsonNode source = mapper.readTree(original);
            ObjectNode context = mapper.createObjectNode();
            context.put("jobTitle", source.path("publishedJob").path("title").asText());
            context.put("experienceLevel", source.path("publishedJob").path("experienceLevel").asText());
            for (String key : List.of("matchedSkills", "missingSkills"))
                context.set(key, source.path("authoritativeMatch").path(key));
            ObjectNode facts = context.putObject("resumeFacts");
            for (String key : List.of("technicalSkills", "experience", "projects")) {
                JsonNode value = source.path("resumeFacts").path(key);
                if (!value.isMissingNode()) facts.set(key, bounded(value));
            }
            return mapper.writeValueAsString(context);
        } catch (Exception error) { throw invalid(); }
    }

    private JsonNode bounded(JsonNode value) {
        if (value.isArray()) {
            var result = mapper.createArrayNode();
            for (int i = 0; i < Math.min(value.size(), 12); i++) result.add(bounded(value.get(i)));
            return result;
        }
        if (value.isObject()) {
            var result = mapper.createObjectNode();
            value.fields().forEachRemaining(entry -> result.set(entry.getKey(), bounded(entry.getValue())));
            return result;
        }
        if (value.isTextual() && value.asText().length() > 400)
            return mapper.getNodeFactory().textNode(value.asText().substring(0, 400) + " [trích đoạn]");
        return value;
    }

    public String compose(String generated) {
        try {
            JsonNode source = mapper.readTree(generated);
            ObjectNode result = mapper.createObjectNode();
            for (int i = 0; i < CATEGORIES.size(); i++) {
                String category = CATEGORIES.get(i);
                JsonNode questions = source.path(category);
                if (!questions.isArray() || questions.size() != 1) throw invalid();
                JsonNode question = questions.get(0);
                ObjectNode item = result.putArray(category).addObject();
                for (String field : List.of("question", "expectedAnswerOutline")) {
                    JsonNode value = question.path(field);
                    if (!value.isTextual() || value.asText().isBlank() || value.asText().length() > 240) throw invalid();
                    item.set(field, value);
                }
                item.put("whyInterviewerAsks", REASONS.get(i));
                item.put("relatedResumeSection", SECTIONS.get(i));
                item.put("difficulty", i == 2 ? "EASY" : "MEDIUM");
            }
            return mapper.writeValueAsString(result);
        } catch (BusinessException error) { throw error; }
        catch (Exception error) { throw invalid(); }
    }

    public boolean acceptsGuidance(String generated) {
        return acceptsGuidance(generated, null);
    }

    public boolean acceptsGuidance(String generated, String groundedContext) {
        try {
            JsonNode root = mapper.readTree(compose(generated));
            for (String category : CATEGORIES) {
                String questionText = root.path(category).get(0).path("question").asText().strip();
                String outline = root.path(category).get(0).path("expectedAnswerOutline").asText().strip();
                String normalized = com.recruitment.ai.matching.util.MatchingText.normalize(outline);
                if (!questionText.endsWith("?") || normalized.matches(".*\\btoi\\b.*") || !outline.matches("(?s).*[.!?]$")) return false;
                if (!(normalized.startsWith("neu ") || normalized.startsWith("mo ta ")
                        || normalized.startsWith("giai thich ") || normalized.startsWith("hay ")
                        || normalized.startsWith("trinh bay ") || normalized.startsWith("lien he "))) return false;
            }
            if (groundedContext != null && !groundedContext.isBlank() && !groundedAgainstContext(root, groundedContext)) return false;
            return true;
        } catch (Exception ignored) { return false; }
    }

    private boolean groundedAgainstContext(JsonNode output, String groundedContext) throws Exception {
        JsonNode context = mapper.readTree(groundedContext);
        String technical = com.recruitment.ai.matching.util.MatchingText.normalize(
                output.path("technicalQuestions").get(0).path("question").asText());
        java.util.List<String> roleTerms = new java.util.ArrayList<>();
        collectTerms(context.path("matchedSkills"), roleTerms);
        collectTerms(context.path("missingSkills"), roleTerms);
        if (!roleTerms.isEmpty() && roleTerms.stream().noneMatch(term -> technical.contains(term))) return false;

        JsonNode projects = context.path("resumeFacts").path("projects");
        String projectText = com.recruitment.ai.matching.util.MatchingText.normalize(
                output.path("projectQuestions").get(0).path("question").asText());
        java.util.List<String> projectTerms = new java.util.ArrayList<>();
        collectTerms(projects, projectTerms);
        if (projectTerms.isEmpty()) return projectText.contains("neu") || projectText.contains("chua co");
        return projectTerms.stream().anyMatch(term -> groundedProjectReference(projectText, term));
    }

    private boolean groundedProjectReference(String question, String project) {
        if (question.contains(project)) return true;
        Set<String> ignored = Set.of("du", "an", "ung", "dung", "he", "thong", "bang", "va", "voi",
                "the", "project", "application", "implemented", "chuc", "nang", "phan", "lam");
        long overlap = java.util.Arrays.stream(project.split("\\s+"))
                .filter(token -> token.length() >= 3 && !ignored.contains(token))
                .distinct()
                .filter(question::contains)
                .limit(2)
                .count();
        // A concise question may shorten a long project fact. Requiring two
        // distinctive source tokens prevents an unrelated project from passing
        // on generic words such as "dự án" or "ứng dụng".
        return overlap >= 2;
    }

    private void collectTerms(JsonNode node, java.util.List<String> target) {
        if (node == null || node.isNull()) return;
        if (node.isTextual()) {
            String normalized = com.recruitment.ai.matching.util.MatchingText.normalize(node.asText());
            if (normalized.length() >= 3) target.add(normalized);
        } else if (node.isContainerNode()) node.elements().forEachRemaining(child -> collectTerms(child, target));
    }
    private BusinessException invalid() { return new BusinessException(ErrorCode.INTERVIEW_INVALID); }
}
