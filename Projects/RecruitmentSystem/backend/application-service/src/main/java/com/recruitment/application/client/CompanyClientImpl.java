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
public class CompanyClientImpl implements CompanyClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public CompanyClientImpl(
            @Value("${services.company-service.url:http://localhost:8083}") String companyServiceUrl,
            ObjectMapper objectMapper
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(companyServiceUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<CompanyClientDto> getCompanyById(UUID companyId) {
        try {
            String responseStr = restClient.get()
                    .uri("/api/v1/companies/{companyId}", companyId)
                    .retrieve()
                    .body(String.class);

            if (responseStr == null || responseStr.isBlank()) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(responseStr);
            JsonNode data = root.has("data") ? root.get("data") : root;

            if (data == null || data.isNull()) {
                return Optional.empty();
            }

            CompanyClientDto dto = CompanyClientDto.builder()
                    .id(data.has("id") && !data.get("id").isNull() ? UUID.fromString(data.get("id").asText()) : null)
                    .ownerId(data.has("ownerId") && !data.get("ownerId").isNull() ? UUID.fromString(data.get("ownerId").asText()) : null)
                    .name(data.has("name") && !data.get("name").isNull() ? data.get("name").asText() : null)
                    .build();

            return Optional.of(dto);
        } catch (Exception e) {
            log.error("Failed to retrieve company details for companyId {}: {}", companyId, e.getMessage());
            return Optional.empty();
        }
    }

}
