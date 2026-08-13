import { apiClient } from '../../lib/api/client'
import type { ApiResponse } from '../../types/api/common'
import type { CandidateProfile } from '../../types/models/profile'
import type { ResumeAsset } from '../../types/models/resume'

export async function getCandidateProfile(): Promise<CandidateProfile> {
  const response = await apiClient.get<ApiResponse<CandidateProfile>>('/api/v1/profiles/me')
  return response.data.data
}

export async function getCurrentResume(userId: string): Promise<ResumeAsset> {
  const response = await apiClient.get<ApiResponse<ResumeAsset>>(`/api/v1/users/${userId}/resumes/current`)
  return response.data.data
}
