package com.recruitment.notification.mapper;

import com.recruitment.notification.dto.response.NotificationDeliveryLogResponse;
import com.recruitment.notification.entity.NotificationDeliveryLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationDeliveryLogMapper {

    @Mapping(target = "notificationId", source = "notification.id")
    NotificationDeliveryLogResponse toResponse(NotificationDeliveryLog deliveryLog);

}
