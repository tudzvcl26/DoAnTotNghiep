import type { UserRole } from '../enums/auth'

export type AuthTokens = {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

export type CurrentUser = {
  id: string
  email: string
  fullName: string
  phone: string | null
  avatarUrl: string | null
  enabled: boolean
  verified: boolean
  roles: Array<UserRole | string>
}

export type LoginRequest = {
  email: string
  password: string
}

export type RegisterRequest = {
  email: string
  password: string
  fullName: string
  phone?: string
}
