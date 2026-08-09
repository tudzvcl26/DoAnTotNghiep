package com.recruitment.notification.consumer;

import com.recruitment.notification.amqp.NotificationEventEnvelope;
import com.recruitment.notification.entity.NotificationEventReceipt;
import com.recruitment.notification.entity.enums.NotificationEventReceiptStatus;
import com.recruitment.notification.entity.enums.NotificationEventType;
import com.recruitment.notification.repository.NotificationEventReceiptRepository;
import com.recruitment.notification.repository.NotificationRepository;
import com.recruitment.notification.repository.NotificationUserStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerImplTest {
    @Mock NotificationEventReceiptRepository receiptRepository;
    @Mock NotificationRepository notificationRepository;
    @Mock NotificationUserStateRepository userStateRepository;
    @InjectMocks NotificationEventHandlerImpl handler;

    @Test
    void processedEventIdIsIdempotent() {
        NotificationEventEnvelope event = event();
        NotificationEventReceipt receipt = new NotificationEventReceipt();
        receipt.setEventId(event.getEventId());
        receipt.setStatus(NotificationEventReceiptStatus.PROCESSED);
        when(receiptRepository.findByEventId(event.getEventId())).thenReturn(Optional.of(receipt));

        handler.handle(event);

        verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(userStateRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private NotificationEventEnvelope event() {
        NotificationEventEnvelope event = new NotificationEventEnvelope();
        event.setEventId(UUID.randomUUID());
        event.setEventVersion(1);
        event.setEventType(NotificationEventType.APPLICATION_SUBMITTED);
        event.setSourceService("application-service");
        event.setRecipientUserIds(List.of(UUID.randomUUID()));
        event.setTitle("New application");
        event.setContent("A candidate applied.");
        event.setPayload(Map.of("version", 1));
        return event;
    }
}
