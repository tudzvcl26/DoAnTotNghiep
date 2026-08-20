import { type ReactNode, useEffect, useMemo, useRef } from 'react'
import { authApi } from './auth.api'
import { normalizeApiError } from '../../lib/api/error-adapter'
import { AuthContext, type AuthContextValue } from './auth-context'
import { useAuthStore } from './auth.store'

export function AuthProvider({ children }: { children: ReactNode }) {
  const { tokens, currentUser, isInitializing, setSession, setTokens, setCurrentUser, setInitializing, clearSession } = useAuthStore()
  const initialized = useRef(false)

  useEffect(() => {
    if (initialized.current) return
    initialized.current = true

    const restore = async () => {
      if (!tokens) {
        setInitializing(false)
        return
      }
      try {
        setCurrentUser(await authApi.me())
      } catch {
        if (!useAuthStore.getState().tokens) clearSession()
      } finally {
        setInitializing(false)
      }
    }
    void restore()
  }, [clearSession, setCurrentUser, setInitializing, tokens])

  const value = useMemo<AuthContextValue>(() => ({
    currentUser,
    isAuthenticated: Boolean(tokens?.accessToken && currentUser),
    isInitializing,
    login: async (request) => {
      const nextTokens = await authApi.login(request)
      setTokens(nextTokens)
      const user = await authApi.me()
      setSession(nextTokens, user)
      return user
    },
    register: async (request) => {
      const nextTokens = await authApi.register(request)
      setTokens(nextTokens)
      const user = await authApi.me()
      setSession(nextTokens, user)
      return user
    },
    logout: async () => {
      const refreshToken = useAuthStore.getState().tokens?.refreshToken
      try {
        if (refreshToken) await authApi.logout(refreshToken)
      } catch (error) {
        const code = normalizeApiError(error).code
        if (!code || !['AUTH_008', 'AUTH_009', 'AUTH_010'].includes(code)) throw error
      } finally {
        clearSession()
      }
    },
  }), [clearSession, currentUser, isInitializing, setSession, setTokens, tokens?.accessToken])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
