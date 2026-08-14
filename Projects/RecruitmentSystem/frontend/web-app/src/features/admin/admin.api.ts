import { apiClient } from '../../lib/api/client'
import type { ApiResponse, PageResponse } from '../../types/api/common'
import type { Notification, NotificationEventType } from '../../types/models/notification'
import type {
  AdminNotificationRequest, AiProviderInfo, BroadcastNotificationRequest, CatalogCreateRequest, CatalogItem,
  CatalogKind, CatalogUpdateRequest, NotificationDeliveryLog, NotificationDeliveryStatus, NotificationTemplate,
  NotificationTemplateCreateRequest, NotificationTemplateUpdateRequest,
} from './admin.types'

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
