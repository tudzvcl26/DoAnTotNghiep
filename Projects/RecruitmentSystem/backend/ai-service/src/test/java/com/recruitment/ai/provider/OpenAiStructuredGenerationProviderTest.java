package com.recruitment.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.config.OpenAiProperties;
import com.recruitment.ai.provider.llm.StructuredGenerationRequest;
import com.recruitment.ai.provider.llm.StructuredGenerationResult;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiStructuredGenerationProviderTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void parsesOpenAiCompatibleStructuredResponseAndUsage() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            String response = """
                    {"model":"runtime-model","choices":[{"message":{"content":"{\\"fullName\\":\\"Candidate\\"}"}}],
                     "usage":{"prompt_tokens":21,"completion_tokens":8}}
                    """;
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();

        OpenAiProperties properties = new OpenAiProperties();
        properties.setEnabled(true);
        properties.setApiKey("test-key");
        properties.setApiKeyRequired(true);
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
        properties.setConnectTimeout(Duration.ofSeconds(2));
        properties.setReadTimeout(Duration.ofSeconds(2));
        OpenAiStructuredGenerationProvider provider =
                new OpenAiStructuredGenerationProvider(properties, new ObjectMapper());

        StructuredGenerationResult result = provider.generate(new StructuredGenerationRequest(
                "requested-model", "system", "user", "{}", "correlation-test"
        ));

        assertThat(provider.descriptor().available()).isTrue();
        assertThat(result.model()).isEqualTo("runtime-model");
        assertThat(result.structuredOutput()).contains("Candidate");
        assertThat(result.inputTokens()).isEqualTo(21);
        assertThat(result.outputTokens()).isEqualTo(8);
    }
}
