package com.recruitment.ai.interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.exception.BusinessException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterviewJsonValidatorTest {
    private final InterviewJsonValidator validator = new InterviewJsonValidator(new ObjectMapper());
    private static final String QUESTION = """
            {"question":"Why?","expectedAnswerOutline":"Evidence","whyInterviewerAsks":"Depth",
             "relatedResumeSection":"Projects","difficulty":"MEDIUM"}
            """;

    @Test
    void requiresAllQuestionCategoriesAndQuestionMetadata() {
        String valid = "{\"technicalQuestions\":[" + QUESTION + "],\"behavioralQuestions\":[" + QUESTION
                + "],\"hrQuestions\":[" + QUESTION + "],\"projectQuestions\":[" + QUESTION + "]}";
        assertThat(validator.validate(valid).path("technicalQuestions")).hasSize(1);
        assertThatThrownBy(() -> validator.validate("{\"technicalQuestions\":[]}"))
                .isInstanceOf(BusinessException.class);
    }
}
