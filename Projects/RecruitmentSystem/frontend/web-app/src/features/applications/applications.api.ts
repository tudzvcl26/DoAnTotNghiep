import { apiClient } from '../../lib/api/client'
import type { ApiResponse } from '../../types/api/common'
import type { PageResponse } from '../../types/api/common'
import type { Application, ApplicationSummary, ApplyJobRequest } from '../../types/models/application'

export async function applyForJob(payload: ApplyJobRequest): Promise<Application> {
  const response = await apiClient.post<ApiResponse<Application>>('/api/v1/applications', payload)
  return response.data.data
}

export async function getMyApplications(): Promise<PageResponse<ApplicationSummary>> {
  const response = await apiClient.get<ApiResponse<PageResponse<ApplicationSummary>>>('/api/v1/applications/my', {
    params: { page: 0, size: 20, sort: 'appliedAt,desc' },
  })
  return response.data.data
}
