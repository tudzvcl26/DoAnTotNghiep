package com.recruitment.ai.service;

import com.recruitment.ai.dto.request.CandidateAssistantRequest;
import com.recruitment.ai.dto.request.RecruiterAssistantRequest;
import com.recruitment.ai.dto.response.AssistantResponseDto;

public interface AssistantService {
    AssistantResponseDto assistCandidate(CandidateAssistantRequest request);
    AssistantResponseDto assistRecruiter(RecruiterAssistantRequest request);
}
