package com.recruitment.notification.service.impl;

import com.recruitment.notification.common.PageResponse;
import com.recruitment.notification.dto.response.NotificationDeliveryLogResponse;
import com.recruitment.notification.entity.enums.NotificationDeliveryStatus;
import com.recruitment.notification.mapper.NotificationDeliveryLogMapper;
import com.recruitment.notification.repository.NotificationDeliveryLogRepository;
import com.recruitment.notification.service.NotificationDeliveryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationDeliveryLogServiceImpl implements NotificationDeliveryLogService {

    private final NotificationDeliveryLogRepository notificationDeliveryLogRepository;
    private final NotificationDeliveryLogMapper notificationDeliveryLogMapper;

    @Override
    public PageResponse<NotificationDeliveryLogResponse> getAll(NotificationDeliveryStatus status, Pageable pageable) {
        if (status == null) {
            return PageResponse.from(notificationDeliveryLogRepository.findAll(pageable), notificationDeliveryLogMapper::toResponse);
        }
        return PageResponse.from(notificationDeliveryLogRepository.findByStatus(status, pageable), notificationDeliveryLogMapper::toResponse);
    }

}
