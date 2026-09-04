import { useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowRight, BriefcaseBusiness } from 'lucide-react'
import { useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useParams } from 'react-router-dom'
import { applyForJob, findMyApplicationForJob } from '../applications/applications.api'
import { useAuth } from '../auth/auth-context'
import { getCurrentResume } from '../candidate/candidate.api'
import { getCompanyById } from '../companies/companies.api'
import { normalizeApiError } from '../../lib/api/error-adapter'
import { normalizeRole } from '../../types/enums/auth'
import { JobApplyCard } from './components/JobApplyCard'
import { JobCompanyCard } from './components/JobCompanyCard'
import { JobDetailContent } from './components/JobDetailContent'
import { JobDetailHero } from './components/JobDetailHero'
import { JobDetailError, JobDetailSkeleton } from './components/JobDetailStates'
import { JobCard } from './components/JobCard'
import { getJobById, getJobs } from './jobs.api'
import './job-detail-page.css'
import './jobs-page.css'

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

export function JobDetailsPage() {
  const { jobId } = useParams()
  // Retire the mutation observer and dialog when moving between job routes.
  // A late response must keep the submitted job's cache key and callbacks.
  return <JobDetailsContent key={jobId} />
}

function JobDetailsContent() {
  const { jobId } = useParams()
  const validJobId = Boolean(jobId && UUID_PATTERN.test(jobId))
  const { currentUser, isAuthenticated } = useAuth()
  const queryClient = useQueryClient()
  const userId = currentUser?.id ?? ''
  const isCandidate = currentUser?.roles.some((role) => ['CANDIDATE', 'ADMIN'].includes(normalizeRole(role))) ?? false
  const job = useQuery({ queryKey: ['job', jobId], queryFn: () => getJobById(jobId!), enabled: validJobId, retry: (count, error) => normalizeApiError(error).status !== 404 && count < 1 })
  const company = useQuery({ queryKey: ['company', job.data?.companyId], queryFn: () => getCompanyById(job.data!.companyId), enabled: Boolean(job.data?.companyId) })
  const relatedJobs = useQuery({
    queryKey: ['jobs', 'related', job.data?.categoryId, jobId],
    queryFn: () => getJobs({ keyword: '', categoryId: job.data!.categoryId!, page: 0, size: 6, sort: 'publishedAt,desc' }),
    enabled: Boolean(job.data?.categoryId),
  })
  const visibleRelatedJobs = useMemo(() => relatedJobs.data?.content.filter((item) => item.id !== jobId).slice(0, 3) ?? [], [relatedJobs.data?.content, jobId])
  const relatedCompanyIds = useMemo(() => [...new Set(visibleRelatedJobs.map((item) => item.companyId).filter((id) => id !== job.data?.companyId))], [visibleRelatedJobs, job.data?.companyId])
  const relatedCompanyQueries = useQueries({ queries: relatedCompanyIds.map((companyId) => ({ queryKey: ['company', companyId], queryFn: () => getCompanyById(companyId), staleTime: 5 * 60_000 })) })
  const relatedCompanies = new Map(relatedCompanyQueries.flatMap((query, index) => query.data ? [[relatedCompanyIds[index], query.data] as const] : []))
  const currentResume = useQuery({ queryKey: ['candidate-current-resume', userId], queryFn: () => getCurrentResume(userId), enabled: Boolean(userId) && isCandidate })
  const existingApplication = useQuery({ queryKey: ['candidate-job-application', userId, jobId], queryFn: () => findMyApplicationForJob(jobId!), enabled: validJobId && Boolean(userId) && isCandidate })
  const application = useMutation({
    mutationFn: (coverLetter?: string) => applyForJob({ jobId: jobId!, ...(coverLetter ? { coverLetter } : {}) }),
    onSuccess: async (created) => {
      queryClient.setQueryData(['candidate-job-application', userId, jobId], created)
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['candidate-applications', userId] }),
        queryClient.invalidateQueries({ queryKey: ['job', jobId] }),
      ])
    },
  })

  if (!validJobId) return <JobDetailError notFound onRetry={() => undefined} />
  if (job.isPending) return <JobDetailSkeleton />
  if (job.isError) return <JobDetailError notFound={normalizeApiError(job.error).status === 404} onRetry={() => job.refetch()} />

  return <>
    <JobDetailHero job={job.data} company={company.data} />
    <section className="job-detail-page"><div className="container job-detail-layout"><JobDetailContent job={job.data} /><div className="job-detail-sidebar"><JobApplyCard jobId={job.data.id} jobTitle={job.data.title} currentUser={currentUser} currentResume={currentResume.data} resumePending={currentResume.isPending && Boolean(userId)} resumeError={currentResume.isError ? (normalizeApiError(currentResume.error).code === 'APP_010' || normalizeApiError(currentResume.error).status === 404 ? 'Bạn cần có CV hiện tại trước khi ứng tuyển.' : normalizeApiError(currentResume.error).message) : ''} isAuthenticated={isAuthenticated} isPending={application.isPending} applied={application.isSuccess || Boolean(existingApplication.data)} error={application.isError ? (normalizeApiError(application.error).code === 'APP_002' ? 'Bạn đã ứng tuyển công việc này.' : normalizeApiError(application.error).message) : ''} onApply={(coverLetter) => application.mutate(coverLetter)} /><JobCompanyCard company={company.data} companyId={job.data.companyId} isLoading={company.isPending} /></div></div>
      {visibleRelatedJobs.length > 0 && <section className="job-related" aria-labelledby="job-related-title"><div className="job-related__heading"><div><span><BriefcaseBusiness size={16} /> Cùng ngành nghề</span><h2 id="job-related-title">Việc làm liên quan</h2></div><Link to={`/jobs?categoryId=${job.data.categoryId}`}>Xem thêm <ArrowRight size={16} /></Link></div><div className="job-related__grid">{visibleRelatedJobs.map((item) => <JobCard key={item.id} job={item} company={item.companyId === company.data?.id ? company.data : relatedCompanies.get(item.companyId)} />)}</div></section>}
    </section>
  </>
}
