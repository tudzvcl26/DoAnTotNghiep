import { create } from 'zustand'
import { authSessionStorage } from '../../lib/auth/session-storage'
import type { AuthTokens, CurrentUser } from '../../types/models/auth'

const restored = authSessionStorage.read()

type AuthState = {
  tokens: AuthTokens | null
  currentUser: CurrentUser | null
  isInitializing: boolean
  setSession: (tokens: AuthTokens, currentUser: CurrentUser) => void
  setTokens: (tokens: AuthTokens) => void
  setCurrentUser: (currentUser: CurrentUser | null) => void
  setInitializing: (value: boolean) => void
  clearSession: () => void
}

function persist(tokens: AuthTokens | null, currentUser: CurrentUser | null) {
  if (!tokens && !currentUser) authSessionStorage.clear()
  else authSessionStorage.write({ tokens, currentUser })
}

export const useAuthStore = create<AuthState>((set, get) => ({
  tokens: restored.tokens,
  currentUser: restored.currentUser,
  isInitializing: true,
  setSession: (tokens, currentUser) => {
    persist(tokens, currentUser)
    set({ tokens, currentUser })
  },
  setTokens: (tokens) => {
    persist(tokens, get().currentUser)
    set({ tokens })
  },
  setCurrentUser: (currentUser) => {
    persist(get().tokens, currentUser)
    set({ currentUser })
  },
  setInitializing: (isInitializing) => set({ isInitializing }),
  clearSession: () => {
    authSessionStorage.clear()
    set({ tokens: null, currentUser: null, isInitializing: false })
  },
}))

export const authStore = useAuthStore
