import { afterEach, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { AppErrorBoundary } from './AppErrorBoundary'

afterEach(() => { cleanup(); vi.restoreAllMocks(); sessionStorage.clear() })
it('shows a safe recovery action without clearing drafts or retrying automatically', () => {
  vi.spyOn(console, 'error').mockImplementation(() => {})
  const reload = vi.fn()
  sessionStorage.setItem('qa-draft', 'Nguyễn Văn An')
  function BrokenRoute(): never { throw new TypeError('Failed to fetch dynamically imported module: private-url') }
  render(<AppErrorBoundary onReload={reload}><BrokenRoute /></AppErrorBoundary>)
  expect(screen.getByRole('alert').textContent).toContain('Không thể mở giao diện')
  expect(screen.queryByText(/private-url/)).toBeNull()
  expect(reload).not.toHaveBeenCalled()
  fireEvent.click(screen.getByRole('button', { name: 'Tải lại ứng dụng' }))
  expect(reload).toHaveBeenCalledTimes(1)
  expect(sessionStorage.getItem('qa-draft')).toBe('Nguyễn Văn An')
})
