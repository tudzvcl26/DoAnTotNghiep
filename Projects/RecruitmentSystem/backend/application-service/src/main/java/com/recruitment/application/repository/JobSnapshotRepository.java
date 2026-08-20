package com.recruitment.application.repository;

import com.recruitment.application.entity.JobSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobSnapshotRepository extends JpaRepository<JobSnapshot, UUID> {

    Optional<JobSnapshot> findByApplicationId(UUID applicationId);

    List<JobSnapshot> findByApplicationIdIn(List<UUID> applicationIds);

}
