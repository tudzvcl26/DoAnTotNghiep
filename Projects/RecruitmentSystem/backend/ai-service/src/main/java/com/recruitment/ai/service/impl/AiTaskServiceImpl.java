package com.recruitment.ai.service.impl;

import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.dto.response.AiTaskResponse;
import com.recruitment.ai.entity.AiTask;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.mapper.AiTaskMapper;
import com.recruitment.ai.repository.AiTaskRepository;
import com.recruitment.ai.security.CurrentUser;
import com.recruitment.ai.security.SecurityUtils;
import com.recruitment.ai.service.AiTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiTaskServiceImpl implements AiTaskService {

    private final AiTaskRepository repository;
    private final AiTaskMapper mapper;

    @Override
    public AiTaskResponse getById(UUID taskId) {
        CurrentUser currentUser = authenticatedUser();
        AiTask task = currentUser.isAdmin()
                ? repository.findById(taskId).orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND))
                : repository.findByIdAndRequestedBy(taskId, currentUser.getUserId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        return mapper.toResponse(task);
    }

    @Override
    public PageResponse<AiTaskResponse> getTasks(Pageable pageable) {
        CurrentUser currentUser = authenticatedUser();
        return PageResponse.from(
                currentUser.isAdmin()
                        ? repository.findAll(pageable)
                        : repository.findByRequestedBy(currentUser.getUserId(), pageable),
                mapper::toResponse
        );
    }

    private CurrentUser authenticatedUser() {
        CurrentUser currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new AccessDeniedException("Bạn chưa đăng nhập.");
        }
        return currentUser;
    }

}
