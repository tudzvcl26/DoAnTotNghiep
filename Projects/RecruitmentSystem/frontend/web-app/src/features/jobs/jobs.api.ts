import { apiClient } from '../../lib/api/client'
import type { ApiResponse, PageResponse } from '../../types/api/common'
import type { EmploymentType, ExperienceLevel, JobCategory, JobDetail, JobSummary } from '../../types/models/job'

export type JobsQueryParams = {
  keyword: string
  location?: string
  categoryId?: string
  companyId?: string
  skillId?: string
  employmentType?: EmploymentType
  experienceLevel?: ExperienceLevel
  remoteAllowed?: boolean
  minSalary?: number
  maxSalary?: number
  page: number
  size: number
  sort: 'publishedAt,desc' | 'publishedAt,asc'
}

export async function getJobs(params: JobsQueryParams): Promise<PageResponse<JobSummary>> {
  const keyword = params.keyword.trim()
  const location = params.location?.trim()
  const response = await apiClient.get<ApiResponse<PageResponse<JobSummary>>>('/api/v1/jobs/public-search', {
    params: {
      ...(keyword ? { keyword } : {}),
      ...(location ? { location } : {}),
      ...(params.categoryId ? { categoryId: params.categoryId } : {}),
      ...(params.companyId ? { companyId: params.companyId } : {}),
      ...(params.skillId ? { skillId: params.skillId } : {}),
      ...(params.employmentType ? { employmentType: params.employmentType } : {}),
      ...(params.experienceLevel ? { experienceLevel: params.experienceLevel } : {}),
      ...(typeof params.remoteAllowed === 'boolean' ? { remoteAllowed: params.remoteAllowed } : {}),
      ...(typeof params.minSalary === 'number' ? { minSalary: params.minSalary } : {}),
      ...(typeof params.maxSalary === 'number' ? { maxSalary: params.maxSalary } : {}),
      page: params.page,
      size: params.size,
      sort: params.sort,
    },
  })
  return response.data.data
}

export async function getFeaturedJobs(): Promise<PageResponse<JobSummary>> {
  return getJobs({ keyword: '', page: 0, size: 6, sort: 'publishedAt,desc' })
}

export async function getPublicJobCategories(): Promise<JobCategory[]> {
  const response = await apiClient.get<ApiResponse<PageResponse<JobCategory>>>('/api/v1/job-categories', {
    params: { page: 0, size: 100, sortBy: 'displayOrder', direction: 'asc' },
  })
  return response.data.data.content.filter((category) => category.active)
}

export async function getJobById(id: string): Promise<JobDetail> {
  const response = await apiClient.get<ApiResponse<JobDetail>>(`/api/v1/jobs/${id}`)
  return response.data.data
}
