package com.recruitment.gateway;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.recruitment.gateway.filter.RequestLoggingFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GatewayIntegrationTest {

    private static final String RAW_SECRET = "0123456789abcdef0123456789abcdef";
    private static final String JWT_SECRET = Base64.getEncoder().encodeToString(RAW_SECRET.getBytes());
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
    private static final Map<String, String> CORRELATION_IDS = new ConcurrentHashMap<>();
    private static final Map<String, String> AUTHORIZATIONS = new ConcurrentHashMap<>();
    private static final Map<String, DisposableServer> UPSTREAMS = new ConcurrentHashMap<>();

    static {
        for (String service : List.of("auth", "user", "company", "recruitment", "application", "notification", "ai")) {
            UPSTREAMS.put(service, startUpstream(service));
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RouteLocator routeLocator;

    @DynamicPropertySource
    static void gatewayProperties(DynamicPropertyRegistry registry) {
        UPSTREAMS.forEach((service, server) -> registry.add(
                "gateway.services." + service,
                () -> "http://127.0.0.1:" + server.port()));
        registry.add("gateway.jwt.secret", () -> JWT_SECRET);
        registry.add("spring.cloud.gateway.server.webflux.httpclient.connect-timeout", () -> "200");
        registry.add("spring.cloud.gateway.server.webflux.httpclient.response-timeout", () -> "200ms");
        registry.add("gateway.cors.allowed-origins", () -> "http://localhost:5173,http://localhost:3000");
    }

    @AfterAll
    static void stopUpstreams() {
        UPSTREAMS.values().forEach(server -> server.disposeNow(Duration.ofSeconds(2)));
    }

    @BeforeAll
    static void clearCapturedData() {
        CORRELATION_IDS.clear();
        AUTHORIZATIONS.clear();
    }

    @Test
    void aiRouteBudgetExceedsTwoBoundedProviderCallsWithoutChangingOtherRoutes() {
        var routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();
        assertThat(routes.stream().filter(route -> route.getId().equals("ai-service")).findFirst().orElseThrow()
                .getMetadata().get("response-timeout")).isEqualTo(390_000);
        assertThat(routes.stream().filter(route -> !route.getId().equals("ai-service")))
                .allMatch(route -> !route.getMetadata().containsKey("response-timeout"));
    }

    @Test
    @Order(1)
    void applicationStartsAndDeclaresAllExpectedRoutes() {
        List<String> routeIds = routeLocator.getRoutes().map(Route::getId).collectList().block();
        assertThat(routeIds).containsExactlyInAnyOrder(
                "application-job-applications", "auth-service", "admin-users", "user-service", "company-service",
                "admin-companies", "application-service", "admin-applications", "recruitment-service",
                "notification-service", "ai-service");
    }

    @Test
    @Order(2)
    void healthEndpointsArePublic() {
        webTestClient.get().uri("/api/v1/health").exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Correlation-ID")
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.service").isEqualTo("api-gateway")
                .jsonPath("$.version").isEqualTo("1.0.0");

        webTestClient.get().uri("/actuator/health").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    @Order(3)
    void publicAuthAndReadRoutesReachTheirServices() {
        expectService(webTestClient.post().uri("/api/v1/auth/login"), "auth", false);
        expectService(webTestClient.get().uri("/api/v1/companies/search"), "company", false);
        expectService(webTestClient.get().uri("/api/v1/jobs/search"), "recruitment", false);
        expectService(webTestClient.get().uri("/api/v1/job-categories"), "recruitment", false);
        expectService(webTestClient.get().uri("/api/v1/skills"), "recruitment", false);
        expectService(webTestClient.get().uri("/api/v1/benefits"), "recruitment", false);
    }

    @Test
    @Order(4)
    void missingInvalidTamperedExpiredAndRefreshTokensReturn401() {
        webTestClient.get().uri("/api/v1/users/me").exchange().expectStatus().isUnauthorized();
        webTestClient.get().uri("/not-an-api-route").exchange().expectStatus().isUnauthorized();
        assertUnauthorized("not-a-jwt");
        assertUnauthorized(validToken() + "tampered");
        assertUnauthorized(token(Instant.now().minusSeconds(120), "access"));
        assertUnauthorized(token(Instant.now().plusSeconds(120), "refresh"));
    }

    @Test
    @Order(5)
    void validJwtRoutesEveryProtectedServiceAndPreservesAuthorization() {
        String token = validToken();
        expectService(authorizedGet("/api/v1/users/me", token), "user", true);
        expectService(authorizedGet("/api/v1/profiles/me", token), "user", true);
        expectService(authorizedGet("/api/v1/cvs", token), "user", true);
        expectService(authorizedPost("/api/v1/companies", token), "company", true);
        expectService(authorizedPost("/api/v1/jobs", token), "recruitment", true);
        expectService(authorizedGet("/api/v1/applications/me", token), "application", true);
        expectService(authorizedGet("/api/v1/notifications", token), "notification", true);
        expectService(authorizedPost("/api/v1/ai/analyze", token), "ai", true);
        expectService(authorizedGet("/api/v1/admin/users", token), "auth", true);
        expectService(authorizedGet("/api/v1/admin/companies", token), "company", true);
        expectService(authorizedGet("/api/v1/admin/applications", token), "application", true);
        expectService(authorizedGet("/api/v1/admin/jobs", token), "recruitment", true);
        assertThat(AUTHORIZATIONS.get("/api/v1/users/me")).isEqualTo("Bearer " + token);
    }

    @Test
    @Order(6)
    void jobApplicationsUsesApplicationServiceInsteadOfRecruitmentService() {
        expectService(authorizedGet("/api/v1/jobs/job-123/applications", validToken()), "application", true);
    }

    @Test
    @Order(7)
    void corsPreflightPassesWithConfiguredOriginAndHeaders() {
        webTestClient.options().uri("/api/v1/users/me")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,X-Request-Id,X-Correlation-ID")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173")
                .expectHeader().value(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, value -> assertThat(value).contains("GET"))
                .expectHeader().value(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        value -> assertThat(value.toLowerCase()).contains("authorization")
                                .contains("x-request-id").contains("x-correlation-id"));
    }

    @Test
    @Order(8)
    void corsActualResponseDeduplicatesUpstreamHeaders() {
        webTestClient.get().uri("/api/v1/jobs/cors-actual")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().values(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        values -> assertThat(values).containsExactly("http://localhost:5173"));
    }

    @Test
    @Order(9)
    void correlationIdIsGeneratedOrPreservedAndForwarded() {
        String generatedPath = "/api/v1/jobs/generated-correlation";
        webTestClient.get().uri(generatedPath).exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Request-Id")
                .expectHeader().exists("X-Correlation-ID");
        assertThat(CORRELATION_IDS.get(generatedPath)).isNotBlank();

        String suppliedPath = "/api/v1/jobs/existing-correlation";
        webTestClient.get().uri(suppliedPath)
                .header("X-Request-Id", "client-request-123")
                .header("X-Correlation-ID", "legacy-correlation-ignored")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Request-Id", "client-request-123")
                .expectHeader().valueEquals("X-Correlation-ID", "client-request-123");
        assertThat(CORRELATION_IDS.get(suppliedPath)).isEqualTo("client-request-123");

        String invalidPath = "/api/v1/jobs/invalid-correlation";
        webTestClient.get().uri(invalidPath)
                .header("X-Request-Id", "invalid request id with spaces")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value("X-Request-Id", value -> {
                    assertThat(value).isNotEqualTo("invalid request id with spaces");
                    assertThat(value).matches("[A-Za-z0-9._:-]{1,128}");
                });
        assertThat(CORRELATION_IDS.get(invalidPath)).matches("[A-Za-z0-9._:-]{1,128}");
    }

    @Test
    @Order(10)
    void backendStatusesAndBodiesArePreserved() {
        for (int status : List.of(400, 401, 403, 404, 409, 500)) {
            authorizedGet("/api/v1/users/status?status=" + status, validToken())
                    .exchange()
                    .expectStatus().isEqualTo(status)
                    .expectBody().jsonPath("$.service").isEqualTo("user")
                    .jsonPath("$.upstreamStatus").isEqualTo(status);
        }
    }

    @Test
    @Order(11)
    void gatewayTimeoutReturnsStable504Envelope() {
        authorizedGet("/api/v1/users/slow?delay=600", validToken())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
                .expectBody()
                .jsonPath("$.status").isEqualTo(504)
                .jsonPath("$.code").isEqualTo("GATEWAY_TIMEOUT")
                .jsonPath("$.message").isEqualTo("Dịch vụ phía sau không phản hồi kịp thời.")
                .jsonPath("$.traceId").isNotEmpty();
    }

    @Test
    @Order(12)
    void requestLogsNeverContainAuthorizationToken() {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.list = new java.util.concurrent.CopyOnWriteArrayList<>();
        appender.start();
        logger.addAppender(appender);
        String token = validToken();
        try {
            authorizedGet("/api/v1/users/log-check", token).exchange().expectStatus().isOk();
            // doFinally logs after response completion on another thread. Wait
            // for this request, not an unrelated earlier request's log event.
            org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() ->
                    assertThat(appender.list).anyMatch(event -> event.getFormattedMessage().contains("path=/api/v1/users/log-check ")));
            List<String> messages = appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
            assertThat(messages).isNotEmpty();
            assertThat(messages).allMatch(message -> !message.contains(token)
                    && !message.toLowerCase().contains("authorization"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    @Order(13)
    void unavailableBackendReturnsStable503Envelope() {
        DisposableServer ai = UPSTREAMS.get("ai");
        ai.disposeNow(Duration.ofSeconds(2));

        authorizedPost("/api/v1/ai/unavailable", validToken())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.code").isEqualTo("GATEWAY_UPSTREAM_UNAVAILABLE")
                .jsonPath("$.message").isEqualTo("Dịch vụ phía sau hiện không khả dụng.");
    }

    @Test
    void unauthorizedResponsesExposeCorsHeadersForSessionRecovery() {
        for (String bearer : List.of("", "not-a-jwt", token(Instant.now().minusSeconds(120), "access"))) {
            webTestClient.get().uri("/api/v1/cvs")
                    .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer)
                    .exchange().expectStatus().isUnauthorized()
                    .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173")
                    .expectBody().jsonPath("$.code").isEqualTo("GATEWAY_UNAUTHORIZED");
        }
    }

    @Test
    void disallowedOriginCannotReadProtectedResponses() {
        webTestClient.get().uri("/api/v1/cvs")
                .header(HttpHeaders.ORIGIN, "https://untrusted.example")
                .headers(headers -> headers.setBearerAuth(validToken()))
                .exchange().expectStatus().isForbidden()
                .expectHeader().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }

    private static DisposableServer startUpstream(String service) {
        return HttpServer.create()
                .host("127.0.0.1")
                .port(0)
                .handle((request, response) -> {
                    String path = request.uri().split("\\?", 2)[0];
                    CORRELATION_IDS.put(path, request.requestHeaders().get("X-Correlation-ID"));
                    String authorization = request.requestHeaders().get(HttpHeaders.AUTHORIZATION);
                    if (authorization != null) {
                        AUTHORIZATIONS.put(path, authorization);
                    }
                    Map<String, String> query = parseQuery(request.uri());
                    int status = Integer.parseInt(query.getOrDefault("status", "200"));
                    long delay = Long.parseLong(query.getOrDefault("delay", "0"));
                    String body = "{\"service\":\"" + service + "\",\"upstreamStatus\":" + status + "}";
                    String origin = request.requestHeaders().get(HttpHeaders.ORIGIN);
                    if (origin != null) {
                        response.header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                    }
                    response.status(status)
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .header(HttpHeaders.CONNECTION, "close");
                    return response.sendString(Mono.delay(Duration.ofMillis(delay)).map(ignored -> body));
                })
                .bindNow();
    }

    private static Map<String, String> parseQuery(String uri) {
        int queryStart = uri.indexOf('?');
        if (queryStart < 0 || queryStart == uri.length() - 1) {
            return Map.of();
        }
        Map<String, String> values = new ConcurrentHashMap<>();
        for (String pair : uri.substring(queryStart + 1).split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                values.put(parts[0], parts[1]);
            }
        }
        return values;
    }

    private WebTestClient.RequestHeadersSpec<?> authorizedGet(String uri, String token) {
        return webTestClient.get().uri(uri).headers(headers -> headers.setBearerAuth(token));
    }

    private WebTestClient.RequestHeadersSpec<?> authorizedPost(String uri, String token) {
        return webTestClient.post().uri(uri).headers(headers -> headers.setBearerAuth(token));
    }

    private void expectService(WebTestClient.RequestHeadersSpec<?> request, String service, boolean protectedRoute) {
        if (protectedRoute) {
            assertThat(request).isNotNull();
        }
        request.exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.service").isEqualTo(service);
    }

    private void assertUnauthorized(String token) {
        authorizedGet("/api/v1/users/me", token).exchange()
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.code").isEqualTo("GATEWAY_UNAUTHORIZED")
                .jsonPath("$.message").isEqualTo("Cần access token hợp lệ để truy cập.");
    }

    private static String validToken() {
        return token(Instant.now().plusSeconds(300), "access");
    }

    private static String token(Instant expiration, String tokenType) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("candidate@example.com")
                .claim("userId", "00000000-0000-0000-0000-000000000001")
                .claim("roles", List.of("CANDIDATE"))
                .claim("token_type", tokenType)
                .issuedAt(Date.from(now.minusSeconds(1)))
                .expiration(Date.from(expiration))
                .signWith(SIGNING_KEY)
                .compact();
    }
}
