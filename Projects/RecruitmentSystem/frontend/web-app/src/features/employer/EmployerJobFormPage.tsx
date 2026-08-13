import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertCircle, BriefcaseBusiness, RefreshCw, ShieldAlert } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { ButtonLink } from '../../components/ui/Button'
import { getErrorMessage, normalizeApiError } from '../../lib/api/error-adapter'
import type { JobMutationRequest } from '../../types/models/job'
import { useAuth } from '../auth/auth-context'
import { EmployerJobForm } from './components/EmployerJobForm'
import {
  createEmployerJob, employerCompanyKey, employerJobKey, getEmployerCompanies, getEmployerJob, getJobCategories, updateEmployerJob,
} from './employer.api'

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

export function EmployerJobFormPage() {
  const { jobId } = useParams()
  const editing = Boolean(jobId)
  const validId = !editing || UUID.test(jobId!)
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const companies = useQuery({ queryKey: employerCompanyKey(userId), queryFn: () => getEmployerCompanies(userId), enabled: Boolean(userId) })
  const categories = useQuery({ queryKey: ['job-categories', 'active'], queryFn: getJobCategories })
  const job = useQuery({ queryKey: employerJobKey(jobId ?? ''), queryFn: () => getEmployerJob(jobId!), enabled: editing && validId, retry: (count, error) => normalizeApiError(error).status !== 404 && count < 1 })
  const selectedCompany = editing ? companies.data?.find((company) => company.id === job.data?.companyId) : companies.data?.[0]
  const complete = async (id: string) => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['employer-jobs'] }),
      queryClient.invalidateQueries({ queryKey: ['employer-published-jobs'] }),
      queryClient.invalidateQueries({ queryKey: employerJobKey(id) }),
    ])
    navigate(`/employer/jobs/${id}`, { replace: true })
  }
  const create = useMutation({ mutationFn: createEmployerJob, onSuccess: (created) => complete(created.id) })
  const update = useMutation({ mutationFn: (request: JobMutationRequest) => updateEmployerJob(jobId!, request), onSuccess: (updated) => complete(updated.id) })

  if (!validId) return <main className="employer-job-state"><AlertCircle /><h1>ID việc làm không hợp lệ</h1><ButtonLink to="/employer/jobs">Quay lại danh sách</ButtonLink></main>
  if (companies.isPending || categories.isPending || (editing && job.isPending)) return <main className="employer-jobs-page"><div className="employer-job-skeleton" aria-label="Đang tải biểu mẫu"><span /><span /><span /></div></main>
  const loadingError = companies.error ?? categories.error ?? job.error
  if (loadingError) return <main className="employer-job-state"><AlertCircle /><h1>Không thể chuẩn bị biểu mẫu</h1><p>{getErrorMessage(loadingError)}</p><button type="button" onClick={() => { void companies.refetch(); void categories.refetch(); if (editing) void job.refetch() }}><RefreshCw /> Thử lại</button></main>
  if (!selectedCompany) return <main className="employer-job-state"><ShieldAlert /><h1>{editing ? 'Việc làm không thuộc doanh nghiệp của bạn' : 'Bạn chưa có doanh nghiệp'}</h1><p>{editing ? 'Ownership không khớp nên biểu mẫu chỉnh sửa bị khóa.' : 'Backend yêu cầu companyId thuộc Employer đang đăng nhập.'}</p><ButtonLink to={editing ? '/employer/jobs' : '/employer/company'}>{editing ? 'Quay lại danh sách' : 'Quản lý công ty'}</ButtonLink></main>
  if (!categories.data?.length) return <main className="employer-job-state"><BriefcaseBusiness /><h1>Chưa có danh mục hoạt động</h1><p>Recruitment Service yêu cầu categoryId hợp lệ nhưng hiện không có danh mục để chọn.</p><ButtonLink to="/employer/jobs">Quay lại danh sách</ButtonLink></main>

  return <main className="employer-job-form-page"><EmployerJobForm job={job.data} companyId={selectedCompany.id} companyName={selectedCompany.name} categories={categories.data} pending={create.isPending || update.isPending} error={create.error ?? update.error} onSubmit={(request) => editing ? update.mutate(request) : create.mutate(request)} /></main>
}
