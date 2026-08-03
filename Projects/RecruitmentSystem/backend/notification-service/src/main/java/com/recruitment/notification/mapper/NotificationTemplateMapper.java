package com.recruitment.notification.mapper;

import com.recruitment.notification.dto.response.NotificationTemplateResponse;
import com.recruitment.notification.entity.NotificationTemplate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationTemplateMapper {

    NotificationTemplateResponse toResponse(NotificationTemplate template);

}
