import { apiClient } from '../../lib/api/client'
import type { ApiResponse } from '../../types/api/common'
import type { AuthTokens, CurrentUser, LoginRequest, RegisterRequest } from '../../types/models/auth'

export const authApi = {
  async login(payload: LoginRequest): Promise<AuthTokens> {
    const response = await apiClient.post<ApiResponse<AuthTokens>>('/api/v1/auth/login', payload)
    return response.data.data
  },

  async register(payload: RegisterRequest): Promise<AuthTokens> {
    const response = await apiClient.post<ApiResponse<AuthTokens>>('/api/v1/auth/register', payload)
    return response.data.data
  },

  async refresh(refreshToken: string): Promise<AuthTokens> {
    const response = await apiClient.post<ApiResponse<AuthTokens>>('/api/v1/auth/refresh', { refreshToken })
    return response.data.data
  },

  async logout(refreshToken: string): Promise<void> {
    await apiClient.post('/api/v1/auth/logout', { refreshToken })
  },

  async me(): Promise<CurrentUser> {
    const response = await apiClient.get<ApiResponse<CurrentUser>>('/api/v1/auth/me')
    return response.data.data
  },
}
