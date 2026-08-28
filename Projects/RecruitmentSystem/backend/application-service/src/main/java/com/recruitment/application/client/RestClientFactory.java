package com.recruitment.application.client;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.slf4j.MDC;

final class RestClientFactory {

    private RestClientFactory() {
    }

    static RestClient create(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    String requestId = MDC.get("correlationId");
                    if (requestId != null && !requestId.isBlank()) {
                        request.getHeaders().set("X-Request-Id", requestId);
                        request.getHeaders().set("X-Correlation-Id", requestId);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
