package com.recruitment.recruitmentservice.repository;

import com.recruitment.recruitmentservice.entity.JobLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobLocationRepository extends JpaRepository<JobLocation, UUID> {
}