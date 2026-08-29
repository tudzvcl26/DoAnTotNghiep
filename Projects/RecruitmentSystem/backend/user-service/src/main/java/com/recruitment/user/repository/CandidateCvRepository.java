package com.recruitment.user.repository;

import com.recruitment.user.entity.CandidateCv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidateCvRepository extends JpaRepository<CandidateCv, UUID> {
    List<CandidateCv> findAllByCandidateIdAndDeletedAtIsNullOrderByUpdatedAtDesc(UUID candidateId);
    Optional<CandidateCv> findByIdAndDeletedAtIsNull(UUID id);
}
