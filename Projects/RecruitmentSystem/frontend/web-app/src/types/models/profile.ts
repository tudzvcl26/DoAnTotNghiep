export type CandidateProfile = {
  id: string
  userId: string
  displayName: string
  headline: string | null
  summary: string | null
  countryCode: string | null
  provinceCode: string | null
  cityName: string | null
  districtName: string | null
  contactEmail: string | null
  contactPhone: string | null
  profileVisibility: string
  profileStatus: string
  completionScore: number | null
  completionCalculatedAt: string | null
  version: number
  createdAt: string
  updatedAt: string
}

export type ProfileVisibility = 'PUBLIC' | 'RECRUITERS_ONLY' | 'PRIVATE' | 'HIDDEN' | 'ANONYMOUS'
export type AvailabilityStatus = 'ACTIVELY_LOOKING' | 'OPEN_TO_OFFERS' | 'NOT_LOOKING' | 'UNAVAILABLE'
export type SalaryPeriod = 'HOURLY' | 'MONTHLY' | 'YEARLY'
export type WorkArrangement = 'ONSITE' | 'HYBRID' | 'REMOTE' | 'FLEXIBLE'
export type ProfileEmploymentType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERNSHIP' | 'FREELANCE' | 'TEMPORARY'
export type SkillLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT'
export type LanguageLevel = 'BASIC' | 'CONVERSATIONAL' | 'PROFESSIONAL' | 'NATIVE_OR_BILINGUAL'
export type SocialLinkType = 'LINKEDIN' | 'GITHUB' | 'GITLAB' | 'PORTFOLIO' | 'WEBSITE' | 'OTHER'

export type InitializeProfileRequest = { displayName: string }
export type UpdateProfileRequest = {
  displayName: string
  headline?: string
  summary?: string
  countryCode?: string
  provinceCode?: string
  cityName?: string
  districtName?: string
  contactEmail?: string
  contactPhone?: string
  profileVisibility?: ProfileVisibility
  version?: number
}

export type CareerObjective = { id: string; objectiveText: string | null; targetSeniority: string | null; availabilityStatus: AvailabilityStatus | null; version: number }
export type CareerObjectiveRequest = { objectiveText?: string; targetSeniority?: string; availabilityStatus?: AvailabilityStatus; version?: number }

export type CandidatePreference = { id: string; salaryMinimum: number | null; salaryMaximum: number | null; salaryCurrency: string | null; salaryPeriod: SalaryPeriod | null; availabilityStatus: AvailabilityStatus | null; workArrangement: WorkArrangement | null; recommendationConsent: boolean | null; version: number }
export type CandidatePreferenceRequest = { salaryMinimum?: number; salaryMaximum?: number; salaryCurrency?: string; salaryPeriod?: SalaryPeriod; availabilityStatus?: AvailabilityStatus; workArrangement?: WorkArrangement; recommendationConsent?: boolean; version?: number }

export type Education = { id: string; institutionName: string; qualification: string; fieldOfStudy: string | null; startDate: string | null; endDate: string | null; grade: string | null; description: string | null; version: number }
export type EducationRequest = { institutionName: string; qualification: string; fieldOfStudy?: string | null; startDate?: string | null; endDate?: string | null; grade?: string | null; description?: string | null; version?: number }
export type Experience = { id: string; employerName: string; jobTitle: string; employmentType: ProfileEmploymentType | null; location: string | null; startDate: string | null; endDate: string | null; current: boolean | null; description: string | null; achievements: string | null; version: number }
export type ExperienceRequest = { employerName: string; jobTitle: string; employmentType?: ProfileEmploymentType | null; location?: string | null; startDate?: string | null; endDate?: string | null; current?: boolean | null; description?: string | null; achievements?: string | null; version?: number }
export type CandidateSkill = { id: string; skillId: string; skillName: string; skillLevel: SkillLevel | null; yearsExperience: number | null; version: number }
export type CandidateSkillRequest = { skillName: string; skillLevel?: SkillLevel; yearsExperience?: number; version?: number }
export type CandidateLanguage = { id: string; languageId: string; languageCode: string; displayName: string; languageLevel: LanguageLevel | null; version: number }
export type CandidateLanguageRequest = { languageCode: string; languageLevel?: LanguageLevel; version?: number }
export type Certificate = { id: string; certificateName: string; issuerName: string; credentialId: string | null; issueDate: string | null; expiryDate: string | null; verificationUrl: string | null; version: number }
export type CertificateRequest = { certificateName: string; issuerName: string; credentialId?: string | null; issueDate?: string | null; expiryDate?: string | null; verificationUrl?: string | null; version?: number }
export type SocialLink = { id: string; linkType: SocialLinkType | null; url: string; label: string | null; version: number }
export type SocialLinkRequest = { linkType?: SocialLinkType | null; url: string; label?: string | null; version?: number }
