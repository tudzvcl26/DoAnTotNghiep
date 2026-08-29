package com.recruitment.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    RouteLocator gatewayRoutes(RouteLocatorBuilder builder, GatewayServiceProperties services) {
        return builder.routes()
                // This more-specific route must precede the general recruitment /jobs route.
                .route("application-job-applications", route -> route
                        .path("/api/v1/jobs/{jobId}/applications")
                        .and().method("GET")
                        .uri(services.application()))
                .route("auth-service", route -> route
                        .path("/api/v1/auth/**")
                        .uri(services.auth()))
                .route("admin-users", route -> route
                        .path("/api/v1/admin/users/**")
                        .uri(services.auth()))
                .route("user-service", route -> route
                        .path("/api/v1/users/**", "/api/v1/profiles/**", "/api/v1/cvs/**")
                        .uri(services.user()))
                .route("company-service", route -> route
                        .path("/api/v1/companies/**")
                        .uri(services.company()))
                .route("admin-companies", route -> route
                        .path("/api/v1/admin/companies/**")
                        .uri(services.company()))
                .route("application-service", route -> route
                        .path("/api/v1/applications/**")
                        .uri(services.application()))
                .route("admin-applications", route -> route
                        .path("/api/v1/admin/applications/**")
                        .uri(services.application()))
                .route("recruitment-service", route -> route
                        .path("/api/v1/jobs/**", "/api/v1/admin/jobs/**", "/api/v1/job-categories/**", "/api/v1/skills/**", "/api/v1/benefits/**")
                        .uri(services.recruitment()))
                .route("notification-service", route -> route
                        .path("/api/v1/notifications/**", "/api/v1/notification-templates/**",
                                "/api/v1/admin/notification-delivery-logs/**")
                        .uri(services.notification()))
                .route("ai-service", route -> route
                        .path("/api/v1/ai/**")
                        .uri(services.ai()))
                .build();
    }
}
