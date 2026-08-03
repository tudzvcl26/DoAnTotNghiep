package com.recruitment.notification.service;

import com.recruitment.notification.dto.request.BroadcastNotificationRequest;
import com.recruitment.notification.dto.response.NotificationResponse;
import com.recruitment.notification.entity.Notification;
import com.recruitment.notification.entity.enums.NotificationAudienceType;
import com.recruitment.notification.entity.enums.NotificationEventType;
import com.recruitment.notification.exception.ResourceNotFoundException;
import com.recruitment.notification.mapper.NotificationMapper;
import com.recruitment.notification.repository.NotificationRepository;
import com.recruitment.notification.repository.NotificationUserStateRepository;
import com.recruitment.notification.security.CurrentUser;
import com.recruitment.notification.security.JwtAuthenticationToken;
import com.recruitment.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationUserStateRepository notificationUserStateRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUpSecurityContext() {
        CurrentUser currentUser = CurrentUser.builder()
                .userId(UUID.randomUUID())
                .email("candidate@example.com")
                .roles(Set.of("CANDIDATE"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(currentUser, "token", List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void broadcastCreatesOnlyGlobalNotificationWithoutMaterializingUserStates() {
        CurrentUser admin = CurrentUser.builder()
                .userId(UUID.randomUUID())
                .email("admin@example.com")
                .roles(Set.of("ADMIN"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(admin, "token", List.of()));

        BroadcastNotificationRequest request = new BroadcastNotificationRequest();
        request.setTitle("System maintenance");
        request.setContent("The system will be unavailable at midnight.");

        Notification savedNotification = new Notification();
        savedNotification.setId(UUID.randomUUID());
        savedNotification.setAudienceType(NotificationAudienceType.ALL_USERS);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.toResponse(savedNotification)).thenReturn(NotificationResponse.builder()
                .id(savedNotification.getId()).build());

        NotificationResponse response = notificationService.broadcast(request);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getEventType()).isEqualTo(NotificationEventType.SYSTEM_ANNOUNCEMENT);
        assertThat(notificationCaptor.getValue().getAudienceType()).isEqualTo(NotificationAudienceType.ALL_USERS);
        assertThat(response.getId()).isEqualTo(savedNotification.getId());
        verify(notificationUserStateRepository, never()).save(any());
    }

    @Test
    void getByIdDoesNotRevealAnotherUsersPersonalNotification() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setAudienceType(NotificationAudienceType.USER);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationUserStateRepository.findByNotificationIdAndUserId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getById(notificationId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(notificationMapper, never()).toResponse(any(Notification.class));
    }

}
