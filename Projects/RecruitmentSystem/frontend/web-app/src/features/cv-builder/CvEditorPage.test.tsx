import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest'
import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { CvEditorPage } from './CvEditorPage'
import { cvApi } from './cv.api'
import { AppError } from '../../lib/api/error-adapter'
import { emptyCvContent, type CandidateCv, type SaveCvPayload } from './cv.types'

vi.mock('./cv.api', () => ({ cvApi: { get: vi.fn(), create: vi.fn(), update: vi.fn(), download: vi.fn(), createFromProfile: vi.fn() }, saveBlob: vi.fn() }))
vi.mock('../auth/auth.store', () => ({ useAuthStore: (selector: (state: { currentUser: { id: string } }) => unknown) => selector({ currentUser: { id: 'qa-owner' } }) }))
beforeAll(() => {
  Object.defineProperty(HTMLElement.prototype, 'innerText', { configurable: true,
    get() { return this.textContent ?? '' }, set(value: string) { this.textContent = value } })
  HTMLElement.prototype.scrollIntoView = vi.fn()
})
afterEach(() => { cleanup(); vi.resetAllMocks(); sessionStorage.clear() })
const savedCv = (payload: SaveCvPayload): CandidateCv => ({ ...structuredClone(payload), id: 'qa-cv', version: 0, createdAt: '', updatedAt: '' })
function openEditor(path = '/cv/new?template=classic&source=blank') {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  return render(<QueryClientProvider client={client}><MemoryRouter initialEntries={[path]}><Routes>
    <Route path="/cv/new" element={<CvEditorPage />} /><Route path="/cv/:id/edit" element={<CvEditorPage />} />
  </Routes></MemoryRouter></QueryClientProvider>)
}
function typeName(text: string) {
  const field = screen.getByRole('textbox', { name: 'Họ và tên' })
  fireEvent.focus(field); field.innerText = text; fireEvent.input(field)
}

