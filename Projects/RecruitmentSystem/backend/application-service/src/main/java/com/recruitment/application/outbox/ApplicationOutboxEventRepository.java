package com.recruitment.application.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ApplicationOutboxEventRepository extends JpaRepository<ApplicationOutboxEvent, UUID> {
    List<ApplicationOutboxEvent> findByStatusAndAvailableAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus status, LocalDateTime availableAt, Pageable pageable);
}
