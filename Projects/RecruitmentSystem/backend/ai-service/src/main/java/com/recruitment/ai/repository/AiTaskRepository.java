package com.recruitment.ai.repository;

import com.recruitment.ai.entity.AiTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.Collection;
import com.recruitment.ai.entity.enums.AiTaskStatus;

@Repository
public interface AiTaskRepository extends JpaRepository<AiTask, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select t from AiTask t where t.id = :id")
    Optional<AiTask> lockById(@org.springframework.data.repository.query.Param("id") UUID id);

    Optional<AiTask> findFirstBySubjectIdAndTaskTypeAndStatusInOrderByCreatedAtDesc(
            UUID subjectId, String taskType, Collection<AiTaskStatus> statuses);
    Optional<AiTask> findFirstBySubjectIdAndTaskTypeAndRequestedByOrderByCreatedAtDesc(UUID subjectId, String taskType, UUID requestedBy);
    java.util.List<AiTask> findTop20ByTaskTypeAndStatusOrderByCreatedAtAsc(String taskType, AiTaskStatus status);
    java.util.List<AiTask> findTop20ByTaskTypeInAndStatusOrderByCreatedAtAsc(Collection<String> taskTypes, AiTaskStatus status);
    java.util.List<AiTask> findByTaskTypeInAndStatusInAndCreatedAtBefore(Collection<String> types,
            Collection<AiTaskStatus> statuses, java.time.LocalDateTime cutoff);

    Optional<AiTask> findFirstBySubjectIdAndTaskTypeAndStatusOrderByCreatedAtDesc(
            UUID subjectId, String taskType, AiTaskStatus status);

    Optional<AiTask> findByIdAndRequestedBy(UUID id, UUID requestedBy);

    Page<AiTask> findByRequestedBy(UUID requestedBy, Pageable pageable);

    Optional<AiTask> findFirstByRequestedByAndTaskTypeAndStatusInOrderByCreatedAtDesc(
            UUID requestedBy, String taskType, Collection<AiTaskStatus> statuses);

    java.util.List<AiTask> findByStatus(AiTaskStatus status);

}
