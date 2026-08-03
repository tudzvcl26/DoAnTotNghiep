package com.recruitment.ai.service.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResumeAnalysisJsonValidator {

    private static final List<String> SCALAR_FIELDS = List.of(
            "fullName", "email", "phone", "location", "linkedIn", "portfolio", "summary"
    );
    private static final List<String> ARRAY_FIELDS = List.of(
            "education", "experience", "projects", "skills", "technicalSkills", "softSkills",
            "languages", "certificates", "achievements", "keywords"
    );

    private final ObjectMapper objectMapper;

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
            return root;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalid();
        }
    }

    private BusinessException invalid() {
        return new BusinessException(ErrorCode.RESUME_ANALYSIS_INVALID);
    }
}
