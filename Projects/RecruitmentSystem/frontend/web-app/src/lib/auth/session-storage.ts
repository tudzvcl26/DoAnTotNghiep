import type { AuthTokens, CurrentUser } from '../../types/models/auth'

const STORAGE_KEY = 'recruitment.auth.session.v1'

export type StoredAuthSession = {
  tokens: AuthTokens | null
  currentUser: CurrentUser | null
}

const emptySession: StoredAuthSession = { tokens: null, currentUser: null }

export const authSessionStorage = {
  read(): StoredAuthSession {
    try {
      const value = window.localStorage.getItem(STORAGE_KEY)
      if (!value) return emptySession
      const parsed = JSON.parse(value) as Partial<StoredAuthSession>
      return {
        tokens: parsed.tokens?.accessToken && parsed.tokens.refreshToken ? parsed.tokens : null,
        currentUser: parsed.currentUser?.id ? parsed.currentUser : null,
      }
    } catch {
      return emptySession
    }
  },

  write(session: StoredAuthSession): void {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
  },

  clear(): void {
    window.localStorage.removeItem(STORAGE_KEY)
  },
}
