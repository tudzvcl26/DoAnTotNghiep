package com.recruitment.notification.service.impl;

import com.recruitment.notification.dto.request.NotificationPreferenceItemRequest;
import com.recruitment.notification.dto.request.UpdateNotificationPreferencesRequest;
import com.recruitment.notification.dto.response.NotificationPreferenceResponse;
import com.recruitment.notification.entity.NotificationPreference;
import com.recruitment.notification.entity.enums.NotificationChannel;
import com.recruitment.notification.entity.enums.NotificationEventType;
import com.recruitment.notification.exception.BusinessException;
import com.recruitment.notification.exception.ErrorCode;
import com.recruitment.notification.repository.NotificationPreferenceRepository;
import com.recruitment.notification.security.CurrentUser;
import com.recruitment.notification.security.SecurityUtils;
import com.recruitment.notification.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> getMyPreferences() {
        Map<String, NotificationPreference> preferences = notificationPreferenceRepository
                .findByUserIdOrderByEventTypeAscChannelAsc(getCurrentUserId()).stream()
                .collect(java.util.stream.Collectors.toMap(
                        preference -> preference.getEventType().name() + preference.getChannel().name(),
                        preference -> preference
                ));

        return java.util.Arrays.stream(NotificationEventType.values())
                .flatMap(eventType -> java.util.Arrays.stream(NotificationChannel.values())
                        .map(channel -> {
                            NotificationPreference preference = preferences.get(eventType.name() + channel.name());
                            return NotificationPreferenceResponse.builder()
                                    .eventType(eventType)
                                    .channel(channel)
                                    .enabled(preference == null || preference.isEnabled())
                                    .build();
                        }))
                .toList();
    }

    @Override
    public List<NotificationPreferenceResponse> updateMyPreferences(UpdateNotificationPreferencesRequest request) {
        UUID userId = getCurrentUserId();
        for (NotificationPreferenceItemRequest item : request.getPreferences()) {
            validateSystemAnnouncementPreference(item);
            NotificationPreference preference = notificationPreferenceRepository
                    .findByUserIdAndEventTypeAndChannel(userId, item.getEventType(), item.getChannel())
                    .orElseGet(NotificationPreference::new);
            preference.setUserId(userId);
            preference.setEventType(item.getEventType());
            preference.setChannel(item.getChannel());
            preference.setEnabled(item.getEnabled());
            notificationPreferenceRepository.save(preference);
        }
        return getMyPreferences();
    }

    private void validateSystemAnnouncementPreference(NotificationPreferenceItemRequest item) {
        if (item.getEventType() == NotificationEventType.SYSTEM_ANNOUNCEMENT
                && item.getChannel() == NotificationChannel.IN_APP
                && !item.getEnabled()) {
            throw new BusinessException(ErrorCode.INVALID_NOTIFICATION_PREFERENCE);
        }
    }

    private UUID getCurrentUserId() {
        CurrentUser currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new AccessDeniedException("User is not authenticated.");
        }
        return currentUser.getUserId();
    }

}
