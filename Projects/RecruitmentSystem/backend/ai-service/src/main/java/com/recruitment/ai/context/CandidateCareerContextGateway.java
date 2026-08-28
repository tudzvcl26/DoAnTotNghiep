package com.recruitment.ai.context;

import java.util.UUID;

public interface CandidateCareerContextGateway {
    CandidateCareerContext load(UUID authenticatedUserId, UUID jobId, String accessToken);
}
