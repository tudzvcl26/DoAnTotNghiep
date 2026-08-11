import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../../features/auth/auth-context'
import { getRoleHome } from '../../lib/auth/role-routing'
import { normalizeRole, type UserRole } from '../../types/enums/auth'

export function RoleGuard({ roles, children }: { roles: UserRole[]; children: ReactNode }) {
  const { currentUser } = useAuth()
  const userRoles = currentUser?.roles.map(normalizeRole) ?? []
  if (!roles.some((role) => userRoles.includes(role))) {
    return <Navigate to={getRoleHome(currentUser?.roles ?? [])} replace />
  }
  return children
}
