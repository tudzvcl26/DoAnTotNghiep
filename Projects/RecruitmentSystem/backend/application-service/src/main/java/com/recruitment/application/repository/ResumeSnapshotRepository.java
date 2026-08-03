package com.recruitment.application.repository;

import com.recruitment.application.entity.ResumeSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeSnapshotRepository extends JpaRepository<ResumeSnapshot, UUID> {

    Optional<ResumeSnapshot> findByApplicationId(UUID applicationId);

}
