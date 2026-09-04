import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider, notifyManager } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { AiCareerPage, JsonResult } from './AiCareerPage'
import * as api from './ai-career.api'
import { getJobs } from '../jobs/jobs.api'
import { AppError } from '../../lib/api/error-adapter'
import type { AiResume, MatchExplanation, MatchingResult, ResumeAnalysis } from './ai-career.types'

vi.mock('./ai-career.api', async (importOriginal) => {
  const original = await importOriginal<typeof api>()
  return Object.fromEntries(Object.keys(original).map(key => [key, vi.fn()]))
})
vi.mock('../jobs/jobs.api', () => ({ getJobs: vi.fn() }))
// Promise-resolved query chains must flush React updates, not race several
// setTimeout(0) notification turns against RTL's one-second wall-clock deadline.
// This is test-only scheduling; production polling and request timers are intact.
const clients: QueryClient[] = []
function createTestClient() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  clients.push(client)
  return client
}
beforeEach(() => {
  notifyManager.setScheduler(queueMicrotask)
  notifyManager.setNotifyFunction(callback => { act(callback) })
  vi.mocked(api.getLatestInterviewTask).mockResolvedValue(null)
  vi.mocked(api.getInterviewPreparation).mockRejectedValue(new AppError('Chưa có kết quả', { status: 404 }))
  vi.mocked(api.getLatestExplanationTask).mockResolvedValue(null)
  vi.mocked(api.getMatchExplanation).mockRejectedValue(new AppError('Chưa có kết quả', { status: 404 }))
})
afterEach(() => {
  cleanup()
  clients.splice(0).forEach(client => client.clear())
  notifyManager.setScheduler(callback => setTimeout(callback, 0))
  notifyManager.setNotifyFunction(callback => callback())
  vi.resetAllMocks()
})
const page = <T,>(content: T[]) => ({ content, totalElements: content.length, totalPages: 1, number: 0, size: 20, first: true, last: true, empty: content.length === 0 })

describe('AI presentation and context', () => {
  it.each(['explanation', 'interview'] as const)('recovers a persisted %s after reload and retries failed reads without generating again', async kind => {
    const match = { id: 'read-match', resumeId: 'read-resume', jobId: 'job', overallScore: 81, scoreBreakdown: [], matchedSkills: [], missingSkills: [], strengths: [], gapAnalysis: [], updatedAt: '2026-08-30T00:00:00Z' } as unknown as MatchingResult
    vi.mocked(api.getAiResumes).mockResolvedValue(page([{ id: 'read-resume', originalFilename: 'QA.pdf', status: 'ANALYZED', fileSize: 100, uploadTime: '2026-08-30T00:00:00Z' }]) as never)
    vi.mocked(api.getAiTasks).mockResolvedValue(page([]) as never)
    vi.mocked(api.getJobRecommendations).mockResolvedValue(page([]) as never)
    vi.mocked(getJobs).mockResolvedValue(page([]) as never)
    vi.mocked(api.getAiResumeAnalysis).mockResolvedValue({ keywords: [], skills: [], structuredData: {}, scoreBreakdown: {} } as never)
    vi.mocked(api.getResumeMatches).mockResolvedValue(page([match]) as never)
    const latestTask = kind === 'explanation' ? api.getLatestExplanationTask : api.getLatestInterviewTask
    const readResult = kind === 'explanation' ? api.getMatchExplanation : api.getInterviewPreparation
    vi.mocked(latestTask).mockResolvedValue({ id: 'completed-task', status: 'COMPLETED' } as never)
    vi.mocked(readResult).mockRejectedValueOnce(new AppError('QA đọc kết quả bị gián đoạn', { status: 503 }))
      .mockResolvedValue({ matchId: 'read-match', providerName: 'test', modelName: 'test', generationDurationMs: 1, [kind === 'explanation' ? 'explanation' : 'questionSet']: { overallEvaluation: 'Kết quả đã lưu được tải lại' } } as never)
    const client = createTestClient()
    render(<QueryClientProvider client={client}><MemoryRouter><AiCareerPage /></MemoryRouter></QueryClientProvider>)
    await screen.findByText('QA đọc kết quả bị gián đoạn')
    fireEvent.click(screen.getByRole('button', { name: 'Thử lại' }))
    await screen.findByText('Kết quả đã lưu được tải lại')
    expect(readResult).toHaveBeenCalledTimes(2)
    expect(api.queueMatchExplanation).not.toHaveBeenCalled()
    expect(api.queueInterviewPreparation).not.toHaveBeenCalled()
  })

  it('translates structured labels and enum values and never prints null placeholders', () => {
    render(<JsonResult value={{ fullName: 'Nguyễn Văn An', phone: 'null', skills: ['Java', null, 'undefined'], priority: 'HIGH', unexpectedCamelCase: null }} />)
    const text = document.body.textContent ?? ''
    expect(text).toContain('Họ và tên'); expect(text).toContain('Cao')
    expect(text).not.toMatch(/null|undefined|HIGH|unexpectedCamelCase|Full Name/)
  })

  it('hides previous CV matches and late explanations after changing CV via dropdown', async () => {
    const resumes = ['a', 'b'].map(id => ({ id, originalFilename: `CV ${id}`, status: 'ANALYZED', fileSize: 100, uploadTime: '2026-08-30T00:00:00Z' } as AiResume))
    const match = { id: 'match-a', resumeId: 'a', jobId: 'job', overallScore: 81, scoreBreakdown: [], matchedSkills: [], missingSkills: [], strengths: ['Kết quả riêng CV A'], gapAnalysis: [], updatedAt: '2026-08-30T00:00:00Z' } as unknown as MatchingResult
    vi.mocked(api.getAiResumes).mockResolvedValue(page(resumes) as never)
    vi.mocked(api.getAiTasks).mockResolvedValue(page([]) as never)
    vi.mocked(api.getJobRecommendations).mockResolvedValue(page([]) as never)
    vi.mocked(getJobs).mockResolvedValue(page([]) as never)
    vi.mocked(api.getAiResumeAnalysis).mockImplementation(async id => ({ id: `analysis-${id}`, aiTaskId: 'task', providerName: 'test', modelName: 'test', correlationId: 'qa', createdAt: '', updatedAt: '', keywords: [], resumeDocumentId: id, qualityScore: 50, scoreBreakdown: {}, skills: [], structuredData: {}, analysisDurationMs: 1 } satisfies ResumeAnalysis))
    vi.mocked(api.getResumeMatches).mockImplementation(async id => page(id === 'a' ? [match] : []) as never)
    let resolve!: (value: MatchExplanation) => void
    vi.mocked(api.getLatestExplanationTask).mockResolvedValue(null)
    vi.mocked(api.getMatchExplanation).mockImplementation(() => new Promise(done => { resolve = done }))
    const client = createTestClient()
    render(<QueryClientProvider client={client}><MemoryRouter><AiCareerPage /></MemoryRouter></QueryClientProvider>)
    await screen.findByText('Kết quả riêng CV A')
    await waitFor(() => expect(api.getMatchExplanation).toHaveBeenCalled())
    fireEvent.change(screen.getByRole('combobox', { name: 'CV dùng để đánh giá' }), { target: { value: 'b' } })
    await act(async () => { resolve({ id: 'explain-a', providerName: 'test', modelName: 'test', generationDurationMs: 1, correlationId: 'qa', createdAt: '', matchId: 'match-a', explanation: { summary: 'Phản hồi muộn của CV A' } }) })
    expect(screen.queryByText('Kết quả riêng CV A')).toBeNull()
    expect(screen.queryByText('Phản hồi muộn của CV A')).toBeNull()
  })
})
