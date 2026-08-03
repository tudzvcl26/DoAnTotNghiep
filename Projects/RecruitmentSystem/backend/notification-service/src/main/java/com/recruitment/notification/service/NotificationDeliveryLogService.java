package com.recruitment.notification.service;

import com.recruitment.notification.common.PageResponse;
import com.recruitment.notification.dto.response.NotificationDeliveryLogResponse;
import com.recruitment.notification.entity.enums.NotificationDeliveryStatus;
import org.springframework.data.domain.Pageable;

public interface NotificationDeliveryLogService {

    PageResponse<NotificationDeliveryLogResponse> getAll(NotificationDeliveryStatus status, Pageable pageable);

}
