package com.recruitment.ai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssistantJsonValidatorTest {
    private final AssistantJsonValidator validator = new AssistantJsonValidator(new ObjectMapper());

    @Test
    void acceptsStructuredAssistantResponse() {
        var result = validator.validate("""
                {"summary":"Grounded summary","recommendations":["Practice Java"],"risks":[],"nextSteps":["Review project"]}
                """);
        assertThat(result.path("recommendations")).hasSize(1);
    }

    @Test
    void rejectsBusinessDecisionFields() {
        assertThatThrownBy(() -> validator.validate("""
                {"summary":"Result","recommendations":[],"risks":[],"nextSteps":[],"decision":"APPROVE"}
                """)).isInstanceOf(BusinessException.class);
    }
}
