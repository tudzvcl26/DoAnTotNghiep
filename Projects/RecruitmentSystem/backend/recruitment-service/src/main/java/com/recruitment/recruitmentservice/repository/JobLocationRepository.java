package com.recruitment.recruitmentservice.repository;

import com.recruitment.recruitmentservice.entity.JobLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface JobLocationRepository extends JpaRepository<JobLocation, UUID> {

    List<JobLocation> findByJob_IdIn(List<UUID> jobIds);
}
