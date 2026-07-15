package com.recruitment.user.repository;

import com.recruitment.user.entity.CareerObjective;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CareerObjectiveRepository extends JpaRepository<CareerObjective, UUID> {
    Optional<CareerObjective> findByProfileIdAndDeletedAtIsNull(UUID profileId);
    boolean existsByProfileIdAndDeletedAtIsNull(UUID profileId);
}
