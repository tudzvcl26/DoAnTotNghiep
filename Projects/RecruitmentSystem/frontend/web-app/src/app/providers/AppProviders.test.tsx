import { afterEach, expect, it, vi } from 'vitest'
import { act, cleanup, render } from '@testing-library/react'
import { useQueryClient, type QueryClient } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { AppProviders } from './AppProviders'
import { useAuthStore } from '../../features/auth/auth.store'
vi.mock('../../features/auth/AuthProvider', () => ({ AuthProvider: ({ children }: {children: ReactNode}) => children }))
afterEach(() => { cleanup(); useAuthStore.setState({ currentUser: null, tokens: null }); localStorage.clear() })
it('retires private query caches across account changes and ignores late old-cache writes', () => {
  let observed!: QueryClient
  function Probe() { observed = useQueryClient(); return null }
  render(<AppProviders><Probe /></AppProviders>)
  const old = observed
  old.setQueryData(['candidate-profile'], { displayName: 'Private old user' })
  act(() => { useAuthStore.setState({ currentUser: { id: 'other-owner', email: 'qa@example.test', fullName: 'QA', roles: ['CANDIDATE'], phone: null, avatarUrl: null, enabled: true, verified: true } }) })
  expect(observed).not.toBe(old)
  expect(observed.getQueryData(['candidate-profile'])).toBeUndefined()
  old.setQueryData(['candidate-profile'], { displayName: 'Delayed old response' })
  expect(observed.getQueryData(['candidate-profile'])).toBeUndefined()
})
