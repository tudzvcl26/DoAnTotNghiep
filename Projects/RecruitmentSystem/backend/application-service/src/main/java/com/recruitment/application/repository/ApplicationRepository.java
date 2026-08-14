package com.recruitment.application.repository;

import com.recruitment.application.entity.Application;
import com.recruitment.application.entity.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByCandidateIdAndJobId(UUID candidateId, UUID jobId);

    Optional<Application> findByIdAndActiveTrue(UUID id);

    Page<Application> findByCandidateIdAndActiveTrue(UUID candidateId, Pageable pageable);

    Page<Application> findByJobIdAndActiveTrue(UUID jobId, Pageable pageable);

    Page<Application> findByJobIdAndStatusAndActiveTrue(UUID jobId, ApplicationStatus status, Pageable pageable);

    @Query("""
            select application from Application application
            where application.active = true
              and application.companyId in :companyIds
              and (:status is null or application.status = :status)
              and (:jobId is null or application.jobId = :jobId)
            """)
    Page<Application> findEmployerApplications(
            @Param("companyIds") Collection<UUID> companyIds,
            @Param("status") ApplicationStatus status,
            @Param("jobId") UUID jobId,
            Pageable pageable
    );

    @Query("""
            select application from Application application
            where application.active = true
              and (:status is null or application.status = :status)
              and (:jobId is null or application.jobId = :jobId)
              and (:companyId is null or application.companyId = :companyId)
              and (:candidateId is null or application.candidateId = :candidateId)
            """)
    Page<Application> findAdminApplications(
            @Param("status") ApplicationStatus status,
            @Param("jobId") UUID jobId,
            @Param("companyId") UUID companyId,
            @Param("candidateId") UUID candidateId,
            Pageable pageable
    );

    long countByActiveTrue();

    long countByActiveTrueAndStatus(ApplicationStatus status);

    long countByActiveTrueAndCompanyIdIn(Collection<UUID> companyIds);

    long countByActiveTrueAndCompanyIdInAndStatus(Collection<UUID> companyIds, ApplicationStatus status);

}
