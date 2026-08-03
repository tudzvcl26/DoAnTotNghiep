package com.recruitment.notification.mapper;

import com.recruitment.notification.dto.response.NotificationResponse;
import com.recruitment.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "read", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    NotificationResponse toResponse(Notification notification);

}
