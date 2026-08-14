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

    Optional<AiTask> findByIdAndRequestedBy(UUID id, UUID requestedBy);

    Page<AiTask> findByRequestedBy(UUID requestedBy, Pageable pageable);

    Optional<AiTask> findFirstByRequestedByAndTaskTypeAndStatusInOrderByCreatedAtDesc(
            UUID requestedBy, String taskType, Collection<AiTaskStatus> statuses);

}
