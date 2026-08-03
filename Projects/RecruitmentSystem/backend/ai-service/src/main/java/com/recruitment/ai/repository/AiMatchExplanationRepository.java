package com.recruitment.ai.repository;

import com.recruitment.ai.entity.AiMatchExplanation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AiMatchExplanationRepository extends JpaRepository<AiMatchExplanation, UUID> {
    Optional<AiMatchExplanation> findByMatchResultId(UUID matchResultId);
}
