export const USER_ROLES = ['CANDIDATE', 'EMPLOYER', 'ADMIN'] as const

export type UserRole = (typeof USER_ROLES)[number]

export function normalizeRole(role: string): string {
  return role.replace(/^ROLE_/, '').toUpperCase()
}
