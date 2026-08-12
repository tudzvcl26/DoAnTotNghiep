import { apiClient } from '../../lib/api/client'
import type { ApiResponse, PageResponse } from '../../types/api/common'
import type { JobDetail, JobSummary } from '../../types/models/job'

export type JobsQueryParams = {
  keyword: string
  page: number
  size: number
  sort: 'publishedAt,desc' | 'publishedAt,asc'
}

export async function getJobs(params: JobsQueryParams): Promise<PageResponse<JobSummary>> {
  const keyword = params.keyword.trim()
  const response = await apiClient.get<ApiResponse<PageResponse<JobSummary>>>(keyword ? '/api/v1/jobs/search' : '/api/v1/jobs', {
    params: {
      ...(keyword ? { keyword } : {}),
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

export async function getJobById(id: string): Promise<JobDetail> {
  const response = await apiClient.get<ApiResponse<JobDetail>>(`/api/v1/jobs/${id}`)
  return response.data.data
}
