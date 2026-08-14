package com.recruitment.ai.matching.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.recruitment.ai.config.ServiceClientProperties;
import com.recruitment.ai.config.RecommendationProperties;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestJobGateway implements JobGateway {

    private final RestClient recruitmentClient;
    private final RestClient companyClient;
    private final int recommendationFeedSize;

    public RestJobGateway(ServiceClientProperties properties, RecommendationProperties recommendationProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.recruitmentClient = RestClient.builder().baseUrl(properties.getRecruitmentServiceUrl())
                .requestFactory(requestFactory).build();
        this.companyClient = RestClient.builder().baseUrl(properties.getCompanyServiceUrl())
                .requestFactory(requestFactory).build();
        this.recommendationFeedSize = Math.max(1, Math.min(100, recommendationProperties.getCandidatePoolSize()));
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
            List<JsonNode> feedJobs = new ArrayList<>();
            int page = 0;
            boolean hasNext;
            do {
                int currentPage = page;
                JsonNode envelope = recruitmentClient.get()
                        .uri(builder -> builder.path("/api/v1/jobs/recommendation-feed").queryParam("page", currentPage)
                                .queryParam("size", recommendationFeedSize)
                                .queryParam("sort", "publishedAt,desc").build())
                        .retrieve().body(JsonNode.class);
                JsonNode data = envelope == null ? null : envelope.path("data");
                JsonNode content = data == null ? null : data.path("content");
                if (content == null || !content.isArray()) {
                    throw new BusinessException(ErrorCode.MATCH_UPSTREAM_UNAVAILABLE);
                }
                for (JsonNode job : content) {
                    feedJobs.add(job);
                }
                hasNext = data.path("hasNext").asBoolean(false);
                page++;
            } while (hasNext && page < 1);
            Set<UUID> companyIds = feedJobs.stream()
                    .map(job -> UUID.fromString(job.path("companyId").asText()))
                    .collect(Collectors.toSet());
            Map<UUID, UUID> companyOwners = companyOwners(companyIds);
            for (JsonNode job : feedJobs) {
                    UUID jobId = UUID.fromString(job.path("id").asText());
                    UUID companyId = UUID.fromString(job.path("companyId").asText());
                    UUID ownerId = companyOwners.get(companyId);
                    if (ownerId == null) throw new BusinessException(ErrorCode.MATCH_UPSTREAM_UNAVAILABLE);
                    jobs.add(new JobSnapshot(jobId, text(job, "title"), text(job, "description"),
                            text(job, "requirements"), text(job, "responsibilities"),
                            text(job, "experienceLevel"), text(job, "status"),
                            job.path("active").asBoolean(false), companyId, ownerId));
            }
            return jobs;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException | IllegalArgumentException exception) {
            log.error("Published job listing failed type={}", exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.MATCH_UPSTREAM_UNAVAILABLE);
        }
    }

    private Map<UUID, UUID> companyOwners(Set<UUID> requiredCompanyIds) {
        if (requiredCompanyIds.isEmpty()) return Map.of();
        JsonNode page = companyClient.get()
                .uri(builder -> builder.path("/api/v1/companies").queryParam("page", 0)
                        .queryParam("size", 1000).build())
                .retrieve().body(JsonNode.class);
        JsonNode content = page == null ? null : page.path("content");
        if (content == null || !content.isArray()) {
            throw new BusinessException(ErrorCode.MATCH_UPSTREAM_UNAVAILABLE);
        }
        Map<UUID, UUID> owners = new HashMap<>();
        for (JsonNode company : content) {
            UUID id = UUID.fromString(company.path("id").asText());
            if (requiredCompanyIds.contains(id) && company.hasNonNull("ownerId")) {
                owners.put(id, UUID.fromString(company.path("ownerId").asText()));
            }
        }
        return owners;
    }

    private String text(JsonNode node, String field) {
        return node.path(field).isNull() || node.path(field).isMissingNode() ? null : node.path(field).asText();
    }
}
