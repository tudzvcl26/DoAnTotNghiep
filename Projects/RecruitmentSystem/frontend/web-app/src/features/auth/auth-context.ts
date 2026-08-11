import { createContext, useContext } from 'react'
import type { CurrentUser, LoginRequest, RegisterRequest } from '../../types/models/auth'

export type AuthContextValue = {
  currentUser: CurrentUser | null
  isAuthenticated: boolean
  isInitializing: boolean
  login: (request: LoginRequest) => Promise<CurrentUser>
  register: (request: RegisterRequest) => Promise<CurrentUser>
  logout: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}
