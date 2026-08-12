import { apiClient } from '../../lib/api/client'
import type { SpringPage } from '../../types/api/common'
import type { Company } from '../../types/models/company'

export type CompaniesQueryParams = {
  keyword: string
  page: number
  size: number
}

export async function listCompanies(params: CompaniesQueryParams): Promise<SpringPage<Company>> {
  const endpoint = params.keyword ? '/api/v1/companies/search' : '/api/v1/companies'
  const response = await apiClient.get<SpringPage<Company>>(endpoint, {
    params: {
      ...(params.keyword ? { keyword: params.keyword } : {}),
      page: params.page,
      size: params.size,
      sort: 'createdAt,desc',
    },
  })
  return response.data
}

export async function getFeaturedCompanies(): Promise<SpringPage<Company>> {
  return listCompanies({ keyword: '', page: 0, size: 6 })
}

export async function getCompanyById(id: string): Promise<Company> {
  const response = await apiClient.get<Company>(`/api/v1/companies/${id}`)
  return response.data
}
