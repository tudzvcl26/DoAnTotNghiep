export type ApplyJobRequest = {
  jobId: string
  coverLetter?: string
}

export type WithdrawApplicationRequest = {
  reasonCode?: string
  reasonDetail?: string
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
  matchingScore: number | null
  matchingVersion: string | null
  resumeSnapshotId: string | null
  jobSnapshotId: string | null
  resumeSnapshot: ResumeSnapshot | null
  jobSnapshot: JobSnapshot | null
  statusHistory: ApplicationStatusHistory[]
  createdAt: string
  updatedAt: string
}

export type ResumeSnapshot = {
  id: string
  applicationId: string
  candidateId: string
  snapshotData: string
  resumeVersion: string
  createdAt: string
}

export type JobSnapshot = {
  id: string
  applicationId: string
  jobId: string
  snapshotData: string
  jobVersion: string
  createdAt: string
}

export type ApplicationStatusHistory = {
  id: string
  applicationId: string
  fromStatus: ApplicationStatus | null
  toStatus: ApplicationStatus
  reasonCode: string | null
  reasonDetail: string | null
  changedBy: string
  changedAt: string
  createdAt: string
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
