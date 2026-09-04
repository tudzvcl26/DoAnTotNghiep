import { cvPresetDefinitions } from './cv.presets'
export type CvTemplateId = keyof typeof cvPresetDefinitions
export type CvLanguage = 'vi' | 'en'
export type CvFontFamily = 'Roboto' | 'Inter' | 'Arial' | 'Times New Roman' | 'Georgia' | 'Open Sans'
export type CvDensity = 'compact' | 'normal' | 'comfortable'
export type CvLayout = 'single' | 'header' | 'sidebar-left' | 'sidebar-right'

export const builtInSectionIds = ['summary', 'experience', 'education', 'skills', 'projects', 'certifications', 'awards', 'activities'] as const
export type CvBuiltInSectionId = typeof builtInSectionIds[number]

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

export type CvThemeConfig = {
  id: string
  primaryColor: string
  secondaryColor: string
  textColor: string
  mutedColor: string
  backgroundColor: string
}

export type CvDesignConfig = {
  fontFamily: CvFontFamily
  fontScale: number
  theme: CvThemeConfig
  density: CvDensity
  layout: CvLayout
  sectionOrder: string[]
  sectionVisibility: Record<string, boolean>
}

export type CvCustomSection = {
  id: string
  title: string
  items: CvNamedItem[]
  visible: boolean
}

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
  designConfig: CvDesignConfig
  customSections: CvCustomSection[]
}

export type CandidateCv = {
  id: string
  title: string
  templateId: CvTemplateId
  language: CvLanguage
  content: CvContent
  version: number
  createdAt: string
  updatedAt: string
}

export type SaveCvPayload = Pick<CandidateCv, 'title' | 'templateId' | 'language' | 'content'>

export const cvThemes: CvThemeConfig[] = [
  { id: 'emerald', primaryColor: '#146F54', secondaryColor: '#DDF5EA', textColor: '#1F2937', mutedColor: '#667085', backgroundColor: '#FFFFFF' },
  { id: 'teal', primaryColor: '#0F766E', secondaryColor: '#CCFBF1', textColor: '#16302E', mutedColor: '#5F7471', backgroundColor: '#FFFFFF' },
  { id: 'blue', primaryColor: '#2563EB', secondaryColor: '#DBEAFE', textColor: '#172554', mutedColor: '#64748B', backgroundColor: '#FFFFFF' },
  { id: 'navy', primaryColor: '#173B66', secondaryColor: '#E8EEF6', textColor: '#172033', mutedColor: '#667085', backgroundColor: '#FFFFFF' },
  { id: 'purple', primaryColor: '#7C3AED', secondaryColor: '#EDE9FE', textColor: '#2E1065', mutedColor: '#746B86', backgroundColor: '#FFFFFF' },
  { id: 'burgundy', primaryColor: '#7A1F3D', secondaryColor: '#F7E9EE', textColor: '#21181B', mutedColor: '#75666B', backgroundColor: '#FFFFFF' },
  { id: 'orange', primaryColor: '#C2410C', secondaryColor: '#FFEDD5', textColor: '#431407', mutedColor: '#7C6A62', backgroundColor: '#FFFFFF' },
  { id: 'gray', primaryColor: '#475467', secondaryColor: '#EAECF0', textColor: '#1D2939', mutedColor: '#667085', backgroundColor: '#FFFFFF' },
]

export function defaultCvDesignConfig(templateId: CvTemplateId = 'classic'): CvDesignConfig {
  const preset = cvPresetDefinitions[templateId] ?? cvPresetDefinitions.classic
  const theme = cvThemes.find((item) => item.id === preset.theme) ?? cvThemes[0]
  const first = preset.arrangement === 'education' ? ['summary', 'education', 'projects'] : preset.arrangement === 'projects' ? ['summary', 'projects', 'experience'] : ['summary', 'experience']
  const sectionOrder = [...first, ...builtInSectionIds.filter(id => !first.includes(id))]
  return { fontFamily: preset.fontFamily, fontScale: 1, theme: { ...theme }, density: preset.density, layout: preset.layout, sectionOrder, sectionVisibility: {} }
}

export function applyCvTemplate(content: CvContent, templateId: CvTemplateId): CvContent {
  const design = defaultCvDesignConfig(templateId)
  return { ...content, designConfig: { ...design, sectionOrder: [...design.sectionOrder, ...content.customSections.map(section => `custom:${section.id}`)], sectionVisibility: { ...content.designConfig.sectionVisibility } } }
}

export const emptyCvContent = (templateId: CvTemplateId = 'classic'): CvContent => ({
  personalInfo: { fullName: '', headline: '', email: '', phone: '', location: '', website: '' },
  summary: '', experiences: [], education: [], skills: [], projects: [], certifications: [], awards: [], activities: [],
  designConfig: defaultCvDesignConfig(templateId), customSections: [],
})

export function normalizeCvContent(value: Partial<CvContent> | null | undefined, templateId: CvTemplateId = 'classic'): CvContent {
  const fallback = emptyCvContent(templateId)
  const customSections = Array.isArray(value?.customSections) ? value.customSections.filter(Boolean).map((section) => ({
    id: section.id || crypto.randomUUID(), title: section.title || 'Mục bổ sung', items: Array.isArray(section.items) ? section.items : [], visible: section.visible !== false,
  })) : []
  const supported = [...builtInSectionIds, ...customSections.map((section) => `custom:${section.id}`)]
  const incomingDesign = value?.designConfig
  const order = Array.isArray(incomingDesign?.sectionOrder) ? incomingDesign.sectionOrder.filter((id, index, all) => supported.includes(id as CvBuiltInSectionId) && all.indexOf(id) === index) : []
  supported.forEach((id) => { if (!order.includes(id)) order.push(id) })
  const theme = incomingDesign?.theme && cvThemes.some((item) => item.id === incomingDesign.theme.id) ? incomingDesign.theme : fallback.designConfig.theme
  return {
    personalInfo: { ...fallback.personalInfo, ...(value?.personalInfo ?? {}) }, summary: value?.summary ?? '',
    experiences: Array.isArray(value?.experiences) ? value.experiences : [], education: Array.isArray(value?.education) ? value.education : [],
    skills: Array.isArray(value?.skills) ? value.skills : [], projects: Array.isArray(value?.projects) ? value.projects : [],
    certifications: Array.isArray(value?.certifications) ? value.certifications : [], awards: Array.isArray(value?.awards) ? value.awards : [],
    activities: Array.isArray(value?.activities) ? value.activities : [], customSections,
    designConfig: {
      fontFamily: incomingDesign?.fontFamily ?? fallback.designConfig.fontFamily,
      fontScale: incomingDesign?.fontScale && incomingDesign.fontScale >= .85 && incomingDesign.fontScale <= 1.15 ? incomingDesign.fontScale : 1,
      theme: { ...theme }, density: incomingDesign?.density ?? fallback.designConfig.density,
      layout: incomingDesign?.layout ?? fallback.designConfig.layout, sectionOrder: order,
      sectionVisibility: { ...(incomingDesign?.sectionVisibility ?? {}) },
    },
  }
}
