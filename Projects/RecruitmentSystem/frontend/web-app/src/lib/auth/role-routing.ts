import { normalizeRole } from '../../types/enums/auth'

export function getRoleHome(roles: string[]): string {
  const normalized = roles.map(normalizeRole)
  if (normalized.includes('ADMIN')) return '/admin'
  if (normalized.includes('EMPLOYER')) return '/employer'
  return '/candidate'
}
