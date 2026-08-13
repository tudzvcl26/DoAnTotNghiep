import { apiClient } from '../../lib/api/client'
import type { ApiResponse, PageResponse, SpringPage } from '../../types/api/common'
import type { ApplicationSummary } from '../../types/models/application'
import type { Company, CreateCompanyRequest, UpdateCompanyRequest } from '../../types/models/company'
import type { JobSummary } from '../../types/models/job'

const PAGE_SIZE = 100
export const employerCompanyKey = (ownerId: string) => ['employer-companies', ownerId] as const

export async function getEmployerCompanies(ownerId: string): Promise<Company[]> {
  const owned: Company[] = []
  let page = 0
  let totalPages = 1
  do {
    const response = await apiClient.get<SpringPage<Company>>('/api/v1/companies', {
      params: { page, size: PAGE_SIZE, sort: 'createdAt,desc' },
    })
    owned.push(...response.data.content.filter((company) => company.ownerId === ownerId))
    totalPages = response.data.totalPages
    page += 1
  } while (page < totalPages)
  return owned
}

export async function createEmployerCompany(request: CreateCompanyRequest): Promise<Company> {
  const response = await apiClient.post<Company>('/api/v1/companies', request)
  return response.data
}

export async function updateEmployerCompany(companyId: string, request: UpdateCompanyRequest): Promise<Company> {
  const response = await apiClient.put<Company>(`/api/v1/companies/${companyId}`, request)
  return response.data
}

export async function getPublishedCompanyJobs(companyIds: string[]): Promise<JobSummary[]> {
  if (companyIds.length === 0) return []
  const owned = new Set(companyIds)
  const jobs: JobSummary[] = []
  let page = 0
  let totalPages = 1
  do {
    const response = await apiClient.get<ApiResponse<PageResponse<JobSummary>>>('/api/v1/jobs', {
      params: { page, size: PAGE_SIZE, sort: 'publishedAt,desc' },
    })
    jobs.push(...response.data.data.content.filter((job) => owned.has(job.companyId)))
    totalPages = response.data.data.totalPages
    page += 1
  } while (page < totalPages)
  return jobs
}

export type PublishedJobApplications = {
  total: number
  recent: ApplicationSummary[]
}

export async function getPublishedJobApplications(jobs: JobSummary[]): Promise<PublishedJobApplications> {
  if (jobs.length === 0) return { total: 0, recent: [] }
  const pages = await Promise.all(jobs.map(async (job) => {
    const response = await apiClient.get<ApiResponse<PageResponse<ApplicationSummary>>>(`/api/v1/jobs/${job.id}/applications`, {
      params: { page: 0, size: 5, sort: 'appliedAt,desc' },
    })
    return response.data.data
  }))
  return {
    total: pages.reduce((sum, page) => sum + page.totalElements, 0),
    recent: pages.flatMap((page) => page.content).sort((a, b) => Date.parse(b.appliedAt) - Date.parse(a.appliedAt)).slice(0, 5),
  }
}
