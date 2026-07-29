package com.recruitment.recruitmentservice.client;

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

    public CompanyClientImpl(
            @Value("${services.company-service.url:http://localhost:8083}") String companyServiceUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(companyServiceUrl)
                .build();
    }

    @Override
    public Optional<CompanyClientDto> getCompanyById(UUID companyId) {
        try {
            CompanyClientDto response = restClient.get()
                    .uri("/api/v1/companies/{companyId}", companyId)
                    .retrieve()
                    .body(CompanyClientDto.class);

            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("Failed to retrieve company details for companyId {}: {}", companyId, e.getMessage());
            return Optional.empty();
        }
    }

}
