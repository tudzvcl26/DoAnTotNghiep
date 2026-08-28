package com.recruitment.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.config.OpenAiProperties;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationRequest;
import com.recruitment.ai.provider.llm.StructuredGenerationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiStructuredGenerationProvider implements StructuredGenerationProvider {

    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public ProviderDescriptor descriptor() {
        return new ProviderDescriptor("openai", "OPENAI_COMPATIBLE_HTTP", properties.isConfigured());
    }

    @Override
    public StructuredGenerationResult generate(StructuredGenerationRequest request) {
        if (!properties.isConfigured()) {
            throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(properties.getConnectTimeout())
                    .build();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", request.model());
            payload.put("temperature", 0);
            payload.put("messages", List.of(
                            Map.of("role", "system", "content", request.systemPrompt()),
                            Map.of("role", "user", "content", request.userPrompt())
                    ));
            payload.put("response_format", Map.of("type", "json_object"));
            if (request.maxOutputTokens() > 0) {
                payload.put("max_completion_tokens", request.maxOutputTokens());
            }
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(endpoint())
                    .timeout(properties.getReadTimeout())
                    .header("Content-Type", "application/json")
                    .header("X-Request-Id", request.correlationId())
                    .header("X-Correlation-Id", request.correlationId())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));
            if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + properties.getApiKey());
            }
            HttpRequest httpRequest = requestBuilder.build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404 && response.body() != null
                    && response.body().toLowerCase().contains("model")) {
                throw new BusinessException(ErrorCode.PROVIDER_MODEL_UNAVAILABLE);
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("OpenAI-compatible provider returned HTTP {} correlationId={}",
                        response.statusCode(), request.correlationId());
                throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new BusinessException(ErrorCode.PROVIDER_EMPTY_RESPONSE);
            }
            return new StructuredGenerationResult(
                    "openai",
                    root.path("model").asText(request.model()),
                    content,
                    root.path("usage").path("prompt_tokens").asLong(0),
                    root.path("usage").path("completion_tokens").asLong(0)
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (HttpTimeoutException exception) {
            throw new BusinessException(ErrorCode.PROVIDER_TIMEOUT);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
        } catch (Exception exception) {
            log.error("OpenAI-compatible provider call failed type={} correlationId={}",
                    exception.getClass().getSimpleName(), request.correlationId());
            throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
        }
    }

    private URI endpoint() {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.PROVIDER_UNAVAILABLE);
        }
        return URI.create(baseUrl.replaceAll("/+$", "") + "/chat/completions");
    }
}
