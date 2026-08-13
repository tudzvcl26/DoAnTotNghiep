import type { ApplicationStatus } from '../../types/models/application'

export const applicationStatusLabels: Record<ApplicationStatus, string> = {
  APPLIED: 'Đã ứng tuyển',
  SCREENING: 'Đang sàng lọc',
  INTERVIEW: 'Phỏng vấn',
  OFFER: 'Đề nghị',
  HIRED: 'Đã tuyển',
  REJECTED: 'Từ chối',
  WITHDRAWN: 'Đã rút',
}

export function formatApplicationDate(value: string) {
  return new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
}

export function canWithdrawApplication(status: ApplicationStatus) {
  return ['APPLIED', 'SCREENING', 'INTERVIEW', 'OFFER'].includes(status)
}
