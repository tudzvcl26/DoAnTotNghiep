import { apiClient } from '../../lib/api/client'
import type { ApiResponse, PageResponse } from '../../types/api/common'
import type { Notification, NotificationEventType } from '../../types/models/notification'
import type { JobDetail, JobStatus, JobSummary } from '../../types/models/job'
import type {
  AdminNotificationRequest, AiProviderInfo, BroadcastNotificationRequest, CatalogCreateRequest, CatalogItem,
  CatalogKind, CatalogUpdateRequest, NotificationDeliveryLog, NotificationDeliveryStatus, NotificationTemplate,
  NotificationTemplateCreateRequest, NotificationTemplateUpdateRequest,
  AdminUser, AdminCompany, AdminApplication, AdminApplicationFilters, AdminApplicationSummary,
} from './admin.types'
import type { SpringPage } from '../../types/api/common'

const catalogPaths: Record<CatalogKind, string> = {
  categories: '/api/v1/job-categories', skills: '/api/v1/skills', benefits: '/api/v1/benefits',
}

export const adminCatalogKey = (kind: CatalogKind) => ['admin-catalog', kind] as const
export const adminNotificationsKey = ['admin-notifications'] as const
export const adminTemplatesKey = ['admin-notification-templates'] as const
export const adminDeliveryLogsKey = ['admin-notification-delivery-logs'] as const
export const adminProviderKey = ['admin-ai-provider'] as const

export async function getCatalog(kind: CatalogKind, params: { page: number; size: number; keyword?: string }): Promise<PageResponse<CatalogItem>> {
  const path = params.keyword ? `${catalogPaths[kind]}/search` : catalogPaths[kind]
  const response = await apiClient.get<ApiResponse<PageResponse<CatalogItem>>>(path, {
    params: { ...params, sortBy: kind === 'categories' ? 'displayOrder' : 'name', direction: 'asc' },
  })
  return response.data.data
}

export async function createCatalogItem(kind: CatalogKind, payload: CatalogCreateRequest): Promise<CatalogItem> {
  const response = await apiClient.post<ApiResponse<CatalogItem>>(catalogPaths[kind], payload)
  return response.data.data
}

export async function updateCatalogItem(kind: CatalogKind, id: string, payload: CatalogUpdateRequest): Promise<CatalogItem> {
  const response = await apiClient.put<ApiResponse<CatalogItem>>(`${catalogPaths[kind]}/${id}`, payload)
  return response.data.data
}

export async function deleteCatalogItem(kind: CatalogKind, id: string): Promise<void> {
  await apiClient.delete(`${catalogPaths[kind]}/${id}`)
}

export async function getAdminNotifications(params: { page: number; size: number; recipientUserId?: string; eventType?: NotificationEventType; read?: boolean; q?: string }): Promise<PageResponse<Notification>> {
  const response = await apiClient.get<ApiResponse<PageResponse<Notification>>>('/api/v1/notifications', { params })
  return response.data.data
}

export async function createAdminNotification(payload: AdminNotificationRequest): Promise<Notification> {
  const response = await apiClient.post<ApiResponse<Notification>>('/api/v1/notifications', payload)
  return response.data.data
}

export async function broadcastAdminNotification(payload: BroadcastNotificationRequest): Promise<Notification> {
  const response = await apiClient.post<ApiResponse<Notification>>('/api/v1/notifications/broadcast', payload)
  return response.data.data
}

export async function getNotificationTemplates(params: { page: number; size: number; active?: boolean }): Promise<PageResponse<NotificationTemplate>> {
  const response = await apiClient.get<ApiResponse<PageResponse<NotificationTemplate>>>('/api/v1/notification-templates', { params })
  return response.data.data
}

export async function createNotificationTemplate(payload: NotificationTemplateCreateRequest): Promise<NotificationTemplate> {
  const response = await apiClient.post<ApiResponse<NotificationTemplate>>('/api/v1/notification-templates', payload)
  return response.data.data
}

export async function updateNotificationTemplate(id: string, payload: NotificationTemplateUpdateRequest): Promise<NotificationTemplate> {
  const response = await apiClient.put<ApiResponse<NotificationTemplate>>(`/api/v1/notification-templates/${id}`, payload)
  return response.data.data
}

