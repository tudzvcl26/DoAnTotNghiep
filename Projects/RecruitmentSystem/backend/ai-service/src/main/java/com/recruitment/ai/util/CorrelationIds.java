package com.recruitment.ai.util;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

public final class CorrelationIds {

    public static final String HEADER = "X-Request-Id";
    public static final String LEGACY_HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";
    public static final String REQUEST_ATTRIBUTE = CorrelationIds.class.getName() + ".value";

    private CorrelationIds() {
    }

    public static String current(HttpServletRequest request) {
        Object requestValue = request.getAttribute(REQUEST_ATTRIBUTE);
        if (requestValue != null) {
            return requestValue.toString();
        }
        return MDC.get(MDC_KEY);
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }

}
