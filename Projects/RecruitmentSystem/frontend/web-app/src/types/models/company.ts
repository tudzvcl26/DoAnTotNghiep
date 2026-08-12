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
  companyType: string | null
  companySize: string | null
  verificationStatus: string | null
  status: string
  logoUrl: string | null
  bannerUrl: string | null
  createdAt: string
  updatedAt: string
}
