package com.recruitment.ai.service;

import com.recruitment.ai.dto.response.InterviewPreparationResponse;
import com.recruitment.ai.dto.response.MatchExplanationResponse;
import java.util.UUID;

public interface ExplanationInterviewService {
    com.recruitment.ai.dto.response.AiTaskResponse queueExplanation(UUID matchId);
    com.recruitment.ai.dto.response.AiTaskResponse latestExplanationTask(UUID matchId);
    com.recruitment.ai.dto.response.AiTaskResponse queueInterview(UUID matchId);
    com.recruitment.ai.dto.response.AiTaskResponse latestInterviewTask(UUID matchId);
    MatchExplanationResponse generateExplanation(UUID matchId);
    MatchExplanationResponse getExplanation(UUID matchId);
    InterviewPreparationResponse generateInterview(UUID matchId);
    InterviewPreparationResponse getInterview(UUID matchId);
}
