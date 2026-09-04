import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { type ReactNode, useEffect, useMemo } from 'react'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '../../features/auth/AuthProvider'
import { useAuthStore } from '../../features/auth/auth.store'

const createQueryClient = (ownerId: string | null) => new QueryClient({
  defaultOptions: {
    queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false, meta: { ownerId } },
    mutations: { retry: 0 },
  },
})

export function AppProviders({ children }: { children: ReactNode }) {
  const ownerId = useAuthStore((state) => state.currentUser?.id ?? null)
  // Never expose an old account's cached data while a new request authorizes.
  // Late callbacks retain the retired client, not the new account's cache.
  const queryClient = useMemo(() => createQueryClient(ownerId), [ownerId])
  useEffect(() => () => queryClient.clear(), [queryClient])
  return (
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>{children}</AuthProvider>
      </QueryClientProvider>
    </BrowserRouter>
  )
}
