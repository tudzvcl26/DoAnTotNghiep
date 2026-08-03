package com.recruitment.application.service.impl;

import com.recruitment.application.client.CompanyClient;
import com.recruitment.application.client.CompanyClientDto;
import com.recruitment.application.client.JobClient;
import com.recruitment.application.client.JobClientDto;
import com.recruitment.application.client.UserClient;
import com.recruitment.application.client.UserClientDto;
import com.recruitment.application.common.PageResponse;
import com.recruitment.application.dto.request.ApplyJobRequest;
import com.recruitment.application.dto.request.UpdateApplicationStatusRequest;
import com.recruitment.application.dto.request.WithdrawApplicationRequest;
import com.recruitment.application.dto.response.ApplicationResponse;
import com.recruitment.application.dto.response.ApplicationSummaryResponse;
import com.recruitment.application.entity.Application;
import com.recruitment.application.entity.ApplicationStatusHistory;
import com.recruitment.application.entity.JobSnapshot;
import com.recruitment.application.entity.ResumeSnapshot;
import com.recruitment.application.entity.enums.ApplicationStatus;
import com.recruitment.application.exception.BusinessException;
import com.recruitment.application.exception.ErrorCode;
import com.recruitment.application.exception.ResourceNotFoundException;
import com.recruitment.application.mapper.ApplicationMapper;
import com.recruitment.application.mapper.ApplicationStatusHistoryMapper;
import com.recruitment.application.mapper.JobSnapshotMapper;
import com.recruitment.application.mapper.ResumeSnapshotMapper;
import com.recruitment.application.repository.ApplicationRepository;
import com.recruitment.application.repository.ApplicationStatusHistoryRepository;
import com.recruitment.application.repository.JobSnapshotRepository;
import com.recruitment.application.repository.ResumeSnapshotRepository;
import com.recruitment.application.security.CurrentUser;
import com.recruitment.application.security.SecurityUtils;
import com.recruitment.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final ResumeSnapshotRepository resumeSnapshotRepository;
    private final JobSnapshotRepository jobSnapshotRepository;

    private final ApplicationMapper applicationMapper;
    private final ApplicationStatusHistoryMapper statusHistoryMapper;
    private final ResumeSnapshotMapper resumeSnapshotMapper;
    private final JobSnapshotMapper jobSnapshotMapper;

    private final JobClient jobClient;
    private final UserClient userClient;
    private final CompanyClient companyClient;

    @Override
    public ApplicationResponse apply(ApplyJobRequest request) {
        CurrentUser currentUser = getCurrentAuthenticatedUser();
        UUID candidateId = currentUser.getUserId();

        JobClientDto job = jobClient.getJobById(request.getJobId())
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));

        if (!"PUBLISHED".equalsIgnoreCase(job.getStatus())) {
            throw new BusinessException(ErrorCode.JOB_NOT_ACCEPTING_APPLICATIONS);
        }

        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.APPLICATION_DEADLINE_PASSED);
        }

        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, request.getJobId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_APPLICATION);
        }

        String bearerToken = SecurityUtils.getBearerToken();
        UserClientDto userProfile = userClient.getCandidateProfile(candidateId, bearerToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.CANDIDATE_PROFILE_NOT_FOUND));

        Application application = new Application();
        application.setCandidateId(candidateId);
        application.setCompanyId(job.getCompanyId());
        application.setJobId(job.getId());
        application.setStatus(ApplicationStatus.APPLIED);
        application.setCoverLetter(request.getCoverLetter());
        application.setAppliedAt(LocalDateTime.now());
        application.setActive(true);

        Application savedApplication = applicationRepository.save(application);

        ResumeSnapshot resumeSnapshot = new ResumeSnapshot();
        resumeSnapshot.setApplicationId(savedApplication.getId());
        resumeSnapshot.setCandidateId(candidateId);
        resumeSnapshot.setSnapshotData(userProfile.getRawJsonData() != null ? userProfile.getRawJsonData() : "{}");
        resumeSnapshot.setResumeVersion("v1.0");
        ResumeSnapshot savedResumeSnapshot = resumeSnapshotRepository.save(resumeSnapshot);

        JobSnapshot jobSnapshot = new JobSnapshot();
        jobSnapshot.setApplicationId(savedApplication.getId());
        jobSnapshot.setJobId(job.getId());
        jobSnapshot.setSnapshotData(job.getRawJsonData() != null ? job.getRawJsonData() : "{}");
        jobSnapshot.setJobVersion("v1.0");
        JobSnapshot savedJobSnapshot = jobSnapshotRepository.save(jobSnapshot);

        savedApplication.setResumeSnapshotId(savedResumeSnapshot.getId());
        savedApplication.setJobSnapshotId(savedJobSnapshot.getId());
        savedApplication = applicationRepository.save(savedApplication);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplicationId(savedApplication.getId());
        history.setFromStatus(null);
        history.setToStatus(ApplicationStatus.APPLIED);
        history.setChangedBy(candidateId);
        history.setChangedAt(savedApplication.getAppliedAt());
        statusHistoryRepository.save(history);

        return buildApplicationResponse(savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApplicationSummaryResponse> getMyApplications(Pageable pageable) {
        CurrentUser currentUser = getCurrentAuthenticatedUser();
        UUID candidateId = currentUser.getUserId();

        return PageResponse.from(
                applicationRepository.findByCandidateIdAndActiveTrue(candidateId, pageable),
                applicationMapper::toSummaryResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getById(UUID id) {
        CurrentUser currentUser = getCurrentAuthenticatedUser();

        Application application = applicationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.APPLICATION_NOT_FOUND));

        assertCanViewApplication(application, currentUser);

        return buildApplicationResponse(application);
    }

    @Override
    public ApplicationResponse withdraw(UUID id, WithdrawApplicationRequest request) {
        CurrentUser currentUser = getCurrentAuthenticatedUser();

        Application application = applicationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.APPLICATION_NOT_FOUND));

        assertCandidateOwner(application, currentUser);

        if (isTerminalStatus(application.getStatus())) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_TERMINAL);
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.WITHDRAWN);
        Application updatedApplication = applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplicationId(updatedApplication.getId());
        history.setFromStatus(oldStatus);
        history.setToStatus(ApplicationStatus.WITHDRAWN);
        history.setReasonCode(request != null ? request.getReasonCode() : null);
        history.setReasonDetail(request != null ? request.getReasonDetail() : null);
        history.setChangedBy(currentUser.getUserId());
        history.setChangedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);

        return buildApplicationResponse(updatedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApplicationSummaryResponse> getJobApplications(UUID jobId, ApplicationStatus status, Pageable pageable) {
        CurrentUser currentUser = getCurrentAuthenticatedUser();

        JobClientDto job = jobClient.getJobById(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));

        assertCompanyOwner(job.getCompanyId(), currentUser);

        if (status != null) {
            return PageResponse.from(
                    applicationRepository.findByJobIdAndStatusAndActiveTrue(jobId, status, pageable),
                    applicationMapper::toSummaryResponse
            );
        }

        return PageResponse.from(
                applicationRepository.findByJobIdAndActiveTrue(jobId, pageable),
                applicationMapper::toSummaryResponse
        );
    }

    @Override
    public ApplicationResponse updateStatus(UUID id, UpdateApplicationStatusRequest request) {
        CurrentUser currentUser = getCurrentAuthenticatedUser();

        Application application = applicationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.APPLICATION_NOT_FOUND));

        assertCompanyOwner(application.getCompanyId(), currentUser);

        if (request.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.INVALID_APPLICATION_STATUS_TRANSITION);
        }

        if (isTerminalStatus(application.getStatus())) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_TERMINAL);
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(request.getStatus());
        Application updatedApplication = applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplicationId(updatedApplication.getId());
        history.setFromStatus(oldStatus);
        history.setToStatus(request.getStatus());
        history.setReasonCode(request.getReasonCode());
        history.setReasonDetail(request.getReasonDetail());
        history.setChangedBy(currentUser.getUserId());
        history.setChangedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);

        return buildApplicationResponse(updatedApplication);
    }

    private CurrentUser getCurrentAuthenticatedUser() {
        CurrentUser currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new AccessDeniedException("User is not authenticated.");
        }
        return currentUser;
    }

    private void assertCandidateOwner(
            Application application,
            CurrentUser currentUser
    ) {
        if (currentUser.isAdmin()) {
            return;
        }
        if (!currentUser.getUserId().equals(application.getCandidateId())) {
            throw new AccessDeniedException("You do not have permission to modify this application.");
        }
    }

    private void assertCompanyOwner(
            UUID companyId,
            CurrentUser currentUser
    ) {
        if (currentUser.isAdmin()) {
            return;
        }

        CompanyClientDto company = companyClient.getCompanyById(companyId)
                .orElseThrow(() -> new AccessDeniedException("Company not found or inaccessible."));

        if (company.getOwnerId() == null || !company.getOwnerId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You do not have permission to manage applications for this company.");
        }
    }

    private void assertCanViewApplication(
            Application application,
            CurrentUser currentUser
    ) {
        if (currentUser.isAdmin()) {
            return;
        }

        if (currentUser.getUserId().equals(application.getCandidateId())) {
            return;
        }

        CompanyClientDto company = companyClient.getCompanyById(application.getCompanyId()).orElse(null);
        if (company != null && currentUser.getUserId().equals(company.getOwnerId())) {
            return;
        }

        throw new AccessDeniedException("You do not have permission to view this application.");
    }

    private boolean isTerminalStatus(ApplicationStatus status) {
        return status == ApplicationStatus.HIRED
                || status == ApplicationStatus.REJECTED
                || status == ApplicationStatus.WITHDRAWN;
    }

    private ApplicationResponse buildApplicationResponse(Application application) {
        ApplicationResponse response = applicationMapper.toResponse(application);

        resumeSnapshotRepository.findByApplicationId(application.getId())
                .ifPresent(snapshot -> response.setResumeSnapshot(resumeSnapshotMapper.toResponse(snapshot)));

        jobSnapshotRepository.findByApplicationId(application.getId())
                .ifPresent(snapshot -> response.setJobSnapshot(jobSnapshotMapper.toResponse(snapshot)));

        List<ApplicationStatusHistory> histories = statusHistoryRepository.findByApplicationIdOrderByChangedAtAsc(application.getId());
        response.setStatusHistory(statusHistoryMapper.toResponseList(histories));

        return response;
    }

}
