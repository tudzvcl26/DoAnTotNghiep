package com.recruitment.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements WebFilter, Ordered {

    public static final String HEADER_NAME = "X-Correlation-ID";
    public static final String ATTRIBUTE_NAME = "gateway.correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String supplied = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);
        String correlationId = isSafe(supplied) ? supplied : UUID.randomUUID().toString();

        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set(HEADER_NAME, correlationId))
                .build();
        exchange.getResponse().getHeaders().set(HEADER_NAME, correlationId);
        ServerWebExchange enriched = exchange.mutate().request(request).build();
        enriched.getAttributes().put(ATTRIBUTE_NAME, correlationId);
        return chain.filter(enriched);
    }

    private boolean isSafe(String value) {
        if (value == null || value.isBlank() || value.length() > 128) {
            return false;
        }
        return value.chars().allMatch(character -> character >= 33 && character <= 126);
    }

    @Override
    public int getOrder() {
        return -300;
    }
}
