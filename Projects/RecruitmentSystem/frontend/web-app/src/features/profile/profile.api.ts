import { apiClient } from '../../lib/api/client'
import type { ApiResponse, SpringPage } from '../../types/api/common'
import type {
  CandidateLanguage, CandidateLanguageRequest, CandidatePreference, CandidatePreferenceRequest,
  CandidateProfile, CandidateSkill, CandidateSkillRequest, CareerObjective, CareerObjectiveRequest,
  Certificate, CertificateRequest, Education, EducationRequest, Experience, ExperienceRequest,
  InitializeProfileRequest, SocialLink, SocialLinkRequest, UpdateProfileRequest,
} from '../../types/models/profile'

const userPath = (userId: string, suffix: string) => `/api/v1/users/${userId}/${suffix}`
const unwrap = <T>(response: { data: ApiResponse<T> }) => response.data.data

export const profileApi = {
  get: async () => unwrap(await apiClient.get<ApiResponse<CandidateProfile>>('/api/v1/profiles/me')),
  initialize: async (payload: InitializeProfileRequest) => unwrap(await apiClient.post<ApiResponse<CandidateProfile>>('/api/v1/profiles/initialize', payload)),
  update: async (payload: UpdateProfileRequest) => unwrap(await apiClient.put<ApiResponse<CandidateProfile>>('/api/v1/profiles/me', payload)),
  getObjective: async (userId: string) => unwrap(await apiClient.get<ApiResponse<CareerObjective>>(userPath(userId, 'career-objective'))),
  updateObjective: async (userId: string, payload: CareerObjectiveRequest) => unwrap(await apiClient.put<ApiResponse<CareerObjective>>(userPath(userId, 'career-objective'), payload)),
  getPreference: async (userId: string) => unwrap(await apiClient.get<ApiResponse<CandidatePreference>>(userPath(userId, 'candidate-preference'))),
  createPreference: async (userId: string, payload: CandidatePreferenceRequest) => unwrap(await apiClient.post<ApiResponse<CandidatePreference>>(userPath(userId, 'candidate-preference'), payload)),
  updatePreference: async (userId: string, payload: CandidatePreferenceRequest) => unwrap(await apiClient.put<ApiResponse<CandidatePreference>>(userPath(userId, 'candidate-preference'), payload)),
}

type CollectionConfig<T, P> = {
  path: string
  list: (userId: string) => Promise<SpringPage<T>>
  create: (userId: string, payload: P) => Promise<T>
  update: (userId: string, itemId: string, payload: P) => Promise<T>
  remove: (userId: string, itemId: string) => Promise<void>
}

function collectionApi<T, P>(path: string): CollectionConfig<T, P> {
  return {
    path,
    list: async (userId) => unwrap(await apiClient.get<ApiResponse<SpringPage<T>>>(userPath(userId, path), { params: { page: 0, size: 100 } })),
    create: async (userId, payload) => unwrap(await apiClient.post<ApiResponse<T>>(userPath(userId, path), payload)),
    update: async (userId, itemId, payload) => unwrap(await apiClient.put<ApiResponse<T>>(`${userPath(userId, path)}/${itemId}`, payload)),
    remove: async (userId, itemId) => { await apiClient.delete(`${userPath(userId, path)}/${itemId}`) },
  }
}

export const educationApi = collectionApi<Education, EducationRequest>('educations')
export const experienceApi = collectionApi<Experience, ExperienceRequest>('experiences')
export const skillApi = collectionApi<CandidateSkill, CandidateSkillRequest>('skills')
export const languageApi = collectionApi<CandidateLanguage, CandidateLanguageRequest>('languages')
export const certificateApi = collectionApi<Certificate, CertificateRequest>('certificates')
export const socialLinkApi = collectionApi<SocialLink, SocialLinkRequest>('social-links')
