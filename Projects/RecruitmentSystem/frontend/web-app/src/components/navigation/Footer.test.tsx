import { afterEach, expect, it } from 'vitest'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { Footer } from './Footer'
afterEach(cleanup)
it('uses existing CV/AI/employer routes and does not disguise unavailable destinations as home links', () => {
  render(<MemoryRouter><Footer /></MemoryRouter>)
  expect(screen.getByRole('link', { name: 'Tạo CV' }).getAttribute('href')).toBe('/cv/templates')
  expect(screen.getByRole('link', { name: 'AI Resume' }).getAttribute('href')).toBe('/candidate/ai-career')
  expect(screen.getByRole('link', { name: 'Đăng tuyển' }).getAttribute('href')).toBe('/employer/jobs/new')
  expect(screen.queryByRole('link', { name: 'Cover Letter' })).toBeNull()
  expect(screen.getByText('Cover Letter').getAttribute('aria-disabled')).toBe('true')
})
