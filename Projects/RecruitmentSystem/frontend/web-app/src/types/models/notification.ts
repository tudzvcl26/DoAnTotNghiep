export type NotificationEventType =
  | 'APPLICATION_SUBMITTED'
  | 'APPLICATION_WITHDRAWN'
  | 'APPLICATION_STATUS_CHANGED'
  | 'JOB_APPROVED'
  | 'JOB_REJECTED'
  | 'COMPANY_VERIFIED'
  | 'COMPANY_REJECTED'
  | 'PASSWORD_CHANGED'
  | 'WELCOME'
  | 'SYSTEM_ANNOUNCEMENT'

export type Notification = {
  id: string
  eventType: NotificationEventType
  title: string
  content: string
  payload: Record<string, unknown>
  relatedResourceType: string | null
  relatedResourceId: string | null
  read: boolean
  readAt: string | null
  createdAt: string
}

export type UnreadNotificationCount = {
  unreadCount: number
}
