import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
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
import { getJobById } from './jobs.api'
import './job-detail-page.css'

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

export function JobDetailsPage() {
  const { jobId } = useParams()
  const validJobId = Boolean(jobId && UUID_PATTERN.test(jobId))
  const { currentUser, isAuthenticated } = useAuth()
  const queryClient = useQueryClient()
  const userId = currentUser?.id ?? ''
  const isCandidate = currentUser?.roles.some((role) => ['CANDIDATE', 'ADMIN'].includes(normalizeRole(role))) ?? false
  const job = useQuery({ queryKey: ['job', jobId], queryFn: () => getJobById(jobId!), enabled: validJobId, retry: (count, error) => normalizeApiError(error).status !== 404 && count < 1 })
  const company = useQuery({ queryKey: ['company', job.data?.companyId], queryFn: () => getCompanyById(job.data!.companyId), enabled: Boolean(job.data?.companyId) })
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
    <section className="job-detail-page"><div className="container job-detail-layout"><JobDetailContent job={job.data} /><div className="job-detail-sidebar"><JobApplyCard jobId={job.data.id} jobTitle={job.data.title} currentUser={currentUser} currentResume={currentResume.data} resumePending={currentResume.isPending && Boolean(userId)} resumeError={currentResume.isError ? (normalizeApiError(currentResume.error).code === 'APP_010' || normalizeApiError(currentResume.error).status === 404 ? 'Bạn cần có CV hiện tại trước khi ứng tuyển.' : normalizeApiError(currentResume.error).message) : ''} isAuthenticated={isAuthenticated} isPending={application.isPending} applied={application.isSuccess || Boolean(existingApplication.data)} error={application.isError ? (normalizeApiError(application.error).code === 'APP_002' ? 'Bạn đã ứng tuyển công việc này.' : normalizeApiError(application.error).message) : ''} onApply={(coverLetter) => application.mutate(coverLetter)} /><JobCompanyCard company={company.data} companyId={job.data.companyId} isLoading={company.isPending} /></div></div></section>
  </>
}
