package com.recruitment.ai.recommendation;

import com.fasterxml.jackson.databind.JsonNode;
import com.recruitment.ai.config.ServiceClientProperties;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class CandidateConsentGateway {
    private final RestClient client;

    public CandidateConsentGateway(ServiceClientProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        this.client = RestClient.builder().baseUrl(properties.getUserServiceUrl()).requestFactory(factory).build();
    }

    public boolean hasConsent(UUID userId, String accessToken) {
        try {
            JsonNode envelope = client.get().uri("/api/v1/users/{id}/candidate-preference", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve().body(JsonNode.class);
            return envelope != null && envelope.path("data").path("recommendationConsent").asBoolean(false);
        } catch (HttpClientErrorException.NotFound exception) {
            return false;
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.MATCH_UPSTREAM_UNAVAILABLE);
        }
    }
}
