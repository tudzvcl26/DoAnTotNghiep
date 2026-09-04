import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useState } from 'react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AdminCatalogPage } from './AdminCatalogPage'
import { createCatalogItem, getCatalog } from './admin.api'
import type { CatalogKind } from './admin.types'

vi.mock('./admin.api', () => ({
  adminCatalogKey: (kind: CatalogKind) => ['admin', 'catalog', kind],
  createCatalogItem: vi.fn(),
  deleteCatalogItem: vi.fn(),
  getCatalog: vi.fn(),
  updateCatalogItem: vi.fn(),
}))

const emptyPage = {
  content: [], page: 0, size: 12, totalElements: 0, totalPages: 0,
  first: true, last: true, hasNext: false, hasPrevious: false,
}

function Harness() {
  const [kind, setKind] = useState<CatalogKind>('categories')
  return <>
    <button type="button" onClick={() => setKind('skills')}>Đi tới kỹ năng</button>
    <AdminCatalogPage kind={kind} />
  </>
}

describe('AdminCatalogPage', () => {
  beforeEach(() => {
    vi.mocked(getCatalog).mockResolvedValue(emptyPage)
    vi.mocked(createCatalogItem).mockResolvedValue({} as never)
  })

  it('clears route-specific feedback when the catalog kind changes', async () => {
    const user = userEvent.setup()
    const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
    render(<QueryClientProvider client={client}><MemoryRouter><Harness /></MemoryRouter></QueryClientProvider>)

    await screen.findByRole('heading', { name: 'Ngành nghề' })
    await user.click(screen.getByRole('button', { name: 'Thêm mới' }))
    await user.type(screen.getByRole('textbox', { name: 'Tên' }), 'QA Category')
    await user.type(screen.getByRole('textbox', { name: 'Slug' }), 'qa-category')
    await user.click(screen.getByRole('button', { name: 'Tạo mới' }))
    expect((await screen.findByRole('status')).textContent).toContain('Tạo ngành nghề thành công.')

    await user.click(screen.getByRole('button', { name: 'Đi tới kỹ năng' }))
    await screen.findByRole('heading', { name: 'Kỹ năng' })
    await waitFor(() => expect(screen.queryByRole('status')).toBeNull())
  })
})
