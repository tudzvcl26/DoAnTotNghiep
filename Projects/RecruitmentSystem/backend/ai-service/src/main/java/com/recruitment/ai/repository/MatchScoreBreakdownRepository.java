package com.recruitment.ai.repository;

import com.recruitment.ai.entity.MatchScoreBreakdown;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchScoreBreakdownRepository extends JpaRepository<MatchScoreBreakdown, UUID> {
}
