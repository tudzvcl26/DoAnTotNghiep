package com.recruitment.ai.explanation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CompactExplanationTest {
    @Test void removesDuplicatedContextAndKeepsScoringFactsAuthoritative() throws Exception {
        var mapper = new ObjectMapper(); var service = new CompactExplanation(mapper);
        String context = """
          {"authoritativeMatch":{"overallScore":92,"matchedSkills":["Java"],"missingSkills":["Redis"],
          "strengths":["Có kỹ năng Java"],"weaknesses":["Chưa có minh chứng Redis"],"scoreBreakdown":[],
          "matchedExperience":"5 năm","matchedEducation":"Chưa có yêu cầu"},
          "resumeFacts":{"summary":"Private detailed CV"},"publishedJob":{"title":"Java Backend"}}
          """;
        assertThat(service.context(context)).contains("92", "Java", "Redis", "5 năm").doesNotContain("Private detailed CV");
        String output = service.compose("""
          {"overallEvaluation":"Bạn phù hợp về Java và cần bổ sung minh chứng Redis.",
          "careerSuggestions":["Tăng kinh nghiệm kiểm thử phần mềm."],"resumeImprovementChecklist":[],"learningRoadmap":[],
          "strengths":["Bịa AWS"],"missingTechnologies":[],"score":100}
          """, context);
        new ExplanationJsonValidator(mapper).validate(output);
        assertThat(output).contains("Có kỹ năng Java", "Redis", "KẾT QUẢ QUY TẮC")
                .doesNotContain("Bịa AWS", "kiểm thử phần mềm", "Study", "\"score\"");
    }
}
