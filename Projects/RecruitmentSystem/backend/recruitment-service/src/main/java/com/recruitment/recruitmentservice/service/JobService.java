package com.recruitment.recruitmentservice.service;

import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.job.CreateJobRequest;
import com.recruitment.recruitmentservice.dto.job.JobResponse;
import com.recruitment.recruitmentservice.dto.job.JobSearchRequest;
import com.recruitment.recruitmentservice.dto.job.JobSummaryResponse;
import com.recruitment.recruitmentservice.dto.job.EmployerJobStatisticsResponse;
import com.recruitment.recruitmentservice.dto.job.UpdateJobRequest;
import com.recruitment.recruitmentservice.entity.enums.JobStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JobService {

    JobResponse create(CreateJobRequest request);

    JobResponse update(
            UUID id,
            UpdateJobRequest request
    );

    JobResponse publish(UUID id);

    JobResponse close(UUID id);

    void delete(UUID id);

    JobResponse getById(UUID id);

    PageResponse<JobSummaryResponse> getAll(
            Pageable pageable
    );

    PageResponse<JobResponse> getRecommendationFeed(Pageable pageable);

    PageResponse<JobSummaryResponse> search(
            JobSearchRequest request,
            Pageable pageable
    );

    PageResponse<JobSummaryResponse> publicSearch(
            JobSearchRequest request,
            Pageable pageable
    );

    PageResponse<JobSummaryResponse> getEmployerJobs(
            UUID companyId,
            JobStatus status,
            String keyword,
            Pageable pageable
    );

    EmployerJobStatisticsResponse getEmployerStatistics();

}
