export const COMPANY_TYPES = ['PRIVATE', 'PUBLIC', 'STARTUP', 'NON_PROFIT', 'GOVERNMENT', 'OTHER'] as const
export const COMPANY_SIZES = ['MICRO', 'SMALL', 'MEDIUM', 'LARGE', 'ENTERPRISE'] as const
export type CompanyType = typeof COMPANY_TYPES[number]
export type CompanySize = typeof COMPANY_SIZES[number]

export type Company = {
  id: string
  ownerId: string
  name: string
  slug: string
  description: string | null
  website: string | null
  email: string | null
  phone: string | null
  taxCode: string | null
  companyType: CompanyType | null
  companySize: CompanySize | null
  verificationStatus: 'PENDING' | 'VERIFIED' | 'REJECTED' | null
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
  logoUrl: string | null
  bannerUrl: string | null
  createdAt: string
  updatedAt: string
}

export type CreateCompanyRequest = {
  name: string
  description?: string
  website?: string
  email?: string
  phone?: string
  taxCode?: string
  companyType?: CompanyType
  companySize?: CompanySize
  logoUrl?: string
  bannerUrl?: string
}

export type UpdateCompanyRequest = Omit<CreateCompanyRequest, 'taxCode'>
