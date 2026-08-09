package com.recruitment.application.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.application.entity.Application;
import com.recruitment.application.entity.enums.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationEventOutboxService {
    private static final int EVENT_VERSION = 1;
    private final ApplicationOutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void applicationSubmitted(Application application, UUID employerUserId) {
        enqueue(application, "APPLICATION_SUBMITTED", "application.submitted", employerUserId,
                "New application received", "A candidate applied for your job.",
                Map.of("candidateId", application.getCandidateId(), "companyId", application.getCompanyId(),
                        "jobId", application.getJobId(), "status", application.getStatus().name()));
    }

    @Transactional
    public void applicationStatusChanged(Application application, ApplicationStatus fromStatus) {
        enqueue(application, "APPLICATION_STATUS_CHANGED", "application.status-changed", application.getCandidateId(),
                "Application status updated", "Your application status changed to " + application.getStatus().name() + ".",
                Map.of("candidateId", application.getCandidateId(), "companyId", application.getCompanyId(),
                        "jobId", application.getJobId(), "fromStatus", fromStatus.name(),
                        "toStatus", application.getStatus().name()));
    }

    @Transactional
    public void applicationWithdrawn(Application application, UUID employerUserId) {
        enqueue(application, "APPLICATION_WITHDRAWN", "application.withdrawn", employerUserId,
                "Application withdrawn", "A candidate withdrew an application.",
                Map.of("candidateId", application.getCandidateId(), "companyId", application.getCompanyId(),
                        "jobId", application.getJobId(), "status", application.getStatus().name()));
    }

    private void enqueue(Application application, String eventType, String routingKey, UUID recipient,
                         String title, String content, Map<String, Object> payload) {
        UUID eventId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        NotificationEventEnvelope envelope = NotificationEventEnvelope.builder()
                .eventId(eventId).eventVersion(EVENT_VERSION).eventType(eventType)
                .sourceService("application-service").occurredAt(now)
                .recipientUserIds(List.of(recipient)).title(title).content(content)
                .relatedResourceType("APPLICATION").relatedResourceId(application.getId())
                .payload(payload).build();
        ApplicationOutboxEvent outbox = new ApplicationOutboxEvent();
        outbox.setEventId(eventId);
        outbox.setAggregateId(application.getId());
        outbox.setEventType(eventType);
        outbox.setEventVersion(EVENT_VERSION);
        outbox.setRoutingKey(routingKey);
        try {
            outbox.setPayload(objectMapper.writeValueAsString(envelope));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize application event", exception);
        }
        outbox.setStatus(OutboxStatus.PENDING);
        outbox.setAttempts(0);
        outbox.setAvailableAt(now);
        repository.save(outbox);
    }
}
