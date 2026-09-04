export const companyVerificationLabels: Record<string, string> = {
  PENDING: 'Đang chờ xác minh',
  VERIFIED: 'Đã xác minh',
  REJECTED: 'Bị từ chối',
}

export const companyStatusLabels: Record<string, string> = {
  ACTIVE: 'Đang hoạt động',
  INACTIVE: 'Ngừng hoạt động',
  SUSPENDED: 'Tạm khóa',
}

export function adminLabel(value: string | null | undefined, labels: Record<string, string>) {
  return value ? (labels[value] ?? 'Chưa xác định') : 'Chưa xác định'
}
import type { NotificationEventType } from '../../types/models/notification'
import type { NotificationChannel } from './admin.types'

export const activeStateLabels: Record<'ACTIVE' | 'INACTIVE' | 'DISABLED', string> = {
  ACTIVE: 'Đang hoạt động',
  INACTIVE: 'Ngừng hoạt động',
  DISABLED: 'Đã vô hiệu hóa',
}

export const notificationEventLabels: Record<NotificationEventType | 'JOB_PUBLISHED', string> = {
  APPLICATION_SUBMITTED: 'Đã nộp đơn ứng tuyển',
  APPLICATION_WITHDRAWN: 'Đã rút đơn ứng tuyển',
  APPLICATION_STATUS_CHANGED: 'Trạng thái đơn thay đổi',
  JOB_APPROVED: 'Việc làm được duyệt',
  JOB_PUBLISHED: 'Việc làm đã đăng',
  JOB_REJECTED: 'Việc làm bị từ chối',
  COMPANY_VERIFIED: 'Doanh nghiệp được xác minh',
  COMPANY_REJECTED: 'Doanh nghiệp bị từ chối',
  PASSWORD_CHANGED: 'Mật khẩu đã thay đổi',
  WELCOME: 'Chào mừng',
  SYSTEM_ANNOUNCEMENT: 'Thông báo hệ thống',
}

export const notificationChannelLabels: Record<NotificationChannel, string> = {
  IN_APP: 'Trong ứng dụng',
  EMAIL: 'Email',
  PUSH: 'Thông báo đẩy',
}

export const notificationResourceLabels: Record<string, string> = {
  APPLICATION: 'Đơn ứng tuyển',
  JOB: 'Việc làm',
  COMPANY: 'Công ty',
  USER: 'Người dùng',
  SYSTEM: 'Hệ thống',
}

export function notificationResourceLabel(value: string | null | undefined) {
  return value ? (notificationResourceLabels[value] ?? 'Tài nguyên liên quan') : 'Tài nguyên liên quan'
}
