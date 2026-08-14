package com.recruitment.gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PublicEndpointPolicy {

    private static final Set<String> PUBLIC_POST_ENDPOINTS = Set.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/resend-verification");

    public boolean isPublic(HttpMethod method, String path) {
        if (HttpMethod.OPTIONS.equals(method)
                || "/api/v1/health".equals(path)
                || "/actuator/health".equals(path)
                || path.startsWith("/actuator/health/")) {
            return true;
        }
        if (HttpMethod.POST.equals(method) && PUBLIC_POST_ENDPOINTS.contains(path)) {
            return true;
        }
        if (!HttpMethod.GET.equals(method)) {
            return false;
        }
        if ("/api/v1/auth/dev/action-token".equals(path)) {
            return true;
        }
        if (isJobApplicationsPath(path)) {
            return false;
        }
        return isWithin(path, "/api/v1/companies")
                || isWithin(path, "/api/v1/jobs")
                || isWithin(path, "/api/v1/job-categories")
                || isWithin(path, "/api/v1/skills")
                || isWithin(path, "/api/v1/benefits");
    }

    private boolean isJobApplicationsPath(String path) {
        String prefix = "/api/v1/jobs/";
        if (!path.startsWith(prefix)) {
            return false;
        }
        String remainder = path.substring(prefix.length());
        return remainder.endsWith("/applications") && remainder.indexOf('/') == remainder.lastIndexOf('/');
    }

    private boolean isWithin(String path, String basePath) {
        return path.equals(basePath) || path.startsWith(basePath + "/");
    }
}
