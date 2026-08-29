export type CvTemplateId = 'classic' | 'modern' | 'ats' | 'student' | 'professional'

export type CvPersonalInfo = {
  fullName: string
  headline: string
  email: string
  phone: string
  location: string
  website: string
}

export type CvExperience = { position: string; company: string; startDate: string; endDate: string; description: string }
export type CvEducation = { school: string; degree: string; startDate: string; endDate: string; description: string }
export type CvProject = { name: string; url: string; description: string }
export type CvCertification = { name: string; issuer: string; date: string }
export type CvNamedItem = { name: string; date: string; description: string }

export type CvContent = {
  personalInfo: CvPersonalInfo
  summary: string
  experiences: CvExperience[]
  education: CvEducation[]
  skills: string[]
  projects: CvProject[]
  certifications: CvCertification[]
  awards: CvNamedItem[]
  activities: CvNamedItem[]
}

export type CandidateCv = {
  id: string
  title: string
  templateId: CvTemplateId
  language: 'vi'
  content: CvContent
  version: number
  createdAt: string
  updatedAt: string
}

export type SaveCvPayload = Pick<CandidateCv, 'title' | 'templateId' | 'language' | 'content'>

export const emptyCvContent = (): CvContent => ({
  personalInfo: { fullName: '', headline: '', email: '', phone: '', location: '', website: '' },
  summary: '', experiences: [], education: [], skills: [], projects: [], certifications: [], awards: [], activities: [],
})
