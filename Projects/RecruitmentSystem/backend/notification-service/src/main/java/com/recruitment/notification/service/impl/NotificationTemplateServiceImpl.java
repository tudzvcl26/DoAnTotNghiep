package com.recruitment.notification.service.impl;

import com.recruitment.notification.common.PageResponse;
import com.recruitment.notification.dto.request.CreateNotificationTemplateRequest;
import com.recruitment.notification.dto.request.UpdateNotificationTemplateRequest;
import com.recruitment.notification.dto.response.NotificationTemplateResponse;
import com.recruitment.notification.entity.NotificationTemplate;
import com.recruitment.notification.exception.BusinessException;
import com.recruitment.notification.exception.ErrorCode;
import com.recruitment.notification.exception.ResourceNotFoundException;
import com.recruitment.notification.mapper.NotificationTemplateMapper;
import com.recruitment.notification.repository.NotificationTemplateRepository;
import com.recruitment.notification.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationTemplateMapper notificationTemplateMapper;

    @Override
    public NotificationTemplateResponse create(CreateNotificationTemplateRequest request) {
        if (notificationTemplateRepository.findByCode(request.getCode().trim()).isPresent()) {
            throw new BusinessException(ErrorCode.NOTIFICATION_TEMPLATE_CODE_EXISTS);
        }
        NotificationTemplate template = new NotificationTemplate();
        template.setCode(request.getCode().trim());
        template.setEventType(request.getEventType());
        template.setChannel(request.getChannel());
        template.setTitleTemplate(request.getTitleTemplate().trim());
        template.setContentTemplate(request.getContentTemplate().trim());
        template.setActive(true);
        return notificationTemplateMapper.toResponse(notificationTemplateRepository.save(template));
    }

    @Override
    public NotificationTemplateResponse update(UUID id, UpdateNotificationTemplateRequest request) {
        NotificationTemplate template = getTemplate(id);
        template.setEventType(request.getEventType());
        template.setChannel(request.getChannel());
        template.setTitleTemplate(request.getTitleTemplate().trim());
        template.setContentTemplate(request.getContentTemplate().trim());
        return notificationTemplateMapper.toResponse(template);
    }

    @Override
    public NotificationTemplateResponse updateActive(UUID id, boolean active) {
        NotificationTemplate template = getTemplate(id);
        template.setActive(active);
        return notificationTemplateMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationTemplateResponse> getAll(Boolean active, Pageable pageable) {
        if (active == null) {
            return PageResponse.from(notificationTemplateRepository.findAll(pageable), notificationTemplateMapper::toResponse);
        }
        return PageResponse.from(notificationTemplateRepository.findByActive(active, pageable), notificationTemplateMapper::toResponse);
    }

    private NotificationTemplate getTemplate(UUID id) {
        return notificationTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND));
    }

}
