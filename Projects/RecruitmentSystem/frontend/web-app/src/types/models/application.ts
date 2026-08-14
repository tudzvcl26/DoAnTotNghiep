export type ApplyJobRequest = {
  jobId: string
  coverLetter?: string
}

export type WithdrawApplicationRequest = {
  reasonCode?: string
  reasonDetail?: string
}

export type ApplicationStatus = 'APPLIED' | 'SCREENING' | 'INTERVIEW' | 'OFFER' | 'HIRED' | 'REJECTED' | 'WITHDRAWN'

export const APPLICATION_STATUSES: ApplicationStatus[] = ['APPLIED', 'SCREENING', 'INTERVIEW', 'OFFER', 'HIRED', 'REJECTED', 'WITHDRAWN']

export type UpdateApplicationStatusRequest = {
  status: ApplicationStatus
  reasonCode?: string
  reasonDetail?: string
}

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
  candidateProfileSnapshotId: string | null
  resumeSnapshot: ResumeSnapshot | null
  jobSnapshot: JobSnapshot | null
  candidateProfileSnapshot: CandidateProfileSnapshot | null
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

export type CandidateProfileSnapshot = {
  id: string
  applicationId: string
  candidateId: string
  profileId: string | null
  displayName: string
  headline: string | null
  contactEmail: string | null
  contactPhone: string | null
  profileVersion: number | null
  capturedAt: string
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
  candidateProfileSnapshot: CandidateProfileSnapshot | null
  status: ApplicationStatus
  matchingScore: number | null
  matchingVersion: string | null
  appliedAt: string
  active: boolean
  createdAt: string
  updatedAt: string
}
