package com.recruitment.recruitmentservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CompanyClientImpl implements CompanyClient {

    private final RestClient restClient;

    public CompanyClientImpl(
            @Value("${services.company-service.url:http://localhost:8083}") String companyServiceUrl,
            @Value("${services.company-service.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${services.company-service.read-timeout-ms}") int readTimeoutMs
    ) {
        this.restClient = RestClientFactory.create(companyServiceUrl, connectTimeoutMs, readTimeoutMs);
    }

    @Override
    public Optional<CompanyClientDto> getCompanyById(UUID companyId) {
        return DownstreamClientSupport.execute(() -> {
            CompanyClientDto response = restClient.get()
                    .uri("/api/v1/companies/{companyId}", companyId)
                    .retrieve()
                    .body(CompanyClientDto.class);

            return response;
        });
    }

    @Override
    public List<CompanyClientDto> getCompaniesByOwner(UUID ownerId, String accessToken) {
        return DownstreamClientSupport.execute(() -> restClient.get()
                .uri("/api/v1/companies/owner/{ownerId}", ownerId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CompanyClientDto>>() { }))
                .orElseGet(List::of);
    }

}
