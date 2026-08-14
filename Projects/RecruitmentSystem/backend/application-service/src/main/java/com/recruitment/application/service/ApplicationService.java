package com.recruitment.application.service;

import com.recruitment.application.common.PageResponse;
import com.recruitment.application.dto.request.ApplyJobRequest;
import com.recruitment.application.dto.request.UpdateApplicationStatusRequest;
import com.recruitment.application.dto.request.WithdrawApplicationRequest;
import com.recruitment.application.dto.response.ApplicationResponse;
import com.recruitment.application.dto.response.ApplicationSummaryResponse;
import com.recruitment.application.dto.response.ApplicationResumeDownload;
import com.recruitment.application.dto.response.EmployerApplicationStatisticsResponse;
import com.recruitment.application.entity.enums.ApplicationStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ApplicationService {

    ApplicationResponse apply(ApplyJobRequest request);

    PageResponse<ApplicationSummaryResponse> getMyApplications(Pageable pageable);

    ApplicationResponse getById(UUID id);

    ApplicationResponse withdraw(UUID id, WithdrawApplicationRequest request);

    PageResponse<ApplicationSummaryResponse> getJobApplications(UUID jobId, ApplicationStatus status, Pageable pageable);

    PageResponse<ApplicationSummaryResponse> getEmployerApplications(ApplicationStatus status, UUID jobId, Pageable pageable);

    EmployerApplicationStatisticsResponse getEmployerStatistics();

    ApplicationResumeDownload downloadResume(UUID applicationId);

    ApplicationResponse updateStatus(UUID id, UpdateApplicationStatusRequest request);

    PageResponse<ApplicationSummaryResponse> getAdminApplications(ApplicationStatus status, UUID jobId,
                                                                   UUID companyId, UUID candidateId,
                                                                   Pageable pageable);

}
