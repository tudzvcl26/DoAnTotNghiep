export type JobSummary = {
  id: string
  title: string
  jobCode: string
  salaryMin: number | null
  salaryMax: number | null
  currency: string | null
  employmentType: string | null
  experienceLevel: string | null
  status: string
  remoteAllowed: boolean
  quantity: number | null
  companyId: string
  categoryId: string | null
  categoryName: string | null
  categorySlug: string | null
  applicationDeadline: string | null
  publishedAt: string | null
  active: boolean
}

export type JobDetail = JobSummary & {
  description: string | null
  requirements: string | null
  responsibilities: string | null
  expiredAt: string | null
  createdAt: string
  updatedAt: string
}
