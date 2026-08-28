package com.recruitment.ai.context;

import com.fasterxml.jackson.databind.JsonNode;

public record CandidateCareerContext(
        JsonNode profile,
        JsonNode skills,
        JsonNode education,
        JsonNode experience,
        JsonNode applications,
        JsonNode job
) { }
