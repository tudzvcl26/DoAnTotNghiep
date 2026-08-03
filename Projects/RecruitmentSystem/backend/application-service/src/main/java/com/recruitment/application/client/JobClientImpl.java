package com.recruitment.application.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class JobClientImpl implements JobClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public JobClientImpl(
            @Value("${services.recruitment-service.url:http://localhost:8084}") String recruitmentServiceUrl,
            ObjectMapper objectMapper
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(recruitmentServiceUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<JobClientDto> getJobById(UUID jobId) {
        try {
            String responseStr = restClient.get()
                    .uri("/api/v1/jobs/{jobId}", jobId)
                    .retrieve()
                    .body(String.class);

            if (responseStr == null || responseStr.isBlank()) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(responseStr);
            JsonNode data = root.has("data") ? root.get("data") : root;

            if (data == null || data.isNull()) {
                return Optional.empty();
            }

            JobClientDto dto = JobClientDto.builder()
                    .id(data.has("id") && !data.get("id").isNull() ? UUID.fromString(data.get("id").asText()) : null)
                    .title(data.has("title") && !data.get("title").isNull() ? data.get("title").asText() : null)
                    .jobCode(data.has("jobCode") && !data.get("jobCode").isNull() ? data.get("jobCode").asText() : null)
                    .status(data.has("status") && !data.get("status").isNull() ? data.get("status").asText() : null)
                    .companyId(data.has("companyId") && !data.get("companyId").isNull() ? UUID.fromString(data.get("companyId").asText()) : null)
                    .applicationDeadline(data.has("applicationDeadline") && !data.get("applicationDeadline").isNull() ? LocalDate.parse(data.get("applicationDeadline").asText()) : null)
                    .description(data.has("description") && !data.get("description").isNull() ? data.get("description").asText() : null)
                    .requirements(data.has("requirements") && !data.get("requirements").isNull() ? data.get("requirements").asText() : null)
                    .responsibilities(data.has("responsibilities") && !data.get("responsibilities").isNull() ? data.get("responsibilities").asText() : null)
                    .employmentType(data.has("employmentType") && !data.get("employmentType").isNull() ? data.get("employmentType").asText() : null)
                    .experienceLevel(data.has("experienceLevel") && !data.get("experienceLevel").isNull() ? data.get("experienceLevel").asText() : null)
                    .salaryMin(data.has("salaryMin") && !data.get("salaryMin").isNull() ? new BigDecimal(data.get("salaryMin").asText()) : null)
                    .salaryMax(data.has("salaryMax") && !data.get("salaryMax").isNull() ? new BigDecimal(data.get("salaryMax").asText()) : null)
                    .currency(data.has("currency") && !data.get("currency").isNull() ? data.get("currency").asText() : null)
                    .rawJsonData(data.toString())
                    .build();

            return Optional.of(dto);
        } catch (Exception e) {
            log.error("Failed to retrieve job details for jobId {}: {}", jobId, e.getMessage());
            return Optional.empty();
        }
    }

}
