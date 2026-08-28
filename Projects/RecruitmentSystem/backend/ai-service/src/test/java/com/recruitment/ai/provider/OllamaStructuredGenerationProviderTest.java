package com.recruitment.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.config.OllamaProperties;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.provider.llm.StructuredGenerationRequest;
import com.recruitment.ai.provider.llm.StructuredGenerationResult;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OllamaStructuredGenerationProviderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void usesChatApiAndMapsStructuredResponseAndUsage() throws Exception {
        AtomicReference<JsonNode> receivedPayload = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/chat", exchange -> {
            receivedPayload.set(objectMapper.readTree(exchange.getRequestBody()));
            respond(exchange, """
                    {"model":"qwen2.5:3b","message":{"role":"assistant","content":"{\\"status\\":\\"ok\\"}"},
                     "done":true,"prompt_eval_count":34,"eval_count":12}
                    """);
        });
        server.start();

        OllamaStructuredGenerationProvider provider = provider();
        StructuredGenerationResult result = provider.generate(new StructuredGenerationRequest(
                "database-model", "system prompt", "user prompt",
                "{\"type\":\"object\",\"required\":[\"summary\",\"recommendations\"]}", "correlation-test", 384
        ));

        JsonNode payload = receivedPayload.get();
        assertThat(payload.path("model").asText()).isEqualTo("qwen2.5:3b");
        assertThat(payload.path("stream").asBoolean()).isFalse();
        assertThat(payload.path("format").path("type").asText()).isEqualTo("object");
        assertThat(payload.path("format").path("properties").path("summary").path("type").asText())
                .isEqualTo("string");
        assertThat(payload.path("format").path("properties").path("recommendations").path("type").asText())
                .isEqualTo("array");
        assertThat(payload.path("messages")).hasSize(2);
        assertThat(payload.path("options").path("temperature").asDouble()).isEqualTo(0.2);
        assertThat(payload.path("options").path("top_p").asDouble()).isEqualTo(0.9);
        assertThat(payload.path("options").path("num_predict").asInt()).isEqualTo(384);
        assertThat(result.providerName()).isEqualTo("ollama");
        assertThat(result.model()).isEqualTo("qwen2.5:3b");
        assertThat(result.structuredOutput()).contains("status");
        assertThat(result.inputTokens()).isEqualTo(34);
        assertThat(result.outputTokens()).isEqualTo(12);
    }

    @Test
    void reportsOnlineOnlyWhenConfiguredModelIsInstalled() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/tags", exchange -> respond(exchange, """
                {"models":[{"name":"Qwen2.5:3b"}]}
                """));
        server.start();

        OllamaStructuredGenerationProvider.OllamaAvailability availability = provider().availability();

        assertThat(availability.reachable()).isTrue();
        assertThat(availability.modelAvailable()).isTrue();
        assertThat(availability.online()).isTrue();
    }

    @Test
    void distinguishesMissingModelEmptyResponseAndTimeout() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/chat", exchange -> respond(exchange, 404,
                "{\"error\":\"model 'missing' not found\"}"));
        server.start();
        assertError(provider(Duration.ofSeconds(2)), ErrorCode.PROVIDER_MODEL_UNAVAILABLE);
        server.stop(0);

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/chat", exchange -> respond(exchange,
                "{\"model\":\"qwen2.5:3b\",\"message\":{\"content\":\"\"}}"));
        server.start();
        assertError(provider(Duration.ofSeconds(2)), ErrorCode.PROVIDER_EMPTY_RESPONSE);
        server.stop(0);

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/chat", exchange -> {
            try {
                Thread.sleep(300);
                respond(exchange, "{\"message\":{\"content\":\"{}\"}}");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
        server.start();
        assertError(provider(Duration.ofMillis(100)), ErrorCode.PROVIDER_TIMEOUT);
    }

    private OllamaStructuredGenerationProvider provider() {
        return provider(Duration.ofSeconds(2));
    }

    private OllamaStructuredGenerationProvider provider(Duration timeout) {
        OllamaProperties properties = new OllamaProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setModel("qwen2.5:3b");
        properties.setTemperature(0.2);
        properties.setTopP(0.9);
        properties.setTimeout(timeout);
        return new OllamaStructuredGenerationProvider(properties, objectMapper, HttpClient.newHttpClient());
    }

    private void assertError(OllamaStructuredGenerationProvider provider, ErrorCode code) {
        assertThatThrownBy(() -> provider.generate(new StructuredGenerationRequest(
                "model", "system", "user", "{\"type\":\"object\"}", "correlation-test")))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(code);
    }

    private void respond(com.sun.net.httpserver.HttpExchange exchange, String response) throws java.io.IOException {
        respond(exchange, 200, response);
    }

    private void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String response) throws java.io.IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
