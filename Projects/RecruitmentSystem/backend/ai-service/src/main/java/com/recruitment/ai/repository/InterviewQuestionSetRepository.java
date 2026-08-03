package com.recruitment.ai.repository;

import com.recruitment.ai.entity.InterviewQuestionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface InterviewQuestionSetRepository extends JpaRepository<InterviewQuestionSet, UUID> {
    Optional<InterviewQuestionSet> findByMatchResultId(UUID matchResultId);
}
