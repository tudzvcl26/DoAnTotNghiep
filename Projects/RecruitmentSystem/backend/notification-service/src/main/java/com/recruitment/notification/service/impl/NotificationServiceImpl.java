package com.recruitment.notification.service.impl;

import com.recruitment.notification.common.PageResponse;
import com.recruitment.notification.dto.request.BroadcastNotificationRequest;
import com.recruitment.notification.dto.request.CreateNotificationRequest;
import com.recruitment.notification.dto.response.NotificationResponse;
import com.recruitment.notification.dto.response.UnreadNotificationCountResponse;
import com.recruitment.notification.entity.Notification;
import com.recruitment.notification.entity.NotificationUserState;
import com.recruitment.notification.entity.enums.NotificationAudienceType;
import com.recruitment.notification.entity.enums.NotificationEventType;
import com.recruitment.notification.exception.ErrorCode;
import com.recruitment.notification.exception.ResourceNotFoundException;
import com.recruitment.notification.mapper.NotificationMapper;
import com.recruitment.notification.repository.NotificationRepository;
import com.recruitment.notification.repository.NotificationUserStateRepository;
import com.recruitment.notification.security.CurrentUser;
import com.recruitment.notification.security.SecurityUtils;
import com.recruitment.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationUserStateRepository notificationUserStateRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationResponse create(CreateNotificationRequest request) {
        CurrentUser currentUser = getCurrentAuthenticatedUser();
        assertAdmin(currentUser);

        Notification notification = new Notification();
        notification.setEventType(request.getEventType());
        notification.setAudienceType(NotificationAudienceType.USER);
        notification.setTitle(request.getTitle().trim());
        notification.setContent(request.getContent().trim());
        notification.setPayload(request.getPayload());
        notification.setRelatedResourceType(request.getRelatedResourceType());
        notification.setRelatedResourceId(request.getRelatedResourceId());
        notification.setCreatedBy(currentUser.getUserId());
        Notification savedNotification = notificationRepository.save(notification);

        NotificationUserState state = new NotificationUserState();
        state.setNotification(savedNotification);
        state.setUserId(request.getRecipientUserId());
        NotificationUserState savedState = notificationUserStateRepository.save(state);
        return toResponse(savedState);
    }

    @Override
    public NotificationResponse broadcast(BroadcastNotificationRequest request) {
        CurrentUser currentUser = getCurrentAuthenticatedUser();
        assertAdmin(currentUser);

        Notification notification = new Notification();
        notification.setEventType(NotificationEventType.SYSTEM_ANNOUNCEMENT);
        notification.setAudienceType(NotificationAudienceType.ALL_USERS);
        notification.setTitle(request.getTitle().trim());
        notification.setContent(request.getContent().trim());
        notification.setPayload(request.getPayload());
        notification.setCreatedBy(currentUser.getUserId());
        Notification savedNotification = notificationRepository.save(notification);

        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    public PageResponse<NotificationResponse> getNotifications(
            UUID recipientUserId,
            NotificationEventType eventType,
            Boolean read,
            String query,
            Pageable pageable
    ) {
        CurrentUser currentUser = getCurrentAuthenticatedUser();
        UUID targetUserId = resolveTargetUserId(currentUser, recipientUserId);
        String normalizedQuery = query == null ? "" : query.trim();
        var notifications = notificationRepository.searchVisible(targetUserId, eventType, read, normalizedQuery, pageable);
        if (notifications.isEmpty()) {
            return PageResponse.from(notifications, notification -> toResponse(notification, null));
        }
        Map<UUID, NotificationUserState> statesByNotificationId = notificationUserStateRepository
                .findByUserIdAndNotificationIdIn(targetUserId,
                        notifications.getContent().stream().map(Notification::getId).toList())
                .stream()
                .collect(Collectors.toMap(state -> state.getNotification().getId(), Function.identity()));

        return PageResponse.from(notifications,
                notification -> toResponse(notification, statesByNotificationId.get(notification.getId())));
    }

    @Override
    public NotificationResponse getById(UUID notificationId) {
        NotificationUserState state = getAccessibleState(notificationId, getCurrentAuthenticatedUser());
        return toResponse(state);
    }

    @Override
    public NotificationResponse markAsRead(UUID notificationId) {
        NotificationUserState state = getAccessibleState(notificationId, getCurrentAuthenticatedUser());
        if (state.getReadAt() == null) {
            state.setReadAt(LocalDateTime.now());
            state = notificationUserStateRepository.save(state);
        }
        return toResponse(state);
    }

    @Override
    public void markAllAsRead() {
        CurrentUser currentUser = getCurrentAuthenticatedUser();
        notificationRepository.searchVisible(currentUser.getUserId(), null, false, "", Pageable.unpaged())
                .forEach(notification -> markNotificationAsRead(notification, currentUser.getUserId()));
    }

    @Override
    public void delete(UUID notificationId) {
        NotificationUserState state = getAccessibleState(notificationId, getCurrentAuthenticatedUser());
        state.setDeletedAt(LocalDateTime.now());
    }

    @Override
    public UnreadNotificationCountResponse getUnreadCount() {
        CurrentUser currentUser = getCurrentAuthenticatedUser();
        return UnreadNotificationCountResponse.builder()
                .unreadCount(notificationRepository.countVisibleUnread(currentUser.getUserId()))
                .build();
    }

    private NotificationUserState getAccessibleState(UUID notificationId, CurrentUser currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));
        NotificationUserState state = notificationUserStateRepository
                .findByNotificationIdAndUserId(notificationId, currentUser.getUserId())
                .orElseGet(() -> createStateForBroadcast(notification, currentUser));

        if (state.getDeletedAt() != null && !currentUser.isAdmin()) {
            throw new ResourceNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        return state;
    }

    private NotificationUserState createStateForBroadcast(Notification notification, CurrentUser currentUser) {
        if (notification.getAudienceType() != NotificationAudienceType.ALL_USERS && !currentUser.isAdmin()) {
            throw new ResourceNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        NotificationUserState state = new NotificationUserState();
        state.setNotification(notification);
        state.setUserId(currentUser.getUserId());
        return notificationUserStateRepository.save(state);
    }

    private NotificationResponse toResponse(NotificationUserState state) {
        return toResponse(state.getNotification(), state);
    }

    private NotificationResponse toResponse(Notification notification, NotificationUserState state) {
        NotificationResponse response = notificationMapper.toResponse(notification);
        if (state == null) {
            response.setRead(false);
            return response;
        }
        response.setRead(state.getReadAt() != null);
        response.setReadAt(state.getReadAt());
        return response;
    }

    private void markNotificationAsRead(Notification notification, UUID userId) {
        NotificationUserState state = notificationUserStateRepository.findByNotificationIdAndUserId(notification.getId(), userId)
                .orElseGet(() -> {
                    NotificationUserState newState = new NotificationUserState();
                    newState.setNotification(notification);
                    newState.setUserId(userId);
                    return newState;
                });
        if (state.getReadAt() == null) {
            state.setReadAt(LocalDateTime.now());
            notificationUserStateRepository.save(state);
        }
    }

    private UUID resolveTargetUserId(CurrentUser currentUser, UUID recipientUserId) {
        if (recipientUserId == null || recipientUserId.equals(currentUser.getUserId())) {
            return currentUser.getUserId();
        }
        if (!currentUser.isAdmin()) {
            throw new AccessDeniedException("You do not have permission to access notifications for another user.");
        }
        return recipientUserId;
    }

    private void assertAdmin(CurrentUser currentUser) {
        if (!currentUser.isAdmin()) {
            throw new AccessDeniedException("You do not have permission to create notifications.");
        }
    }

    private CurrentUser getCurrentAuthenticatedUser() {
        CurrentUser currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new AccessDeniedException("User is not authenticated.");
        }
        return currentUser;
    }

}
