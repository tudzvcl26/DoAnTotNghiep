package com.recruitment.notification.service;

import com.recruitment.notification.dto.request.UpdateNotificationPreferencesRequest;
import com.recruitment.notification.dto.response.NotificationPreferenceResponse;

import java.util.List;

public interface NotificationPreferenceService {

    List<NotificationPreferenceResponse> getMyPreferences();

    List<NotificationPreferenceResponse> updateMyPreferences(UpdateNotificationPreferencesRequest request);

}
