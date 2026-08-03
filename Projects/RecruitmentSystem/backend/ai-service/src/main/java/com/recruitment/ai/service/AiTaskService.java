package com.recruitment.ai.service;

import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.dto.response.AiTaskResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AiTaskService {

    AiTaskResponse getById(UUID taskId);

    PageResponse<AiTaskResponse> getTasks(Pageable pageable);

}
