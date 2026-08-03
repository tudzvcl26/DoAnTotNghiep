package com.recruitment.ai.matching.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.recruitment.ai.config.ServiceClientProperties;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.matching.model.JobSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RestJobGateway implements JobGateway {

    private final RestClient recruitmentClient;
    private final RestClient companyClient;

    public RestJobGateway(ServiceClientProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.recruitmentClient = RestClient.builder().baseUrl(properties.getRecruitmentServiceUrl())
                .requestFactory(requestFactory).build();
        this.companyClient = RestClient.builder().baseUrl(properties.getCompanyServiceUrl())
                .requestFactory(requestFactory).build();
    }

    @Override
    public JobSnapshot getJob(UUID jobId, String accessToken) {
        try {
            JsonNode envelope = recruitmentClient.get().uri("/api/v1/jobs/{id}", jobId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve().body(JsonNode.class);
            JsonNode job = envelope == null ? null : envelope.path("data");
            if (job == null || job.isMissingNode() || job.isNull() || !job.hasNonNull("companyId")) {
                throw new BusinessException(ErrorCode.MATCH_JOB_NOT_FOUND);
            }
            UUID companyId = UUID.fromString(job.path("companyId").asText());
            JsonNode company = companyClient.get().uri("/api/v1/companies/{id}", companyId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve().body(JsonNode.class);
            if (company == null || !company.hasNonNull("ownerId")) {
                throw new BusinessException(ErrorCode.MATCH_JOB_NOT_FOUND);
            }
            return new JobSnapshot(
                    jobId, text(job, "title"), text(job, "description"), text(job, "requirements"),
                    text(job, "responsibilities"), text(job, "experienceLevel"), text(job, "status"),
                    job.path("active").asBoolean(false), companyId, UUID.fromString(company.path("ownerId").asText())
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new BusinessException(ErrorCode.MATCH_JOB_NOT_FOUND);
        } catch (RestClientException | IllegalArgumentException exception) {
            log.error("Matching job lookup failed jobId={} type={}", jobId, exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.MATCH_UPSTREAM_UNAVAILABLE);
        }
    }

    @Override
    public List<JobSnapshot> getPublishedJobs(String accessToken) {
        try {
            List<JobSnapshot> jobs = new ArrayList<>();
            int page = 0;
            boolean hasNext;
            do {
                int currentPage = page;
                JsonNode envelope = recruitmentClient.get()
                        .uri(builder -> builder.path("/api/v1/jobs").queryParam("page", currentPage)
                                .queryParam("size", 100).build())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .retrieve().body(JsonNode.class);
                JsonNode data = envelope == null ? null : envelope.path("data");
                JsonNode content = data == null ? null : data.path("content");
                if (content == null || !content.isArray()) {
                    throw new BusinessException(ErrorCode.MATCH_UPSTREAM_UNAVAILABLE);
                }
                for (JsonNode summary : content) {
                    UUID jobId = UUID.fromString(summary.path("id").asText());
                    JobSnapshot job = getJob(jobId, accessToken);
                    if (job.active() && "PUBLISHED".equalsIgnoreCase(job.status())) jobs.add(job);
                }
                hasNext = data.path("hasNext").asBoolean(false);
                page++;
            } while (hasNext && page < 100);
            return jobs;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException | IllegalArgumentException exception) {
            log.error("Published job listing failed type={}", exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.MATCH_UPSTREAM_UNAVAILABLE);
        }
    }

    private String text(JsonNode node, String field) {
        return node.path(field).isNull() || node.path(field).isMissingNode() ? null : node.path(field).asText();
    }
}
