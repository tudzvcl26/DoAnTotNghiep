package com.recruitment.application.repository;

import com.recruitment.application.entity.Application;
import com.recruitment.application.entity.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByCandidateIdAndJobId(UUID candidateId, UUID jobId);

    Optional<Application> findByIdAndActiveTrue(UUID id);

    Page<Application> findByCandidateIdAndActiveTrue(UUID candidateId, Pageable pageable);

    Page<Application> findByJobIdAndActiveTrue(UUID jobId, Pageable pageable);

    Page<Application> findByJobIdAndStatusAndActiveTrue(UUID jobId, ApplicationStatus status, Pageable pageable);

}