export async function updateNotificationTemplateActive(id: string, active: boolean): Promise<NotificationTemplate> {
  const response = await apiClient.patch<ApiResponse<NotificationTemplate>>(`/api/v1/notification-templates/${id}/active`, { active })
  return response.data.data
}

export async function getNotificationDeliveryLogs(params: { page: number; size: number; status?: NotificationDeliveryStatus }): Promise<PageResponse<NotificationDeliveryLog>> {
  const response = await apiClient.get<ApiResponse<PageResponse<NotificationDeliveryLog>>>('/api/v1/admin/notification-delivery-logs', { params })
  return response.data.data
}

export async function getAiProviderInfo(): Promise<AiProviderInfo> {
  const response = await apiClient.get<ApiResponse<AiProviderInfo>>('/api/v1/ai/providers')
  return response.data.data
}

export const adminUsersKey = ['admin-users'] as const
export const adminCompaniesKey = ['admin-companies'] as const
export const adminApplicationsKey = ['admin-applications'] as const
export const adminJobsKey = ['admin-jobs'] as const

export type AdminJobsParams = { page: number; size: number; sort: string; keyword?: string; status?: JobStatus; companyId?: string }

export async function getAdminJobs(params: AdminJobsParams): Promise<PageResponse<JobSummary>> {
  const response = await apiClient.get<ApiResponse<PageResponse<JobSummary>>>('/api/v1/admin/jobs', { params })
  return response.data.data
}

export async function getAdminJob(id: string): Promise<JobDetail> {
  const response = await apiClient.get<ApiResponse<JobDetail>>(`/api/v1/admin/jobs/${id}`)
  return response.data.data
}

export async function publishAdminJob(id: string): Promise<JobDetail> {
  const response = await apiClient.patch<ApiResponse<JobDetail>>(`/api/v1/admin/jobs/${id}/publish`)
  return response.data.data
}

export async function closeAdminJob(id: string): Promise<JobDetail> {
  const response = await apiClient.patch<ApiResponse<JobDetail>>(`/api/v1/admin/jobs/${id}/close`)
  return response.data.data
}

export async function deleteAdminJob(id: string): Promise<void> {
  await apiClient.delete(`/api/v1/admin/jobs/${id}`)
}

export async function getAdminUsers(params: { page: number; size: number; sort: string; keyword?: string; role?: string; enabled?: boolean }): Promise<SpringPage<AdminUser>> {
  const response = await apiClient.get<ApiResponse<SpringPage<AdminUser>>>('/api/v1/admin/users', { params })
  return response.data.data
}
export async function updateAdminUserRoles(id: string, roles: string[]): Promise<AdminUser> {
  const response = await apiClient.patch<ApiResponse<AdminUser>>(`/api/v1/admin/users/${id}/roles`, { roles })
  return response.data.data
}
export async function updateAdminUserEnabled(id: string, enabled: boolean): Promise<AdminUser> {
  const response = await apiClient.patch<ApiResponse<AdminUser>>(`/api/v1/admin/users/${id}/enabled`, { enabled })
  return response.data.data
}
export async function getAdminCompanies(params: { page: number; size: number; sort: string; keyword?: string; status?: string; verificationStatus?: string }): Promise<SpringPage<AdminCompany>> {
  const response = await apiClient.get<SpringPage<AdminCompany>>('/api/v1/admin/companies', { params })
  return response.data
}
export async function updateAdminCompanyVerification(id: string, verificationStatus: string): Promise<AdminCompany> {
  const response = await apiClient.patch<AdminCompany>(`/api/v1/admin/companies/${id}/verification`, { verificationStatus })
  return response.data
}
export async function getAdminApplications(params: AdminApplicationFilters): Promise<PageResponse<AdminApplicationSummary>> {
  const response = await apiClient.get<ApiResponse<PageResponse<AdminApplicationSummary>>>('/api/v1/admin/applications', { params })
  return response.data.data
}
export async function getAdminApplication(id: string): Promise<AdminApplication> {
  const response = await apiClient.get<ApiResponse<AdminApplication>>(`/api/v1/admin/applications/${id}`)
  return response.data.data
}
