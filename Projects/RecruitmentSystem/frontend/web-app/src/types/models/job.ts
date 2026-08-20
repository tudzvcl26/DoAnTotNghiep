export const JOB_STATUSES = ['DRAFT', 'PUBLISHED', 'CLOSED', 'EXPIRED'] as const
export const EMPLOYMENT_TYPES = ['FULL_TIME', 'PART_TIME', 'INTERNSHIP', 'FREELANCE', 'CONTRACT', 'TEMPORARY'] as const
export const EXPERIENCE_LEVELS = ['NO_EXPERIENCE', 'FRESHER', 'JUNIOR', 'MIDDLE', 'SENIOR', 'LEADER', 'MANAGER'] as const

export type JobStatus = (typeof JOB_STATUSES)[number]
export type EmploymentType = (typeof EMPLOYMENT_TYPES)[number]
export type ExperienceLevel = (typeof EXPERIENCE_LEVELS)[number]

export type JobSummary = {
  id: string
  title: string
  jobCode: string
  salaryMin: number | null
  salaryMax: number | null
  currency: string | null
  employmentType: EmploymentType | null
  experienceLevel: ExperienceLevel | null
  status: JobStatus
  remoteAllowed: boolean
  location: string | null
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

export type JobMutationRequest = {
  title: string
  jobCode: string
  description?: string
  requirements?: string
  responsibilities?: string
  salaryMin?: number
  salaryMax?: number
  currency?: string
  employmentType: EmploymentType
  experienceLevel: ExperienceLevel
  quantity?: number
  applicationDeadline?: string
  remoteAllowed?: boolean
  active?: boolean
  companyId: string
  categoryId: string
}

export type JobCategory = {
  id: string
  name: string
  slug: string
  description: string | null
  icon: string | null
  displayOrder: number
  active: boolean
  parentId: string | null
  parentName: string | null
  createdAt: string
  updatedAt: string
}
