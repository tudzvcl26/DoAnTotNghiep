import { apiClient } from '../../lib/api/client'
import type { ApiResponse, PageResponse } from '../../types/api/common'
import type { Notification, NotificationEventType, UnreadNotificationCount } from '../../types/models/notification'

export type NotificationListParams = {
  page: number
  size: number
  read?: boolean
  eventType?: NotificationEventType
  q?: string
}

export async function getNotifications(params: NotificationListParams): Promise<PageResponse<Notification>> {
  const response = await apiClient.get<ApiResponse<PageResponse<Notification>>>('/api/v1/notifications', { params })
  return response.data.data
}

export async function getUnreadNotificationCount(): Promise<UnreadNotificationCount> {
  const response = await apiClient.get<ApiResponse<UnreadNotificationCount>>('/api/v1/notifications/unread-count')
  return response.data.data
}

export async function markNotificationRead(notificationId: string): Promise<Notification> {
  const response = await apiClient.patch<ApiResponse<Notification>>(`/api/v1/notifications/${notificationId}/read`)
  return response.data.data
}

export async function markAllNotificationsRead(): Promise<void> {
  await apiClient.patch<ApiResponse<void>>('/api/v1/notifications/read-all')
}
