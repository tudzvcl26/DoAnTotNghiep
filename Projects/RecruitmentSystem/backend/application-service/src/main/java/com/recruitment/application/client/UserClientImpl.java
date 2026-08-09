package com.recruitment.application.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserClientImpl implements UserClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public UserClientImpl(
            @Value("${services.user-service.url:http://localhost:8082}") String userServiceUrl,
            @Value("${services.user-service.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${services.user-service.read-timeout-ms}") int readTimeoutMs,
            ObjectMapper objectMapper
    ) {
        this.restClient = RestClientFactory.create(userServiceUrl, connectTimeoutMs, readTimeoutMs);
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<UserClientDto> getCandidateProfile(UUID candidateId, String bearerToken) {
        return DownstreamClientSupport.execute(() -> {
            RestClient.RequestHeadersSpec<?> spec = restClient.get().uri("/api/v1/profiles/me");

            if (bearerToken != null && !bearerToken.isBlank()) {
                spec.header("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }

            String responseStr = spec.retrieve().body(String.class);

            if (responseStr == null || responseStr.isBlank()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(responseStr);
            JsonNode data = root.has("data") ? root.get("data") : root;

            if (data == null || data.isNull()) {
                return null;
            }

            UserClientDto dto = UserClientDto.builder()
                    .id(data.has("id") && !data.get("id").isNull() ? UUID.fromString(data.get("id").asText()) : null)
                    .userId(data.has("userId") && !data.get("userId").isNull() ? UUID.fromString(data.get("userId").asText()) : candidateId)
                    .displayName(data.has("displayName") && !data.get("displayName").isNull() ? data.get("displayName").asText() : null)
                    .headline(data.has("headline") && !data.get("headline").isNull() ? data.get("headline").asText() : null)
                    .summary(data.has("summary") && !data.get("summary").isNull() ? data.get("summary").asText() : null)
                    .contactEmail(data.has("contactEmail") && !data.get("contactEmail").isNull() ? data.get("contactEmail").asText() : null)
                    .contactPhone(data.has("contactPhone") && !data.get("contactPhone").isNull() ? data.get("contactPhone").asText() : null)
                    .rawJsonData(data.toString())
                    .build();

            return dto;
        });
    }

    @Override
    public Optional<ResumeClientDto> getCurrentResume(UUID candidateId, String bearerToken) {
        return DownstreamClientSupport.execute(() -> {
            RestClient.RequestHeadersSpec<?> spec = restClient.get()
                    .uri("/api/v1/users/{candidateId}/resumes/current", candidateId);

            if (bearerToken != null && !bearerToken.isBlank()) {
                spec.header("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }

            String responseStr = spec.retrieve().body(String.class);
            if (responseStr == null || responseStr.isBlank()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(responseStr);
            JsonNode data = root.has("data") ? root.get("data") : root;
            if (data == null || data.isNull()) {
                return null;
            }

            return ResumeClientDto.builder()
                    .id(uuid(data, "id"))
                    .storageKey(text(data, "storageKey"))
                    .originalFilename(text(data, "originalFilename"))
                    .contentType(text(data, "contentType"))
                    .sizeBytes(data.hasNonNull("sizeBytes") ? data.get("sizeBytes").asLong() : null)
                    .checksum(text(data, "checksum"))
                    .assetVersion(data.hasNonNull("assetVersion") ? data.get("assetVersion").asLong() : null)
                    .rawJsonData(data.toString())
                    .build();
        });
    }

    private UUID uuid(JsonNode node, String field) {
        return node.hasNonNull(field) ? UUID.fromString(node.get(field).asText()) : null;
    }

    private String text(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

}
