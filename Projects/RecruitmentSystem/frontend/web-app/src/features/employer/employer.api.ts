import { apiClient } from '../../lib/api/client'
import type { ApiResponse, PageResponse } from '../../types/api/common'
import type { Application, ApplicationStatus, ApplicationSummary, UpdateApplicationStatusRequest } from '../../types/models/application'
import type { Company, CreateCompanyRequest, UpdateCompanyRequest } from '../../types/models/company'
import type { JobCategory, JobDetail, JobMutationRequest, JobStatus, JobSummary } from '../../types/models/job'

const PAGE_SIZE = 100
export const employerCompanyKey = (ownerId: string) => ['employer-companies', ownerId] as const
export const employerJobsKey = ['employer-jobs'] as const
export const employerJobKey = (jobId: string) => ['employer-job', jobId] as const
export const employerJobStatisticsKey = ['employer-job-statistics'] as const
export const employerPublishedJobsKey = ['employer-published-jobs'] as const
export const employerApplicationsKey = ['employer-applications'] as const
export const employerApplicationKey = (applicationId: string) => ['employer-application', applicationId] as const
export const employerApplicationStatisticsKey = ['employer-application-statistics'] as const
export const employerApplicationSummaryKey = ['employer-application-summary'] as const

export async function getEmployerCompanies(ownerId: string): Promise<Company[]> {
  const response = await apiClient.get<Company[]>(`/api/v1/companies/owner/${ownerId}`)
  return response.data
}

export async function createEmployerCompany(request: CreateCompanyRequest): Promise<Company> {
  const response = await apiClient.post<Company>('/api/v1/companies', request)
  return response.data
}

export async function updateEmployerCompany(companyId: string, request: UpdateCompanyRequest): Promise<Company> {
  const response = await apiClient.put<Company>(`/api/v1/companies/${companyId}`, request)
  return response.data
}

export type EmployerJobsParams = { page: number; size: number; sort: string; keyword?: string; status?: JobStatus; companyId?: string }

export async function getEmployerJobs(params: EmployerJobsParams): Promise<PageResponse<JobSummary>> {
  const response = await apiClient.get<ApiResponse<PageResponse<JobSummary>>>('/api/v1/jobs/employer', {
    params: {
      page: params.page, size: params.size, sort: params.sort,
      ...(params.keyword ? { keyword: params.keyword } : {}),
      ...(params.status ? { status: params.status } : {}),
      ...(params.companyId ? { companyId: params.companyId } : {}),
    },
  })
  return response.data.data
}

export type EmployerJobStatistics = { total: number; published: number; draft: number; closed: number }
export type EmployerApplicationStatistics = { total: number; applied: number; screening: number; interview: number; offer: number; hired: number; rejected: number; withdrawn: number }

export async function getEmployerJobStatistics(): Promise<EmployerJobStatistics> {
  const response = await apiClient.get<ApiResponse<EmployerJobStatistics>>('/api/v1/jobs/employer/statistics')
  return response.data.data
}

export async function getEmployerApplicationStatistics(): Promise<EmployerApplicationStatistics> {
  const response = await apiClient.get<ApiResponse<EmployerApplicationStatistics>>('/api/v1/applications/employer/statistics')
  return response.data.data
}

export async function getEmployerJob(jobId: string): Promise<JobDetail> {
  const response = await apiClient.get<ApiResponse<JobDetail>>(`/api/v1/jobs/employer/${jobId}`)
  return response.data.data
}

export async function getJobCategories(): Promise<JobCategory[]> {
  const response = await apiClient.get<ApiResponse<PageResponse<JobCategory>>>('/api/v1/job-categories', {
    params: { page: 0, size: PAGE_SIZE, sortBy: 'displayOrder', direction: 'asc' },
  })
  return response.data.data.content.filter((category) => category.active)
}

export async function createEmployerJob(request: JobMutationRequest): Promise<JobDetail> {
  const response = await apiClient.post<ApiResponse<JobDetail>>('/api/v1/jobs', request)
  return response.data.data
}

export async function updateEmployerJob(jobId: string, request: JobMutationRequest): Promise<JobDetail> {
  const response = await apiClient.put<ApiResponse<JobDetail>>(`/api/v1/jobs/${jobId}`, request)
  return response.data.data
}

export async function publishEmployerJob(jobId: string): Promise<JobDetail> {
  const response = await apiClient.patch<ApiResponse<JobDetail>>(`/api/v1/jobs/${jobId}/publish`)
  return response.data.data
}

export async function closeEmployerJob(jobId: string): Promise<JobDetail> {
  const response = await apiClient.patch<ApiResponse<JobDetail>>(`/api/v1/jobs/${jobId}/close`)
  return response.data.data
}

export async function deleteEmployerJob(jobId: string): Promise<void> {
  await apiClient.delete(`/api/v1/jobs/${jobId}`)
}

export type EmployerApplicationsParams = { page: number; size: number; sort: string; status?: ApplicationStatus; jobId?: string }

export async function getEmployerApplications(params: EmployerApplicationsParams): Promise<PageResponse<ApplicationSummary>> {
  const response = await apiClient.get<ApiResponse<PageResponse<ApplicationSummary>>>('/api/v1/applications/employer', {
    params: { page: params.page, size: params.size, sort: params.sort, ...(params.status ? { status: params.status } : {}), ...(params.jobId ? { jobId: params.jobId } : {}) },
  })
  return response.data.data
}

export async function getEmployerApplication(applicationId: string): Promise<Application> {
  const response = await apiClient.get<ApiResponse<Application>>(`/api/v1/applications/${applicationId}`)
  return response.data.data
}

export async function updateEmployerApplicationStatus(applicationId: string, request: UpdateApplicationStatusRequest): Promise<Application> {
  const response = await apiClient.patch<ApiResponse<Application>>(`/api/v1/applications/${applicationId}/status`, request)
  return response.data.data
}

export async function downloadEmployerApplicationResume(applicationId: string): Promise<Blob> {
  const response = await apiClient.get<Blob>(`/api/v1/applications/${applicationId}/resume`, { responseType: 'blob' })
  return response.data
}

export type PublishedJobApplications = {
  total: number
  recent: ApplicationSummary[]
}

export async function getEmployerApplicationSummary(): Promise<PublishedJobApplications> {
  const page = await getEmployerApplications({ page: 0, size: 5, sort: 'appliedAt,desc' })
  return { total: page.totalElements, recent: page.content }
}
