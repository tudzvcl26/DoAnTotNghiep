package com.recruitment.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component @Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String LEGACY_HEADER = "X-Correlation-Id";
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        String incoming = request.getHeader(REQUEST_ID_HEADER);
        if (!isSafe(incoming)) incoming = request.getHeader(LEGACY_HEADER);
        String id = isSafe(incoming) ? incoming.trim() : UUID.randomUUID().toString();
        request.setAttribute("requestId", id);
        response.setHeader(REQUEST_ID_HEADER, id);
        response.setHeader(LEGACY_HEADER, id);
        MDC.put("correlationId", id);
        try {
            chain.doFilter(request, response);
        } finally {
            log.info("http_request method={} path={} status={} durationMs={} requestId={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(),
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt), id);
            MDC.remove("correlationId");
        }
    }

    private boolean isSafe(String value) {
        return StringUtils.hasText(value) && value.trim().matches("[A-Za-z0-9._:-]{1,128}");
    }
}
