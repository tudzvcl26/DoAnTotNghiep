package com.recruitment.recruitmentservice.service.impl;

import com.recruitment.recruitmentservice.client.CompanyClient;
import com.recruitment.recruitmentservice.client.CompanyClientDto;
import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.job.CreateJobRequest;
import com.recruitment.recruitmentservice.dto.job.JobResponse;
import com.recruitment.recruitmentservice.dto.job.JobSummaryResponse;
import com.recruitment.recruitmentservice.dto.job.UpdateJobRequest;
import com.recruitment.recruitmentservice.entity.Job;
import com.recruitment.recruitmentservice.entity.JobCategory;
import com.recruitment.recruitmentservice.entity.enums.JobStatus;
import com.recruitment.recruitmentservice.exception.BusinessException;
import com.recruitment.recruitmentservice.exception.ErrorCode;
import com.recruitment.recruitmentservice.mapper.JobMapper;
import com.recruitment.recruitmentservice.repository.JobCategoryRepository;
import com.recruitment.recruitmentservice.repository.JobRepository;
import com.recruitment.recruitmentservice.security.CurrentUser;
import com.recruitment.recruitmentservice.security.SecurityUtils;
import com.recruitment.recruitmentservice.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    private final JobCategoryRepository jobCategoryRepository;

    private final JobMapper jobMapper;

    private final CompanyClient companyClient;

    @Override
    public JobResponse create(CreateJobRequest request) {

        assertCompanyOwner(request.getCompanyId());

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

        assertJobOwner(job);

        if (!Objects.equals(job.getCompanyId(), request.getCompanyId())) {
            assertCompanyOwner(request.getCompanyId());
        }

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
    public JobResponse publish(UUID id) {

        Job job = findActiveJob(id);

        assertJobOwner(job);

        if (job.getStatus() == JobStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.JOB_ALREADY_PUBLISHED);
        }

        if (job.getStatus() != JobStatus.DRAFT) {
            throw new BusinessException(ErrorCode.INVALID_JOB_STATUS);
        }

        job.setStatus(JobStatus.PUBLISHED);
        job.setPublishedAt(LocalDateTime.now());

        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Override
    public JobResponse close(UUID id) {

        Job job = findActiveJob(id);

        assertJobOwner(job);

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new BusinessException(ErrorCode.JOB_ALREADY_CLOSED);
        }

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.INVALID_JOB_STATUS);
        }

        job.setStatus(JobStatus.CLOSED);

        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Override
    public void delete(UUID id) {

        Job job = jobRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.JOB_NOT_FOUND
                        )
                );

        assertJobOwner(job);

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

        if (job.getStatus() != JobStatus.PUBLISHED
                && !isCurrentUserAdmin()
                && !isCurrentUserCompanyOwner(job.getCompanyId())) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }

        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobSummaryResponse> getAll(
            Pageable pageable
    ) {

        return PageResponse.from(
                isCurrentUserAdmin()
                        ? jobRepository.findByActiveTrue(pageable)
                        : jobRepository.findByActiveTrueAndStatus(JobStatus.PUBLISHED, pageable),
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
                    isCurrentUserAdmin()
                            ? jobRepository.findByActiveTrue(pageable)
                            : jobRepository.findByActiveTrueAndStatus(JobStatus.PUBLISHED, pageable),
                    jobMapper::toSummaryResponse
            );
        }

        return PageResponse.from(
                isCurrentUserAdmin()
                        ? jobRepository.findByActiveTrueAndTitleContainingIgnoreCase(
                                keyword.trim(),
                                pageable
                        )
                        : jobRepository.findByActiveTrueAndStatusAndTitleContainingIgnoreCase(
                                JobStatus.PUBLISHED,
                                keyword.trim(),
                                pageable
                        ),
                jobMapper::toSummaryResponse
        );
    }

    private Job findActiveJob(UUID id) {

        return jobRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.JOB_NOT_FOUND
                        )
                );
    }

    private boolean isCurrentUserAdmin() {

        CurrentUser currentUser = SecurityUtils.getCurrentUser();

        return currentUser != null && currentUser.isAdmin();
    }

    private boolean isCurrentUserCompanyOwner(UUID companyId) {
        CurrentUser currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null || companyId == null) {
            return false;
        }
        return companyClient.getCompanyById(companyId)
                .map(company -> currentUser.getUserId().equals(company.getOwnerId()))
                .orElse(false);
    }

    private void assertCompanyOwner(UUID companyId) {

        CurrentUser currentUser = SecurityUtils.getCurrentUser();

        if (currentUser == null || currentUser.getUserId() == null) {
            throw new AccessDeniedException("User is not authenticated.");
        }

        if (currentUser.isAdmin()) {
            return;
        }

        if (companyId == null) {
            throw new AccessDeniedException("Company ID is required.");
        }

        CompanyClientDto company = companyClient.getCompanyById(companyId)
                .orElseThrow(() -> new AccessDeniedException("Company not found or inaccessible."));

        if (company.getOwnerId() == null || !company.getOwnerId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You do not have permission to manage jobs for this company.");
        }

    }

    private void assertJobOwner(Job job) {

        if (job.getCompanyId() == null) {
            throw new AccessDeniedException("Job is not associated with any company.");
        }

        assertCompanyOwner(job.getCompanyId());

    }

}
