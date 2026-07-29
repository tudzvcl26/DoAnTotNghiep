package com.recruitment.recruitmentservice.service.impl;

import org.springframework.data.domain.Pageable;
import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.job.CreateJobRequest;
import com.recruitment.recruitmentservice.dto.job.JobResponse;
import com.recruitment.recruitmentservice.dto.job.JobSummaryResponse;
import com.recruitment.recruitmentservice.dto.job.UpdateJobRequest;
import com.recruitment.recruitmentservice.entity.Job;
import com.recruitment.recruitmentservice.entity.JobCategory;
import com.recruitment.recruitmentservice.exception.BusinessException;
import com.recruitment.recruitmentservice.exception.ErrorCode;
import com.recruitment.recruitmentservice.mapper.JobMapper;
import com.recruitment.recruitmentservice.repository.JobCategoryRepository;
import com.recruitment.recruitmentservice.repository.JobRepository;
import com.recruitment.recruitmentservice.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    private final JobCategoryRepository jobCategoryRepository;

    private final JobMapper jobMapper;

    @Override
    public JobResponse create(CreateJobRequest request) {

        if (jobRepository.existsByJobCode(request.getJobCode())) {
            throw new BusinessException(
                    ErrorCode.JOB_CODE_ALREADY_EXISTS
            );
        }

        JobCategory category = jobCategoryRepository
                .findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.JOB_CATEGORY_NOT_FOUND
                        )
                );

        Job job = jobMapper.toEntity(request);

        job.setCategory(category);

        Job savedJob = jobRepository.save(job);

        return jobMapper.toResponse(savedJob);
    }
    @Override
    public JobResponse update(
            UUID id,
            UpdateJobRequest request
    ) {

        Job job = jobRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.JOB_NOT_FOUND
                        )
                );

        if (jobRepository.existsByJobCodeAndIdNot(
                request.getJobCode(),
                id
        )) {
            throw new BusinessException(
                    ErrorCode.JOB_CODE_ALREADY_EXISTS
            );
        }

        JobCategory category = jobCategoryRepository
                .findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.JOB_CATEGORY_NOT_FOUND
                        )
                );

        jobMapper.updateEntity(request, job);

        job.setCategory(category);

        Job updatedJob = jobRepository.save(job);

        return jobMapper.toResponse(updatedJob);
    }

    @Override
    public void delete(UUID id) {

        Job job = jobRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.JOB_NOT_FOUND
                        )
                );

        // Soft delete
        job.setActive(false);

        jobRepository.save(job);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getById(UUID id) {

        Job job = jobRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.JOB_NOT_FOUND
                        )
                );

        return jobMapper.toResponse(job);
    }
    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobSummaryResponse> getAll(
            Pageable pageable
    ) {

        return PageResponse.from(
                jobRepository.findByActiveTrue(pageable),
                jobMapper::toSummaryResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobSummaryResponse> search(
            String keyword,
            Pageable pageable
    ) {

        if (keyword == null || keyword.isBlank()) {
            return PageResponse.from(
                    jobRepository.findByActiveTrue(pageable),
                    jobMapper::toSummaryResponse
            );
        }

        return PageResponse.from(
                jobRepository.findByActiveTrueAndTitleContainingIgnoreCase(
                        keyword.trim(),
                        pageable
                ),
                jobMapper::toSummaryResponse
        );
    }

}
