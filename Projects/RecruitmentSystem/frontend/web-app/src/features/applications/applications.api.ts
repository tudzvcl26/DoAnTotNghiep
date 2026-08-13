import { apiClient } from '../../lib/api/client'
import type { ApiResponse } from '../../types/api/common'
import type { PageResponse } from '../../types/api/common'
import type { Application, ApplicationSummary, ApplyJobRequest, WithdrawApplicationRequest } from '../../types/models/application'

export type MyApplicationsParams = {
  page: number
  size: number
}

export async function applyForJob(payload: ApplyJobRequest): Promise<Application> {
  const response = await apiClient.post<ApiResponse<Application>>('/api/v1/applications', payload)
  return response.data.data
}

export async function getMyApplications(params: MyApplicationsParams = { page: 0, size: 20 }): Promise<PageResponse<ApplicationSummary>> {
  const response = await apiClient.get<ApiResponse<PageResponse<ApplicationSummary>>>('/api/v1/applications/my', {
    params: { ...params, sort: 'appliedAt,desc' },
  })
  return response.data.data
}

export async function findMyApplicationForJob(jobId: string): Promise<ApplicationSummary | null> {
  const size = 30
  let page = 0
  while (true) {
    const result = await getMyApplications({ page, size })
    const match = result.content.find((application) => application.jobId === jobId)
    if (match) return match
    if (!result.hasNext) return null
    page += 1
  }
}

export async function getApplicationById(id: string): Promise<Application> {
  const response = await apiClient.get<ApiResponse<Application>>(`/api/v1/applications/${id}`)
  return response.data.data
}

export async function withdrawApplication(id: string, payload?: WithdrawApplicationRequest): Promise<Application> {
  const response = await apiClient.patch<ApiResponse<Application>>(`/api/v1/applications/${id}/withdraw`, payload)
  return response.data.data
}
