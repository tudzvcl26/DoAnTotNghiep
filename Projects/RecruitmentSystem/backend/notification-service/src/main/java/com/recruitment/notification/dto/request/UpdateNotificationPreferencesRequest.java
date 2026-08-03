package com.recruitment.notification.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateNotificationPreferencesRequest {

    @Valid
    @NotEmpty(message = "At least one preference is required.")
    private List<NotificationPreferenceItemRequest> preferences;

}
