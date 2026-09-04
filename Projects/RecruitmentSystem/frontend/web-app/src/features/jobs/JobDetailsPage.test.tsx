import { afterEach, expect, it, vi } from 'vitest'
import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes, useNavigate } from 'react-router-dom'
import { JobDetailsPage } from './JobDetailsPage'
import { applyForJob } from '../applications/applications.api'

const first = '11111111-1111-4111-8111-111111111111'
const second = '22222222-2222-4222-8222-222222222222'
vi.mock('../auth/auth-context', () => ({ useAuth: () => ({ currentUser: { id: 'qa', roles: ['CANDIDATE'] }, isAuthenticated: true }) }))
vi.mock('../candidate/candidate.api', () => ({ getCurrentResume: vi.fn().mockResolvedValue({ id: 'cv' }) }))
vi.mock('../companies/companies.api', () => ({ getCompanyById: vi.fn().mockResolvedValue({ id: 'company' }) }))
vi.mock('../applications/applications.api', () => ({ applyForJob: vi.fn(), findMyApplicationForJob: vi.fn().mockResolvedValue(null) }))
vi.mock('./jobs.api', () => ({ getJobById: vi.fn(async (id: string) => ({ id, title: id, companyId: 'company' })), getJobs: vi.fn().mockResolvedValue({ content: [] }) }))
vi.mock('./components/JobDetailHero', () => ({ JobDetailHero: () => null }))
vi.mock('./components/JobDetailContent', () => ({ JobDetailContent: () => null }))
vi.mock('./components/JobCompanyCard', () => ({ JobCompanyCard: () => null }))
vi.mock('./components/JobApplyCard', () => ({ JobApplyCard: ({ jobId, applied, isPending, onApply }: { jobId: string; applied: boolean; isPending: boolean; onApply: () => void }) =>
  <div><span>{jobId}</span><span>{applied ? 'Already applied' : 'Not applied'}</span><button disabled={isPending} onClick={() => onApply()}>Apply</button></div> }))
afterEach(() => { cleanup(); vi.clearAllMocks() })

function setup() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  function Next() { const navigate = useNavigate(); return <button onClick={() => navigate(`/jobs/${second}`)}>Next job</button> }
  render(<QueryClientProvider client={client}><MemoryRouter initialEntries={[`/jobs/${first}`]}><Next /><Routes><Route path="/jobs/:jobId" element={<JobDetailsPage />} /></Routes></MemoryRouter></QueryClientProvider>)
  return client
}

it('does not carry a successful application into a different job route', async () => {
  vi.mocked(applyForJob).mockResolvedValue({ id: 'application', jobId: first } as never)
  setup()
  fireEvent.click(await screen.findByText('Apply'))
  await screen.findByText('Already applied')
  fireEvent.click(screen.getByText('Next job'))
  await screen.findByText(second)
  expect(screen.getByText('Not applied')).toBeTruthy()
})

it('keeps a late application response scoped to the job submitted before navigation', async () => {
  let resolve!: (value: never) => void
  vi.mocked(applyForJob).mockImplementation(() => new Promise(done => { resolve = done }))
  const client = setup()
  fireEvent.click(await screen.findByText('Apply'))
  await waitFor(() => expect(applyForJob).toHaveBeenCalled())
  fireEvent.click(screen.getByText('Next job'))
  await screen.findByText(second)
  await act(async () => resolve({ id: 'application', jobId: first } as never))
  expect(screen.getByText('Not applied')).toBeTruthy()
  expect(client.getQueryData(['candidate-job-application', 'qa', second])).toBeNull()
  expect(client.getQueryData(['candidate-job-application', 'qa', first])).toMatchObject({ jobId: first })
})
