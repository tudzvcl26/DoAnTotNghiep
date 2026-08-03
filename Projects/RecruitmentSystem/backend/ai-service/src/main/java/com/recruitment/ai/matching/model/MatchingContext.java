package com.recruitment.ai.matching.model;

import com.fasterxml.jackson.databind.JsonNode;

public record MatchingContext(JsonNode resumeFacts, JobSnapshot job, JobRequirements requirements) {
}
