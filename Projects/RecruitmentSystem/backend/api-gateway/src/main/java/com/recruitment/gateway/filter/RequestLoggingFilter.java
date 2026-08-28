package com.recruitment.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Component
public class RequestLoggingFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startedAt = System.nanoTime();
        return chain.filter(exchange).doFinally(signal -> {
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            log.info("gateway_request method={} path={} status={} durationMs={} requestId={} route={}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getPath().value(),
                    status == null ? 0 : status.value(),
                    durationMs,
                    exchange.getAttributeOrDefault(CorrelationIdFilter.ATTRIBUTE_NAME, "unknown"),
                    route == null ? "gateway" : route.getId());
        });
    }

    @Override
    public int getOrder() {
        return -250;
    }
}
