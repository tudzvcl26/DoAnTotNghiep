package com.recruitment.user.repository;

import com.recruitment.user.entity.CandidatePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CandidatePreferenceRepository extends JpaRepository<CandidatePreference, UUID> {
    Optional<CandidatePreference> findByProfileIdAndDeletedAtIsNull(UUID profileId);
    boolean existsByProfileIdAndDeletedAtIsNull(UUID profileId);
}
