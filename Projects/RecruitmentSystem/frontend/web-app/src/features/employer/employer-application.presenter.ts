import type { ApplicationStatus } from '../../types/models/application'
export { applicationStatusLabels as employerApplicationStatusLabels } from '../applications/application-presenter'

export const employerTransitions: Record<ApplicationStatus, ApplicationStatus[]> = {
  APPLIED: ['SCREENING', 'REJECTED'], SCREENING: ['INTERVIEW', 'REJECTED'],
  INTERVIEW: ['OFFER', 'REJECTED'], OFFER: ['HIRED', 'REJECTED'],
  HIRED: [], REJECTED: [], WITHDRAWN: [],
}

export function formatEmployerApplicationDate(value: string) {
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

export function parseSnapshot(value?: string | null): Record<string, unknown> {
  if (!value) return {}
  try {
    const parsed: unknown = JSON.parse(value)
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed as Record<string, unknown> : {}
  } catch {
    return {}
  }
}

export function snapshotText(snapshot: Record<string, unknown>, field: string) {
  return typeof snapshot[field] === 'string' ? snapshot[field] : null
}

export function snapshotNumber(snapshot: Record<string, unknown>, field: string) {
  return typeof snapshot[field] === 'number' ? snapshot[field] : null
}
