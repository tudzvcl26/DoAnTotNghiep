import { apiClient } from '../../lib/api/client'
import type { ApiResponse, SpringPage } from '../../types/api/common'
import type { ResumeAsset } from '../../types/models/resume'

const resumePath = (userId: string) => `/api/v1/users/${userId}/resumes`
const PDF_HEADER = [0x25, 0x50, 0x44, 0x46, 0x2d]

function startsWith(bytes: Uint8Array, signature: number[], offset = 0) {
  return signature.every((byte, index) => bytes[offset + index] === byte)
}

async function normalizePdfHeader(file: File): Promise<File> {
  if (file.type !== 'application/pdf') return file

  const prefix = new Uint8Array(await file.slice(0, 16).arrayBuffer())
  if (startsWith(prefix, PDF_HEADER)) return file

  const headerOffset = prefix.findIndex((_, index) => startsWith(prefix, PDF_HEADER, index))
  if (headerOffset <= 0) return file

  const leadingBytes = prefix.slice(0, headerOffset)
  const startsWithBom = startsWith(leadingBytes, [0xef, 0xbb, 0xbf])
  const whitespaceOffset = startsWithBom ? 3 : 0
  const containsOnlyWhitespace = leadingBytes
    .slice(whitespaceOffset)
    .every((byte) => [0x09, 0x0a, 0x0c, 0x0d, 0x20].includes(byte))

  if (!containsOnlyWhitespace) return file

  return new File([file.slice(headerOffset)], file.name, {
    type: file.type,
    lastModified: file.lastModified,
  })
}

export async function getResumes(userId: string): Promise<SpringPage<ResumeAsset>> {
  const response = await apiClient.get<ApiResponse<SpringPage<ResumeAsset>>>(resumePath(userId), {
    params: { page: 0, size: 100, sort: 'createdAt,desc' },
  })
  return response.data.data
}

export async function uploadResume(userId: string, file: File): Promise<ResumeAsset> {
  const normalizedFile = await normalizePdfHeader(file)
  const formData = new FormData()
  formData.append('file', normalizedFile, normalizedFile.name)
  const response = await apiClient.post<ApiResponse<ResumeAsset>>(resumePath(userId), formData)
  return response.data.data
}

export async function downloadResume(userId: string, assetId: string): Promise<Blob> {
  const response = await apiClient.get<Blob>(`${resumePath(userId)}/${assetId}/download`, {
    responseType: 'blob',
  })
  return response.data
}

export async function deleteResume(userId: string, assetId: string): Promise<void> {
  await apiClient.delete(`${resumePath(userId)}/${assetId}`)
}
