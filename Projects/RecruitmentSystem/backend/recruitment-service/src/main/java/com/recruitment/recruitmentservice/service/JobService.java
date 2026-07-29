package com.recruitment.recruitmentservice.service;

import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.job.CreateJobRequest;
import com.recruitment.recruitmentservice.dto.job.JobResponse;
import com.recruitment.recruitmentservice.dto.job.JobSummaryResponse;
import com.recruitment.recruitmentservice.dto.job.UpdateJobRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JobService {

    JobResponse create(CreateJobRequest request);

    JobResponse update(
            UUID id,
            UpdateJobRequest request
    );

    void delete(UUID id);

    JobResponse getById(UUID id);

    PageResponse<JobSummaryResponse> getAll(
            Pageable pageable
    );

    PageResponse<JobSummaryResponse> search(
            String keyword,
            Pageable pageable
    );

}