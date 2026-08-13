package com.recruitment.application.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Component
public class CompanyClientImpl implements CompanyClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public CompanyClientImpl(
            @Value("${services.company-service.url:http://localhost:8083}") String companyServiceUrl,
            @Value("${services.company-service.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${services.company-service.read-timeout-ms}") int readTimeoutMs,
            ObjectMapper objectMapper
    ) {
        this.restClient = RestClientFactory.create(companyServiceUrl, connectTimeoutMs, readTimeoutMs);
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<CompanyClientDto> getCompanyById(UUID companyId) {
        return DownstreamClientSupport.execute(() -> {
            String responseStr = restClient.get()
                    .uri("/api/v1/companies/{companyId}", companyId)
                    .retrieve()
                    .body(String.class);

            if (responseStr == null || responseStr.isBlank()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(responseStr);
            JsonNode data = root.has("data") ? root.get("data") : root;

            if (data == null || data.isNull()) {
                return null;
            }

            CompanyClientDto dto = CompanyClientDto.builder()
                    .id(data.has("id") && !data.get("id").isNull() ? UUID.fromString(data.get("id").asText()) : null)
                    .ownerId(data.has("ownerId") && !data.get("ownerId").isNull() ? UUID.fromString(data.get("ownerId").asText()) : null)
                    .name(data.has("name") && !data.get("name").isNull() ? data.get("name").asText() : null)
                    .build();

            return dto;
        });
    }

    @Override
    public List<CompanyClientDto> getCompaniesByOwnerId(UUID ownerId, String bearerToken) {
        return DownstreamClientSupport.execute(() -> {
            RestClient.RequestHeadersSpec<?> spec = restClient.get()
                    .uri("/api/v1/companies/owner/{ownerId}", ownerId);
            if (bearerToken != null && !bearerToken.isBlank()) {
                spec.header("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }
            String responseStr = spec.retrieve().body(String.class);
            if (responseStr == null || responseStr.isBlank()) {
                return List.<CompanyClientDto>of();
            }
            JsonNode root = objectMapper.readTree(responseStr);
            JsonNode data = root.has("data") ? root.get("data") : root;
            if (data == null || !data.isArray()) {
                return List.<CompanyClientDto>of();
            }
            return java.util.stream.StreamSupport.stream(data.spliterator(), false)
                    .map(node -> CompanyClientDto.builder()
                            .id(node.hasNonNull("id") ? UUID.fromString(node.get("id").asText()) : null)
                            .ownerId(node.hasNonNull("ownerId") ? UUID.fromString(node.get("ownerId").asText()) : null)
                            .name(node.hasNonNull("name") ? node.get("name").asText() : null)
                            .build())
                    .filter(company -> company.getId() != null)
                    .toList();
        }).orElse(List.<CompanyClientDto>of());
    }

}
