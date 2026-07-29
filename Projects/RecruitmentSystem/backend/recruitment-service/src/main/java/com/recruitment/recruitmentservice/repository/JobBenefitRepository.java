package com.recruitment.recruitmentservice.repository;

import com.recruitment.recruitmentservice.entity.JobBenefit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobBenefitRepository extends JpaRepository<JobBenefit, UUID> {
}