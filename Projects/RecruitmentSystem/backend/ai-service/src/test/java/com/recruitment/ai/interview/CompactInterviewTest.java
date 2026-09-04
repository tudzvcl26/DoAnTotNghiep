package com.recruitment.ai.interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.exception.BusinessException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CompactInterviewTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final CompactInterview compact = new CompactInterview(mapper);

    @Test
    void excludesContactAndRepeatedScoringButKeepsExplicitFactsAndNulls() throws Exception {
        String result = compact.context("""
            {"publishedJob":{"title":"Java Developer","description":"duplicate job text"},
             "authoritativeMatch":{"matchedSkills":["Java"],"missingSkills":["Redis"],"scoreBreakdown":["duplicate scoring"]},
             "resumeFacts":{"email":"private@example.test","technicalSkills":["Java"],"experience":null,
             "projects":[{"name":"A"},{"name":"B"},{"name":"C"},{"name":"D"}]}}
            """);
        assertThat(result).doesNotContain("private@example.test", "duplicate", "scoreBreakdown");
        assertThat(mapper.readTree(result).path("resumeFacts").path("experience").isNull()).isTrue();
        assertThat(mapper.readTree(result).path("resumeFacts").path("projects").size()).isEqualTo(4);
        assertThat(result).contains("D");
        assertThat(result).contains("Java", "Redis");
    }

    @Test
    void rejectsMissingCategoriesAndUnboundedQuestions() {
        assertThatThrownBy(() -> compact.compose("{}" )).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> compact.compose("{\"technicalQuestions\":[]}" )).isInstanceOf(BusinessException.class);
    }

    @Test void deterministicQuestionsAreCompleteAndGroundedForProjectAndSparseContexts() {
        String context = "{\"matchedSkills\":[\"Java\"],\"missingSkills\":[\"Redis\"],\"resumeFacts\":{\"projects\":[\"ứng dụng quản lý thư viện bằng Java\"]}}";
        String output = compact.grounded(context);
        assertThat(compact.acceptsGuidance(output, context)).isTrue();
        assertThat(output).contains("Java", "ứng dụng quản lý thư viện").doesNotContain("Tôi đã");

        String sparse = "{\"matchedSkills\":[\"Java\"],\"missingSkills\":[],\"resumeFacts\":{\"projects\":[]}}";
        String sparseOutput = compact.grounded(sparse);
        assertThat(compact.acceptsGuidance(sparseOutput, sparse)).isTrue();
        assertThat(sparseOutput).contains("Nếu bạn từng", "chưa có minh chứng");
    }

    @Test void rejectsFirstPersonInventedExperienceAndUnfinishedGuidance() throws Exception {
        var root = mapper.createObjectNode();
        for (String category : java.util.List.of("technicalQuestions", "behavioralQuestions", "hrQuestions", "projectQuestions")) {
            root.putArray(category).addObject().put("question", "Bạn sẽ chuẩn bị như thế nào?")
                    .put("expectedAnswerOutline", "Nếu chưa có minh chứng, nêu rõ và đề xuất bài thực hành.");
        }
        assertThat(compact.acceptsGuidance(root.toString())).isTrue();
        var outline = (com.fasterxml.jackson.databind.node.ObjectNode) root.path("projectQuestions").get(0);
        outline.put("expectedAnswerOutline", "Tôi đã sử dụng RESTful API trong dự án thư viện.");
        assertThat(compact.acceptsGuidance(root.toString())).isFalse();
        outline.put("expectedAnswerOutline", "Nêu cách thực hiện các dự");
        assertThat(compact.acceptsGuidance(root.toString())).isFalse();
    }

    @Test void groundsTechnicalAndProjectQuestionsOrUsesConditionalSparseWording() throws Exception {
        var root = mapper.createObjectNode();
        for (String category : java.util.List.of("technicalQuestions", "behavioralQuestions", "hrQuestions", "projectQuestions")) {
            root.putArray(category).addObject().put("question", "Bạn sẽ chuẩn bị Java như thế nào?")
                    .put("expectedAnswerOutline", "Nêu cách bạn chuẩn bị Java.");
        }
        String grounded = """
                {"jobTitle":"Java Developer","matchedSkills":["Java"],"missingSkills":["Redis"],
                 "resumeFacts":{"projects":[{"name":"Order API"}]}}
                """;
        assertThat(compact.acceptsGuidance(root.toString(), grounded)).isFalse();
        ((com.fasterxml.jackson.databind.node.ObjectNode) root.path("projectQuestions").get(0))
                .put("question", "Bạn đã làm gì trong Order API?")
                .put("expectedAnswerOutline", "Nêu vai trò và phần việc trong Order API.");
        assertThat(compact.acceptsGuidance(root.toString(), grounded)).isTrue();

        String longProject = """
                {"jobTitle":"Java Developer","matchedSkills":["Java"],"missingSkills":[],
                 "resumeFacts":{"projects":["ứng dụng quản lý thư viện bằng Java, làm giao diện và chức năng mượn sách"]}}
                """;
        ((com.fasterxml.jackson.databind.node.ObjectNode) root.path("projectQuestions").get(0))
                .put("question", "Bạn đã xử lý chức năng mượn sách trong dự án thế nào?")
                .put("expectedAnswerOutline", "Nêu vai trò và cách xử lý chức năng mượn sách.");
        assertThat(compact.acceptsGuidance(root.toString(), longProject)).isTrue();
        ((com.fasterxml.jackson.databind.node.ObjectNode) root.path("projectQuestions").get(0))
                .put("question", "Bạn đã làm gì trong dự án bán hàng?");
        assertThat(compact.acceptsGuidance(root.toString(), longProject)).isFalse();

        String sparse = "{\"jobTitle\":\"Java Developer\",\"matchedSkills\":[\"Java\"],\"missingSkills\":[],\"resumeFacts\":{\"projects\":[]}}";
        ((com.fasterxml.jackson.databind.node.ObjectNode) root.path("projectQuestions").get(0))
                .put("question", "Nếu chưa có dự án, bạn sẽ tạo bài thực hành nào?")
                .put("expectedAnswerOutline", "Nếu chưa có minh chứng, nêu kế hoạch tạo API nhỏ.");
        assertThat(compact.acceptsGuidance(root.toString(), sparse)).isTrue();
    }
}
