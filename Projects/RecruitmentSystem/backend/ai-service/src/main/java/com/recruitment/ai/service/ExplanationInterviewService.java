package com.recruitment.ai.service;

import com.recruitment.ai.dto.response.InterviewPreparationResponse;
import com.recruitment.ai.dto.response.MatchExplanationResponse;
import java.util.UUID;

public interface ExplanationInterviewService {
    MatchExplanationResponse generateExplanation(UUID matchId);
    MatchExplanationResponse getExplanation(UUID matchId);
    InterviewPreparationResponse generateInterview(UUID matchId);
    InterviewPreparationResponse getInterview(UUID matchId);
}
