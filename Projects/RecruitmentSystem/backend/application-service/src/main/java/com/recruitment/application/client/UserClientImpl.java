package com.recruitment.application.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class UserClientImpl implements UserClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public UserClientImpl(
            @Value("${services.user-service.url:http://localhost:8082}") String userServiceUrl,
            ObjectMapper objectMapper
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<UserClientDto> getCandidateProfile(UUID candidateId, String bearerToken) {
        try {
            RestClient.RequestHeadersSpec<?> spec = restClient.get().uri("/api/v1/profiles/me");

            if (bearerToken != null && !bearerToken.isBlank()) {
                spec.header("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }

            String responseStr = spec.retrieve().body(String.class);

            if (responseStr == null || responseStr.isBlank()) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(responseStr);
            JsonNode data = root.has("data") ? root.get("data") : root;

            if (data == null || data.isNull()) {
                return Optional.empty();
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

            return Optional.of(dto);
        } catch (Exception e) {
            log.error("Failed to retrieve candidate profile for candidateId {}: {}", candidateId, e.getMessage());
            return Optional.empty();
        }
    }

}
