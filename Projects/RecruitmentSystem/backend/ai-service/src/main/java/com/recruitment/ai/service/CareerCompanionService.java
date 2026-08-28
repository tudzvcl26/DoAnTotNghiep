package com.recruitment.ai.service;

import com.recruitment.ai.dto.request.CareerChatRequest;
import com.recruitment.ai.dto.response.CareerChatResponse;

public interface CareerCompanionService {
    CareerChatResponse chat(CareerChatRequest request);
}
