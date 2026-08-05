package com.recruitment.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recruitment.ai.config.OllamaProperties;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationRequest;
import com.recruitment.ai.provider.llm.StructuredGenerationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class OllamaStructuredGenerationProvider implements StructuredGenerationProvider {

    private static final Set<String> STRING_ARRAY_FIELDS = Set.of(
            "strengths", "weaknesses", "highScoreReasons", "lowScoreReasons", "missingTechnologies",
            "careerSuggestions", "resumeImprovementChecklist", "skillRecommendations", "projectRecommendations",
            "certificationSuggestions", "keywordImprovements", "experienceImprovements", "educationImprovements",
            "learningRoadmap", "recommendedTechnologies", "recommendedCertifications", "portfolioImprovements",
            "recommendations", "risks", "nextSteps", "education", "experience", "projects", "skills",
            "technicalSkills", "softSkills", "languages", "certificates", "achievements", "keywords"
    );
    private static final Set<String> QUESTION_ARRAY_FIELDS = Set.of(
            "technicalQuestions", "behavioralQuestions", "hrQuestions", "projectQuestions"
    );

    private final OllamaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OllamaStructuredGenerationProvider(
            OllamaProperties properties,
            ObjectMapper objectMapper,
            @Qualifier("ollamaHttpClient") HttpClient httpClient
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public ProviderDescriptor descriptor() {
        return new ProviderDescriptor("ollama", "OLLAMA_CHAT_HTTP", properties.isConfigured());
    }

    @Override
    public StructuredGenerationResult generate(StructuredGenerationRequest request) {
        if (!properties.isConfigured()) {
            throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
        }

        try {
            JsonNode responseFormat = ollamaResponseFormat(request.outputSchema());
            Map<String, Object> payload = Map.of(
                    "model", properties.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", request.systemPrompt()),
                            Map.of("role", "user", "content", request.userPrompt())
                    ),
                    "stream", false,
                    "format", responseFormat,
                    "options", Map.of(
                            "temperature", properties.getTemperature(),
                            "top_p", properties.getTopP()
                    )
            );
            HttpRequest httpRequest = HttpRequest.newBuilder(endpoint("/api/chat"))
                    .timeout(properties.getTimeout())
                    .header("Content-Type", "application/json")
                    .header("X-Correlation-Id", request.correlationId())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("Ollama returned HTTP {} correlationId={}", response.statusCode(), request.correlationId());
                throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
            }
            return new StructuredGenerationResult(
                    "ollama",
                    root.path("model").asText(properties.getModel()),
                    content,
                    root.path("prompt_eval_count").asLong(0),
                    root.path("eval_count").asLong(0)
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
        } catch (Exception exception) {
            log.error("Ollama provider call failed correlationId={}", request.correlationId(), exception);
            throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
        }
    }

    public OllamaAvailability availability() {
        if (!properties.isConfigured()) {
            return new OllamaAvailability(false, false);
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(endpoint("/api/tags"))
                    .timeout(properties.getTimeout())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new OllamaAvailability(false, false);
            }
            JsonNode models = objectMapper.readTree(response.body()).path("models");
            boolean modelAvailable = models.isArray() && models.valueStream()
                    .map(model -> model.path("name").asText())
                    .anyMatch(properties.getModel()::equalsIgnoreCase);
            return new OllamaAvailability(true, modelAvailable);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new OllamaAvailability(false, false);
        } catch (Exception exception) {
            log.debug("Ollama availability check failed: {}", exception.getClass().getSimpleName());
            return new OllamaAvailability(false, false);
        }
    }

    private URI endpoint(String path) {
        return URI.create(properties.getBaseUrl().replaceAll("/+$", "") + path);
    }

    private JsonNode ollamaResponseFormat(String outputSchema) throws Exception {
        ObjectNode schema = (ObjectNode) objectMapper.readTree(outputSchema);
        if (schema.has("properties")) {
            return schema;
        }

        ObjectNode propertiesNode = schema.putObject("properties");
        JsonNode required = schema.path("required");
        if (required.isArray()) {
            for (JsonNode fieldNode : required) {
                String field = fieldNode.asText();
                if ("gapExplanations".equals(field)) {
                    propertiesNode.set(field, gapExplanationsSchema());
                } else if (QUESTION_ARRAY_FIELDS.contains(field)) {
                    propertiesNode.set(field, interviewQuestionsSchema());
                } else if (STRING_ARRAY_FIELDS.contains(field)) {
                    propertiesNode.set(field, stringArraySchema());
                } else {
                    propertiesNode.set(field, stringSchema());
                }
            }
        }
        schema.put("additionalProperties", false);
        return schema;
    }

    private ObjectNode stringArraySchema() {
        ObjectNode array = objectMapper.createObjectNode().put("type", "array");
        array.set("items", stringSchema());
        return array;
    }

    private ObjectNode gapExplanationsSchema() {
        ObjectNode array = objectMapper.createObjectNode().put("type", "array");
        ObjectNode item = array.putObject("items").put("type", "object");
        ObjectNode fields = item.putObject("properties");
        fields.set("area", stringSchema());
        fields.set("gap", stringSchema());
        fields.set("explanation", stringSchema());
        fields.putObject("priority").put("type", "string")
                .putArray("enum").add("HIGH").add("MEDIUM").add("LOW");
        item.set("required", requiredArray("area", "gap", "explanation", "priority"));
        item.put("additionalProperties", false);
        return array;
    }

    private ObjectNode interviewQuestionsSchema() {
        ObjectNode array = objectMapper.createObjectNode().put("type", "array");
        array.put("minItems", 1).put("maxItems", 1);
        ObjectNode item = array.putObject("items").put("type", "object");
        ObjectNode fields = item.putObject("properties");
        fields.set("question", stringSchema());
        fields.set("expectedAnswerOutline", stringSchema());
        fields.set("whyInterviewerAsks", stringSchema());
        fields.set("relatedResumeSection", stringSchema());
        fields.putObject("difficulty").put("type", "string")
                .putArray("enum").add("EASY").add("MEDIUM").add("HARD");
        item.set("required", requiredArray("question", "expectedAnswerOutline", "whyInterviewerAsks",
                "relatedResumeSection", "difficulty"));
        item.put("additionalProperties", false);
        return array;
    }

    private ObjectNode stringSchema() {
        return objectMapper.createObjectNode().put("type", "string").put("minLength", 1);
    }

    private ArrayNode requiredArray(String... fields) {
        ArrayNode required = objectMapper.createArrayNode();
        for (String field : fields) required.add(field);
        return required;
    }

    public record OllamaAvailability(boolean reachable, boolean modelAvailable) {

        public boolean online() {
            return reachable && modelAvailable;
        }
    }
}
