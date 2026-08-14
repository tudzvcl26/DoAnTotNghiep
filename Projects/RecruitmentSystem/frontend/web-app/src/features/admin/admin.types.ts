import type { NotificationEventType } from '../../types/models/notification'

export type CatalogKind = 'categories' | 'skills' | 'benefits'

export type CatalogItem = {
  id: string
  name: string
  slug: string
  description: string | null
  icon: string | null
  active: boolean
  createdAt: string
  updatedAt: string
  displayOrder?: number
  parentId?: string | null
  parentName?: string | null
}

export type CatalogCreateRequest = {
  name: string
  slug: string
  description?: string
  icon?: string
  active: boolean
  displayOrder?: number
  parentId?: string | null
}

export type CatalogUpdateRequest = Omit<CatalogCreateRequest, 'slug'>

export type NotificationChannel = 'IN_APP' | 'EMAIL' | 'PUSH'
export type NotificationDeliveryStatus = 'PENDING' | 'SENT' | 'FAILED' | 'SKIPPED'

export type AdminNotificationRequest = {
  recipientUserId: string
  eventType: NotificationEventType
  title: string
  content: string
  relatedResourceType?: string
  relatedResourceId?: string
}

export type BroadcastNotificationRequest = { title: string; content: string }

export type NotificationTemplate = {
  id: string
  code: string
  eventType: NotificationEventType
  channel: NotificationChannel
  titleTemplate: string
  contentTemplate: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export type NotificationTemplateCreateRequest = Omit<NotificationTemplate, 'id' | 'active' | 'createdAt' | 'updatedAt'>
export type NotificationTemplateUpdateRequest = Omit<NotificationTemplateCreateRequest, 'code'>

export type NotificationDeliveryLog = {
  id: string
  notificationId: string
  userId: string
  channel: NotificationChannel
  status: NotificationDeliveryStatus
  attemptNumber: number
  attemptedAt: string | null
  deliveredAt: string | null
  errorMessage: string | null
  createdAt: string
}

export type AiProviderInfo = {
  phase: string
  provider: string
  model: string
  endpoint: string
  status: string
  openAiEnabled: boolean
  openAiConfigured: boolean
  ollamaEnabled: boolean
  ollamaConfigured: boolean
  structuredGeneration: { providerName: string; implementation: string; available: boolean }
  embedding: { providerName: string; implementation: string; available: boolean }
  enabledModelDeployments: number
}
