package com.recruitment.gateway;

import com.recruitment.gateway.security.PublicEndpointPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

class PublicEndpointPolicyTest {

    private final PublicEndpointPolicy policy = new PublicEndpointPolicy();

    @Test
    void allowlistIsMethodAwareAndDoesNotExposeNestedBusinessEndpoints() {
        assertThat(policy.isPublic(HttpMethod.POST, "/api/v1/auth/login")).isTrue();
        assertThat(policy.isPublic(HttpMethod.GET, "/api/v1/jobs/search")).isTrue();
        assertThat(policy.isPublic(HttpMethod.GET, "/api/v1/jobs/job-id")).isTrue();
        assertThat(policy.isPublic(HttpMethod.GET, "/api/v1/companies/slug/example-company")).isTrue();
        assertThat(policy.isPublic(HttpMethod.GET, "/api/v1/jobs/company/company-id")).isTrue();

        assertThat(policy.isPublic(HttpMethod.GET, "/api/v1/auth/login")).isFalse();
        assertThat(policy.isPublic(HttpMethod.POST, "/api/v1/jobs")).isFalse();
        assertThat(policy.isPublic(HttpMethod.GET, "/api/v1/jobs/job-id/applications")).isFalse();
        assertThat(policy.isPublic(HttpMethod.GET, "/api/v1/notifications")).isFalse();
    }
}