describe('CV persistence regressions', () => {
  it('pauses an already scheduled autosave until composition completes', async () => {
    vi.useFakeTimers()
    try {
      vi.mocked(cvApi.create).mockImplementation(() => new Promise(() => {}))
      openEditor()
      typeName('Ng')
      const field = screen.getByRole('textbox', { name: 'Họ và tên' })
      fireEvent.compositionStart(field)
      field.innerText = 'Nguyễ'; fireEvent.compositionUpdate(field); fireEvent.input(field)
      await act(async () => { await vi.advanceTimersByTimeAsync(1200) })
      expect(cvApi.create).not.toHaveBeenCalled()
      field.innerText = 'Nguyễn'; fireEvent.compositionEnd(field)
      await act(async () => { await vi.advanceTimersByTimeAsync(1000) })
      expect(cvApi.create).toHaveBeenCalledTimes(1)
      expect(vi.mocked(cvApi.create).mock.calls[0][0].content.personalInfo.fullName).toBe('Nguyễn')
    } finally { vi.useRealTimers() }
  })
  it('does not expose a writable blank CV while automatic profile import is pending', async () => {
    let resolveImport!: (cv: CandidateCv) => void
    vi.mocked(cvApi.createFromProfile).mockImplementation(() => new Promise(resolve => { resolveImport = resolve }))
    const content = emptyCvContent('developer'); content.personalInfo.fullName = 'Nguyễn Văn An'
    const imported = savedCv({ title: 'CV từ hồ sơ', templateId: 'developer', language: 'vi', content })
    vi.mocked(cvApi.get).mockResolvedValue(imported)
    openEditor('/cv/new?template=developer&source=profile')
    await waitFor(() => expect(cvApi.createFromProfile).toHaveBeenCalledTimes(1))
    expect(screen.queryByRole('textbox', { name: 'Họ và tên' })).toBeNull()
    await act(async () => { resolveImport(imported) })
    await waitFor(() => expect(screen.getByRole('textbox', { name: 'Họ và tên' }).innerText).toBe('Nguyễn Văn An'))
    expect(cvApi.create).not.toHaveBeenCalled()
  })
  it('can retry a temporary load error without claiming the CV is missing', async () => {
    vi.mocked(cvApi.get).mockRejectedValueOnce(new AppError('Dịch vụ tạm thời chưa sẵn sàng.', { status: 503 }))
      .mockResolvedValueOnce(savedCv({ title: 'QA', templateId: 'classic', language: 'vi', content: emptyCvContent() }))
    openEditor('/cv/qa-cv/edit')
    await screen.findByText('Dịch vụ tạm thời chưa sẵn sàng.')
    fireEvent.click(screen.getByRole('button', { name: 'Tải lại CV' }))
    await screen.findByRole('textbox', { name: 'Họ và tên' })
  })
  it('acknowledges equal server content regardless of JSON object key order', async () => {
    const original = savedCv({ title: 'QA', templateId: 'classic', language: 'vi', content: emptyCvContent() })
    vi.mocked(cvApi.get).mockResolvedValue(original)
    const reorder = (value: unknown): unknown => Array.isArray(value) ? value.map(reorder) : value && typeof value === 'object'
      ? Object.fromEntries(Object.entries(value).reverse().map(([key, child]) => [key, reorder(child)])) : value
    vi.mocked(cvApi.update).mockImplementation(async (_id, payload) => reorder(savedCv(payload)) as CandidateCv)
    openEditor('/cv/qa-cv/edit')
    await screen.findByRole('textbox', { name: 'Họ và tên' })
    typeName('Nguyễn Văn An')
    fireEvent.click(screen.getByRole('button', { name: 'Lưu CV' }))
    await waitFor(() => expect(screen.getByText('Đã lưu', { exact: true })).toBeTruthy())
    expect(sessionStorage.getItem('recruitment.cv-draft.v1:qa-owner:qa-cv')).toBeNull()
  })
  it('does not claim a new blank CV was saved', () => {
    openEditor()
    expect(screen.queryByText('Đã lưu', { exact: true })).toBeNull()
  })

  it('preserves typing during first creation without a duplicate create', async () => {
    let resolveCreate!: (cv: CandidateCv) => void
    vi.mocked(cvApi.create).mockImplementation(() => new Promise(resolve => { resolveCreate = resolve }))
    vi.mocked(cvApi.update).mockImplementation(async (_id, payload) => savedCv(payload))
    openEditor()
    fireEvent.change(screen.getByRole('textbox', { name: 'Tên CV' }), { target: { value: 'QA first save' } })
    await waitFor(() => expect(cvApi.create).toHaveBeenCalledTimes(1), { timeout: 2500 })
    const first = savedCv(vi.mocked(cvApi.create).mock.calls[0][0])
    vi.mocked(cvApi.get).mockResolvedValue(first)
    typeName('Nguyễn Văn An — nhập khi đang lưu')
    await act(async () => { resolveCreate(first) })
    await waitFor(() => expect(screen.getByRole('textbox', { name: 'Họ và tên' }).innerText).toBe('Nguyễn Văn An — nhập khi đang lưu'))
    await waitFor(() => expect(cvApi.update).toHaveBeenCalled(), { timeout: 2500 })
    expect(vi.mocked(cvApi.update).mock.calls.at(-1)?.[1].content.personalInfo.fullName).toBe('Nguyễn Văn An — nhập khi đang lưu')
    expect(cvApi.create).toHaveBeenCalledTimes(1)
  })

  it('does not allow PDF to start a second create while autosave is pending', async () => {
    vi.mocked(cvApi.create).mockImplementation(() => new Promise(() => {}))
    openEditor(); typeName('Nguyễn Văn An')
    await waitFor(() => expect(cvApi.create).toHaveBeenCalledTimes(1), { timeout: 2500 })
    fireEvent.click(screen.getByRole('button', { name: 'PDF' }))
    await act(async () => { await Promise.resolve() })
    expect(cvApi.create).toHaveBeenCalledTimes(1)
  })

  it('does not automatically retry an unchanged rejected payload', async () => {
    vi.mocked(cvApi.create).mockRejectedValue(new Error('Backend offline'))
    openEditor(); typeName('Nguyễn Văn An')
    await waitFor(() => expect(cvApi.create).toHaveBeenCalledTimes(1), { timeout: 2500 })
    await act(async () => { await new Promise(resolve => setTimeout(resolve, 1100)) })
    expect(cvApi.create).toHaveBeenCalledTimes(1)
    expect(screen.getByText('Không thể lưu')).toBeTruthy()
  })

  it('can reveal a legacy custom section with visible=false', async () => {
    const content = emptyCvContent()
    content.customSections = [{ id: 'languages', title: 'Ngoại ngữ', visible: false, items: [{ name: 'Tiếng Việt', date: '', description: '' }] }]
    content.designConfig.sectionOrder.push('custom:languages')
    vi.mocked(cvApi.get).mockResolvedValue(savedCv({ title: 'Legacy', templateId: 'classic', language: 'vi', content }))
    openEditor('/cv/qa-cv/edit')
    await screen.findByRole('button', { name: 'Bố cục' })
    fireEvent.click(screen.getByRole('button', { name: 'Bố cục' }))
    fireEvent.click(screen.getByRole('button', { name: 'Hiện Ngoại ngữ' }))
    expect(screen.getByRole('textbox', { name: 'Tên section Ngoại ngữ' })).toBeTruthy()
  })

  it('recovers unsaved text after unmount before the autosave timer fires', () => {
    const first = openEditor(); typeName('Nguyễn Văn An — bản nháp')
    first.unmount(); openEditor()
    expect(screen.getByRole('button', { name: 'Khôi phục bản nháp' })).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'Khôi phục bản nháp' }))
    expect(screen.getByRole('textbox', { name: 'Họ và tên' }).innerText).toBe('Nguyễn Văn An — bản nháp')
    expect(cvApi.create).not.toHaveBeenCalled()
  })
})
