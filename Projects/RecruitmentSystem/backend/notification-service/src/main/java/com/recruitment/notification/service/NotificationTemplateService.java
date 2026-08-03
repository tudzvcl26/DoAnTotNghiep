package com.recruitment.notification.service;

import com.recruitment.notification.common.PageResponse;
import com.recruitment.notification.dto.request.CreateNotificationTemplateRequest;
import com.recruitment.notification.dto.request.UpdateNotificationTemplateRequest;
import com.recruitment.notification.dto.response.NotificationTemplateResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationTemplateService {

    NotificationTemplateResponse create(CreateNotificationTemplateRequest request);

    NotificationTemplateResponse update(UUID id, UpdateNotificationTemplateRequest request);

    NotificationTemplateResponse updateActive(UUID id, boolean active);

    PageResponse<NotificationTemplateResponse> getAll(Boolean active, Pageable pageable);

}
