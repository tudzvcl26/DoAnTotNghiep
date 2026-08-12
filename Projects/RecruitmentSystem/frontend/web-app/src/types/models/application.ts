export type ApplyJobRequest = {
  jobId: string
  coverLetter?: string
}

export type ApplicationStatus = 'APPLIED' | 'SCREENING' | 'INTERVIEW' | 'OFFER' | 'HIRED' | 'REJECTED' | 'WITHDRAWN'

export type Application = {
  id: string
  candidateId: string
  companyId: string
  jobId: string
  status: ApplicationStatus
  coverLetter: string | null
  appliedAt: string
  active: boolean
}

export type ApplicationSummary = {
  id: string
  candidateId: string
  companyId: string
  jobId: string
  status: ApplicationStatus
  matchingScore: number | null
  matchingVersion: string | null
  appliedAt: string
  active: boolean
  createdAt: string
  updatedAt: string
}
