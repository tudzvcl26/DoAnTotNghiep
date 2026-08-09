package com.recruitment.notification.consumer;

import com.recruitment.notification.amqp.NotificationEventEnvelope;
import com.recruitment.notification.entity.Notification;
import com.recruitment.notification.entity.NotificationEventReceipt;
import com.recruitment.notification.entity.NotificationUserState;
import com.recruitment.notification.entity.enums.NotificationAudienceType;
import com.recruitment.notification.entity.enums.NotificationEventReceiptStatus;
import com.recruitment.notification.repository.NotificationEventReceiptRepository;
import com.recruitment.notification.repository.NotificationRepository;
import com.recruitment.notification.repository.NotificationUserStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationEventHandlerImpl implements NotificationEventHandler {

    private final NotificationEventReceiptRepository notificationEventReceiptRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationUserStateRepository notificationUserStateRepository;

    @Override
    public void handle(NotificationEventEnvelope event) {
        validate(event);
        NotificationEventReceipt receipt = notificationEventReceiptRepository.findByEventId(event.getEventId())
                .orElseGet(NotificationEventReceipt::new);
        if (receipt.getStatus() == NotificationEventReceiptStatus.PROCESSED) {
            return;
        }
        receipt.setEventId(event.getEventId());
        receipt.setEventVersion(event.getEventVersion());
        receipt.setSourceService(event.getSourceService());
        receipt.setEventType(event.getEventType());
        receipt.setStatus(NotificationEventReceiptStatus.RECEIVED);
        receipt.setPayload(event.getPayload());
        receipt.setErrorMessage(null);
        notificationEventReceiptRepository.save(receipt);

        Notification notification = new Notification();
        notification.setEventType(event.getEventType());
        notification.setAudienceType(NotificationAudienceType.USER);
        notification.setTitle(event.getTitle());
        notification.setContent(event.getContent());
        notification.setPayload(event.getPayload());
        notification.setRelatedResourceType(event.getRelatedResourceType());
        notification.setRelatedResourceId(event.getRelatedResourceId());
        notification = notificationRepository.save(notification);

        for (var recipientUserId : event.getRecipientUserIds()) {
            NotificationUserState state = new NotificationUserState();
            state.setNotification(notification);
            state.setUserId(recipientUserId);
            notificationUserStateRepository.save(state);
        }

        receipt.setStatus(NotificationEventReceiptStatus.PROCESSED);
        receipt.setProcessedAt(LocalDateTime.now());
    }

    private void validate(NotificationEventEnvelope event) {
        if (event == null || event.getEventId() == null || event.getEventVersion() == null
                || event.getEventVersion() != 1 || event.getEventType() == null
                || event.getSourceService() == null || event.getSourceService().isBlank()
                || event.getRecipientUserIds() == null || event.getRecipientUserIds().isEmpty()
                || event.getTitle() == null || event.getTitle().isBlank()
                || event.getContent() == null || event.getContent().isBlank()) {
            throw new IllegalArgumentException("Notification event does not satisfy the required event contract.");
        }
    }

}
