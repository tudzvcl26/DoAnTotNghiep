package com.recruitment.ai.observability;

import com.recruitment.ai.util.CorrelationIds;
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

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        String incoming = request.getHeader(CorrelationIds.HEADER);
        if (!isSafe(incoming)) incoming = request.getHeader(CorrelationIds.LEGACY_HEADER);
        String correlationId = isSafe(incoming) ? incoming.trim() : UUID.randomUUID().toString();
        request.setAttribute(CorrelationIds.REQUEST_ATTRIBUTE, correlationId);
        response.setHeader(CorrelationIds.HEADER, correlationId);
        response.setHeader(CorrelationIds.LEGACY_HEADER, correlationId);
        MDC.put(CorrelationIds.MDC_KEY, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("http_request method={} path={} status={} durationMs={} requestId={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(),
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt), correlationId);
            MDC.remove(CorrelationIds.MDC_KEY);
        }
    }

    private boolean isSafe(String value) {
        return StringUtils.hasText(value) && value.trim().matches("[A-Za-z0-9._:-]{1,128}");
    }

}
