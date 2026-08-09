package com.recruitment.notification.consumer;

import com.recruitment.notification.amqp.NotificationEventEnvelope;
import com.recruitment.notification.entity.NotificationEventReceipt;
import com.recruitment.notification.entity.enums.NotificationEventReceiptStatus;
import com.recruitment.notification.repository.NotificationEventReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationFailureRecorder {
    private final NotificationEventReceiptRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(NotificationEventEnvelope event, RuntimeException failure) {
        if (event == null || event.getEventId() == null || event.getEventType() == null) {
            return;
        }
        NotificationEventReceipt receipt = repository.findByEventId(event.getEventId())
                .orElseGet(NotificationEventReceipt::new);
        if (receipt.getStatus() == NotificationEventReceiptStatus.PROCESSED) {
            return;
        }
        receipt.setEventId(event.getEventId());
        receipt.setEventVersion(event.getEventVersion() == null ? 0 : event.getEventVersion());
        receipt.setSourceService(event.getSourceService() == null ? "unknown" : event.getSourceService());
        receipt.setEventType(event.getEventType());
        receipt.setPayload(event.getPayload());
        receipt.setStatus(NotificationEventReceiptStatus.FAILED);
        String message = failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
        receipt.setErrorMessage(message.length() <= 2000 ? message : message.substring(0, 2000));
        repository.save(receipt);
    }
}
