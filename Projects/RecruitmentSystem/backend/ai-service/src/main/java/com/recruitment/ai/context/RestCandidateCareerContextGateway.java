package com.recruitment.ai.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recruitment.ai.config.ServiceClientProperties;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class RestCandidateCareerContextGateway implements CandidateCareerContextGateway {

    private static final int MAX_ITEMS = 20;
    private static final int MAX_TEXT = 4000;
    private final RestClient userClient;
    private final RestClient applicationClient;
    private final RestClient recruitmentClient;
    private final ObjectMapper objectMapper;

    public RestCandidateCareerContextGateway(ServiceClientProperties properties, ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.userClient = RestClient.builder().baseUrl(properties.getUserServiceUrl())
                .requestFactory(requestFactory).build();
        this.applicationClient = RestClient.builder().baseUrl(properties.getApplicationServiceUrl())
                .requestFactory(requestFactory).build();
        this.recruitmentClient = RestClient.builder().baseUrl(properties.getRecruitmentServiceUrl())
                .requestFactory(requestFactory).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public CandidateCareerContext load(UUID authenticatedUserId, UUID jobId, String accessToken) {
        try {
            JsonNode profile = safeProfile(data(get(userClient, "/api/v1/profiles/me", accessToken)));
            JsonNode skills = safeCollection(content(get(userClient,
                    "/api/v1/users/" + authenticatedUserId + "/skills?page=0&size=" + MAX_ITEMS, accessToken)),
                    List.of("skillName", "skillLevel", "yearsExperience"));
            JsonNode education = safeCollection(content(get(userClient,
                    "/api/v1/users/" + authenticatedUserId + "/educations?page=0&size=" + MAX_ITEMS, accessToken)),
                    List.of("institutionName", "qualification", "fieldOfStudy", "startDate", "endDate", "description"));
            JsonNode experience = safeCollection(content(get(userClient,
                    "/api/v1/users/" + authenticatedUserId + "/experiences?page=0&size=" + MAX_ITEMS, accessToken)),
                    List.of("employerName", "jobTitle", "employmentType", "location", "startDate", "endDate",
                            "current", "description", "achievements"));
            JsonNode applications = safeApplications(content(get(applicationClient,
                    "/api/v1/applications/my?page=0&size=10&sort=createdAt,desc", accessToken)));
            JsonNode job = jobId == null ? objectMapper.nullNode()
                    : safeJob(data(get(recruitmentClient, "/api/v1/jobs/" + jobId, accessToken)));
            return new CandidateCareerContext(profile, skills, education, experience, applications, job);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException | IllegalArgumentException exception) {
            log.warn("Candidate career context unavailable type={}", exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.CAREER_CONTEXT_UNAVAILABLE);
        }
    }

    private JsonNode get(RestClient client, String uri, String token) {
        try {
            return client.get().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve().body(JsonNode.class);
        } catch (HttpClientErrorException.NotFound exception) {
            return objectMapper.nullNode();
        }
    }

    private JsonNode data(JsonNode envelope) {
        return envelope == null || envelope.isNull() ? objectMapper.nullNode() : envelope.path("data");
    }

    private JsonNode content(JsonNode envelope) {
        JsonNode value = data(envelope).path("content");
        return value.isArray() ? value : objectMapper.createArrayNode();
    }

    private JsonNode safeProfile(JsonNode source) {
        return safeObject(source, List.of("displayName", "headline", "summary", "countryCode", "cityName",
                "districtName", "profileStatus", "completionScore"));
    }

    private JsonNode safeJob(JsonNode source) {
        return safeObject(source, List.of("title", "description", "requirements", "responsibilities",
                "employmentType", "experienceLevel", "applicationDeadline", "remoteAllowed", "location",
                "categoryName", "status"));
    }

    private JsonNode safeCollection(JsonNode source, List<String> allowedFields) {
        ArrayNode result = objectMapper.createArrayNode();
        if (!source.isArray()) return result;
        for (JsonNode item : source) {
            if (result.size() >= MAX_ITEMS) break;
            result.add(safeObject(item, allowedFields));
        }
        return result;
    }

    private JsonNode safeApplications(JsonNode source) {
        ArrayNode result = objectMapper.createArrayNode();
        if (!source.isArray()) return result;
        for (JsonNode item : source) {
            if (result.size() >= 10) break;
            ObjectNode safe = objectMapper.createObjectNode();
            copy(item, safe, "status");
            copy(item, safe, "appliedAt");
            JsonNode snapshot = item.path("jobSnapshot").path("snapshotData");
            if (snapshot.isTextual()) {
                try {
                    safe.set("job", safeObject(objectMapper.readTree(snapshot.asText()),
                            List.of("title", "jobCode", "companyName", "employmentType", "experienceLevel")));
                } catch (Exception ignored) {
                    // A malformed historical snapshot is omitted instead of leaking raw data.
                }
            }
            result.add(safe);
        }
        return result;
    }

    private ObjectNode safeObject(JsonNode source, List<String> allowedFields) {
        ObjectNode result = objectMapper.createObjectNode();
        if (source == null || !source.isObject()) return result;
        for (String field : allowedFields) copy(source, result, field);
        return result;
    }

    private void copy(JsonNode source, ObjectNode target, String field) {
        JsonNode value = source.path(field);
        if (value.isMissingNode() || value.isNull()) return;
        if (value.isTextual()) {
            String text = value.asText();
            target.put(field, text.length() > MAX_TEXT ? text.substring(0, MAX_TEXT) : text);
        } else if (value.isNumber()) {
            target.set(field, value);
        } else if (value.isBoolean()) {
            target.put(field, value.asBoolean());
        }
    }
}
