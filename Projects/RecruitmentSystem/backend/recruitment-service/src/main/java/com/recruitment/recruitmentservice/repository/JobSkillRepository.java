package com.recruitment.recruitmentservice.repository;

import com.recruitment.recruitmentservice.entity.JobSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobSkillRepository extends JpaRepository<JobSkill, UUID> {
}