import { z } from 'zod'

const optionalText = (max: number) => z.string().max(max).optional()
const optionalNumber = z.number().min(0).optional()
const optionalDate = z.string().optional()

export const initializeProfileSchema = z.object({ displayName: z.string().trim().min(1, 'Họ tên là bắt buộc.') })

export const profileSchema = z.object({
  displayName: z.string().trim().min(1, 'Họ tên là bắt buộc.').max(150),
  headline: optionalText(255), summary: optionalText(5000),
  countryCode: z.string().regex(/^$|^[A-Z]{2}$/, 'Mã quốc gia gồm 2 chữ in hoa.').optional(),
  provinceCode: optionalText(50), cityName: optionalText(120), districtName: optionalText(120),
  contactEmail: z.union([z.literal(''), z.email('Email chưa hợp lệ.')]).optional(),
  contactPhone: optionalText(30),
  profileVisibility: z.enum(['PUBLIC', 'RECRUITERS_ONLY', 'PRIVATE', 'HIDDEN', 'ANONYMOUS']),
})

export const objectiveSchema = z.object({
  objectiveText: optionalText(5000), targetSeniority: optionalText(100),
  availabilityStatus: z.enum(['ACTIVELY_LOOKING', 'OPEN_TO_OFFERS', 'NOT_LOOKING', 'UNAVAILABLE']).optional(),
})

export const preferenceSchema = z.object({
  salaryMinimum: optionalNumber, salaryMaximum: optionalNumber,
  salaryCurrency: z.string().regex(/^$|^.{3}$/, 'Mã tiền tệ gồm 3 ký tự.').optional(),
  salaryPeriod: z.enum(['HOURLY', 'MONTHLY', 'YEARLY']).optional(),
  availabilityStatus: z.enum(['ACTIVELY_LOOKING', 'OPEN_TO_OFFERS', 'NOT_LOOKING', 'UNAVAILABLE']).optional(),
  workArrangement: z.enum(['ONSITE', 'HYBRID', 'REMOTE', 'FLEXIBLE']).optional(),
  recommendationConsent: z.boolean().optional(),
}).refine((value) => value.salaryMinimum == null || value.salaryMaximum == null || value.salaryMaximum >= value.salaryMinimum, { message: 'Mức lương tối đa phải lớn hơn hoặc bằng tối thiểu.', path: ['salaryMaximum'] })

export const educationSchema = z.object({ institutionName: z.string().trim().min(1).max(255), qualification: z.string().trim().min(1).max(150), fieldOfStudy: optionalText(200), startDate: z.string().min(1, 'Ngày bắt đầu là bắt buộc.'), endDate: optionalDate, grade: optionalText(50), description: optionalText(5000) })
export const experienceSchema = z.object({ employerName: z.string().trim().min(1).max(255), jobTitle: z.string().trim().min(1).max(200), employmentType: z.enum(['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE', 'TEMPORARY']), location: optionalText(255), startDate: z.string().min(1, 'Ngày bắt đầu là bắt buộc.'), endDate: optionalDate, current: z.boolean().optional(), description: optionalText(5000), achievements: optionalText(5000) })
export const skillSchema = z.object({ skillName: z.string().trim().min(1).max(150), skillLevel: z.enum(['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT']).optional(), yearsExperience: z.number().min(0).max(99.9).optional() })
export const languageSchema = z.object({ languageCode: z.string().trim().min(1).max(20), languageLevel: z.enum(['BASIC', 'CONVERSATIONAL', 'PROFESSIONAL', 'NATIVE_OR_BILINGUAL']).optional() })
export const certificateSchema = z.object({ certificateName: z.string().trim().min(1).max(255), issuerName: z.string().trim().min(1).max(255), credentialId: optionalText(150), issueDate: z.string().min(1, 'Ngày cấp là bắt buộc.'), expiryDate: optionalDate, verificationUrl: z.union([z.literal(''), z.url('URL chưa hợp lệ.').max(2048)]).optional() })
export const socialLinkSchema = z.object({ linkType: z.enum(['LINKEDIN', 'GITHUB', 'GITLAB', 'PORTFOLIO', 'WEBSITE', 'OTHER']).optional(), url: z.url('URL phải bắt đầu bằng http:// hoặc https://').max(2048), label: optionalText(150) })

export type InitializeProfileForm = z.infer<typeof initializeProfileSchema>
export type ProfileForm = z.infer<typeof profileSchema>
export type ObjectiveForm = z.infer<typeof objectiveSchema>
export type PreferenceForm = z.infer<typeof preferenceSchema>
export type EducationForm = z.infer<typeof educationSchema>
export type ExperienceForm = z.infer<typeof experienceSchema>
export type SkillForm = z.infer<typeof skillSchema>
export type LanguageForm = z.infer<typeof languageSchema>
export type CertificateForm = z.infer<typeof certificateSchema>
export type SocialLinkForm = z.infer<typeof socialLinkSchema>
