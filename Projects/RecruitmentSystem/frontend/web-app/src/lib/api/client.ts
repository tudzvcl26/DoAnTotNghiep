import axios, { type InternalAxiosRequestConfig } from 'axios'
import { env } from '../../config/env'
import { authStore } from '../../features/auth/auth.store'
import type { ApiResponse } from '../../types/api/common'
import type { AuthTokens } from '../../types/models/auth'
import { createCorrelationId } from './correlation-id'
import { normalizeApiError } from './error-adapter'

type RetryableRequest = InternalAxiosRequestConfig & { _retry?: boolean }

export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  timeout: 120_000,
  headers: { Accept: 'application/json' },
})

apiClient.interceptors.request.use((config) => {
  const accessToken = authStore.getState().tokens?.accessToken
  config.headers.set('X-Correlation-ID', createCorrelationId())
  if (accessToken) config.headers.set('Authorization', `Bearer ${accessToken}`)
  return config
})

let refreshPromise: Promise<AuthTokens> | null = null

async function refreshSession(): Promise<AuthTokens> {
  const refreshToken = authStore.getState().tokens?.refreshToken
  if (!refreshToken) throw new Error('Missing refresh token')

  const response = await axios.post<ApiResponse<AuthTokens>>(
    `${env.apiBaseUrl}/api/v1/auth/refresh`,
    { refreshToken },
    { headers: { 'X-Correlation-ID': createCorrelationId(), Accept: 'application/json' }, timeout: 30_000 },
  )
  const tokens = response.data.data
  authStore.getState().setTokens(tokens)
  return tokens
}

function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    const returnTo = `${window.location.pathname}${window.location.search}`
    window.location.assign(`/login?returnTo=${encodeURIComponent(returnTo)}`)
  }
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: unknown) => {
    if (!axios.isAxiosError(error)) return Promise.reject(normalizeApiError(error))

    const original = error.config as RetryableRequest | undefined
    const url = original?.url ?? ''
    const nonRefreshableAuthRequest = ['/api/v1/auth/login', '/api/v1/auth/register', '/api/v1/auth/refresh', '/api/v1/auth/logout'].some((path) => url.includes(path))
    const canRefresh = error.response?.status === 401 && original && !original._retry && !nonRefreshableAuthRequest && Boolean(authStore.getState().tokens?.refreshToken)

    if (!canRefresh) return Promise.reject(normalizeApiError(error))

    original._retry = true
    try {
      refreshPromise ??= refreshSession().finally(() => { refreshPromise = null })
      const tokens = await refreshPromise
      original.headers.set('Authorization', `Bearer ${tokens.accessToken}`)
      original.headers.set('X-Correlation-ID', createCorrelationId())
      return apiClient(original)
    } catch (refreshError) {
      authStore.getState().clearSession()
      redirectToLogin()
      return Promise.reject(normalizeApiError(refreshError))
    }
  },
)
