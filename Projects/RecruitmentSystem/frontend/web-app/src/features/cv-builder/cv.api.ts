import { apiClient } from '../../lib/api/client'
import type { ApiResponse } from '../../types/api/common'
import type { CandidateCv, CvTemplateId, SaveCvPayload } from './cv.types'

const unwrap = <T>(response: { data: ApiResponse<T> }) => response.data.data

export const cvApi = {
  list: async () => unwrap(await apiClient.get<ApiResponse<CandidateCv[]>>('/api/v1/cvs')),
  get: async (id: string) => unwrap(await apiClient.get<ApiResponse<CandidateCv>>(`/api/v1/cvs/${id}`)),
  create: async (payload: SaveCvPayload) => unwrap(await apiClient.post<ApiResponse<CandidateCv>>('/api/v1/cvs', payload)),
  createFromProfile: async (title: string, templateId: CvTemplateId) => unwrap(await apiClient.post<ApiResponse<CandidateCv>>('/api/v1/cvs/from-profile', { title, templateId })),
  update: async (id: string, payload: SaveCvPayload) => unwrap(await apiClient.put<ApiResponse<CandidateCv>>(`/api/v1/cvs/${id}`, payload)),
  remove: async (id: string) => { await apiClient.delete(`/api/v1/cvs/${id}`) },
  download: async (id: string) => (await apiClient.get<Blob>(`/api/v1/cvs/${id}/pdf`, { responseType: 'blob', headers: { Accept: 'application/pdf' } })).data,
}

export function saveBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename
  anchor.click()
  URL.revokeObjectURL(url)
}
