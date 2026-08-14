package com.recruitment.application.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.application.exception.BusinessException;
import com.recruitment.application.exception.ErrorCode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationRestClientsTest {
    private HttpServer server;
    private final ObjectMapper mapper = new ObjectMapper();

    @AfterEach void stop() { if (server != null) server.stop(0); }

    @Test void allClientsHandleNormalResponses() throws Exception {
        UUID id = UUID.randomUUID();
        start(exchange -> respond(exchange, 200, "{\"data\":{\"id\":\"" + id + "\",\"userId\":\"" + id + "\",\"ownerId\":\"" + id + "\",\"companyId\":\"" + id + "\",\"assetVersion\":1,\"version\":9}}"));
        String base = baseUrl();
        assertThat(new UserClientImpl(base, 500, 500, mapper).getCandidateProfile(id, "Bearer token"))
                .hasValueSatisfying(profile -> assertThat(profile.getVersion()).isEqualTo(9L));
        assertThat(new UserClientImpl(base, 500, 500, mapper).getCurrentResume(id, "Bearer token")).isPresent();
        assertThat(new JobClientImpl(base, 500, 500, mapper).getJobById(id)).isPresent();
        assertThat(new CompanyClientImpl(base, 500, 500, mapper).getCompanyById(id)).isPresent();
    }

    @Test void distinguishesNotFoundClientAuthServerAndTimeoutFailures() throws Exception {
        UUID id = UUID.randomUUID();
        assertStatus(id, 404, null);
        assertStatus(id, 400, ErrorCode.DOWNSTREAM_BAD_REQUEST);
        assertStatus(id, 401, ErrorCode.DOWNSTREAM_UNAUTHORIZED);
        assertStatus(id, 403, ErrorCode.DOWNSTREAM_FORBIDDEN);
        assertStatus(id, 500, ErrorCode.DOWNSTREAM_UNAVAILABLE);

        start(exchange -> { try { Thread.sleep(150); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); } respond(exchange, 200, "{}"); });
        JobClientImpl timeoutClient = new JobClientImpl(baseUrl(), 100, 25, mapper);
        assertThatThrownBy(() -> timeoutClient.getJobById(id))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DOWNSTREAM_TIMEOUT));
    }

    private void assertStatus(UUID id, int status, ErrorCode expected) throws Exception {
        start(exchange -> respond(exchange, status, "{}"));
        JobClientImpl client = new JobClientImpl(baseUrl(), 200, 200, mapper);
        if (expected == null) {
            assertThat(client.getJobById(id)).isEmpty();
        } else {
            assertThatThrownBy(() -> client.getJobById(id)).isInstanceOfSatisfying(BusinessException.class,
                    ex -> assertThat(ex.getErrorCode()).isEqualTo(expected));
        }
        server.stop(0); server = null;
    }

    private void start(Handler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> handler.handle(exchange));
        server.start();
    }

    private String baseUrl() { return "http://127.0.0.1:" + server.getAddress().getPort(); }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @FunctionalInterface interface Handler { void handle(HttpExchange exchange) throws IOException; }
}
