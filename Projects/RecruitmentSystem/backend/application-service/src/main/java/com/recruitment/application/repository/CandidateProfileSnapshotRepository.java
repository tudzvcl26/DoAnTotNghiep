package com.recruitment.application.repository;

import com.recruitment.application.entity.CandidateProfileSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidateProfileSnapshotRepository extends JpaRepository<CandidateProfileSnapshot, UUID> {

    Optional<CandidateProfileSnapshot> findByApplicationId(UUID applicationId);

    List<CandidateProfileSnapshot> findByApplicationIdIn(Collection<UUID> applicationIds);
}
