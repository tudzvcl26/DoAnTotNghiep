package com.recruitment.gateway.filter;

import com.recruitment.gateway.exception.GatewayResponseWriter;
import com.recruitment.gateway.security.JwtTokenValidator;
import com.recruitment.gateway.security.PublicEndpointPolicy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private final PublicEndpointPolicy publicEndpointPolicy;
    private final JwtTokenValidator tokenValidator;
    private final GatewayResponseWriter responseWriter;

    public JwtAuthenticationFilter(PublicEndpointPolicy publicEndpointPolicy,
                                   JwtTokenValidator tokenValidator,
                                   GatewayResponseWriter responseWriter) {
        this.publicEndpointPolicy = publicEndpointPolicy;
        this.tokenValidator = tokenValidator;
        this.responseWriter = responseWriter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (publicEndpointPolicy.isPublic(exchange.getRequest().getMethod(), path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)
                || authorization.length() == BEARER_PREFIX.length()) {
            return unauthorized(exchange);
        }
        try {
            tokenValidator.validateAccessToken(authorization.substring(BEARER_PREFIX.length()));
            return chain.filter(exchange);
        } catch (RuntimeException invalidToken) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        return responseWriter.write(exchange, HttpStatus.UNAUTHORIZED,
                "GATEWAY_UNAUTHORIZED", "Cần access token hợp lệ để truy cập.");
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
