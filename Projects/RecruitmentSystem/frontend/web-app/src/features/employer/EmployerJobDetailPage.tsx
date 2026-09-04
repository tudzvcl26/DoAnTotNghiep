import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertCircle, ArrowLeft, BriefcaseBusiness, CalendarDays, CheckCircle2, Edit3, RadioTower, RefreshCw, ShieldAlert, Trash2, UsersRound, XCircle } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Button, ButtonLink } from '../../components/ui/Button'
import { getErrorMessage, normalizeApiError } from '../../lib/api/error-adapter'
import type { JobDetail } from '../../types/models/job'
import { useAuth } from '../auth/auth-context'
import {
  closeEmployerJob, deleteEmployerJob, employerCompanyKey, employerJobKey, employerJobsKey,
  employerJobStatisticsKey, employerPublishedJobsKey, getEmployerCompanies, getEmployerJob, publishEmployerJob,
} from './employer.api'

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i
const statusLabels = { DRAFT: 'Bản nháp', PUBLISHED: 'Đang tuyển', CLOSED: 'Đã đóng', EXPIRED: 'Hết hạn' }
const valueLabels: Record<string, string> = { FULL_TIME: 'Toàn thời gian', PART_TIME: 'Bán thời gian', INTERNSHIP: 'Thực tập', FREELANCE: 'Freelance', CONTRACT: 'Hợp đồng', TEMPORARY: 'Tạm thời', NO_EXPERIENCE: 'Không yêu cầu kinh nghiệm', FRESHER: 'Fresher', JUNIOR: 'Junior', MIDDLE: 'Middle', SENIOR: 'Senior', LEADER: 'Leader', MANAGER: 'Manager' }
const date = new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' })
const money = new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 2 })

function salary(job: JobDetail) {
  if (job.salaryMin == null && job.salaryMax == null) return 'Thỏa thuận'
  if (job.salaryMin != null && job.salaryMax != null) return `${money.format(job.salaryMin)} – ${money.format(job.salaryMax)} ${job.currency ?? ''}`
  return `${money.format(job.salaryMin ?? job.salaryMax ?? 0)} ${job.currency ?? ''}`
}

function TextSection({ title, content }: { title: string; content: string | null }) {
  if (!content) return null
  return <section className="employer-job-detail__text"><h2>{title}</h2>{content.split(/\r?\n/).filter(Boolean).map((line, index) => <p key={`${title}-${index}`}>{line}</p>)}</section>
}

export function EmployerJobDetailPage() {
  const { jobId = '' } = useParams()
  const validId = UUID.test(jobId)
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const companies = useQuery({ queryKey: employerCompanyKey(userId), queryFn: () => getEmployerCompanies(userId), enabled: Boolean(userId) })
  const job = useQuery({ queryKey: employerJobKey(jobId), queryFn: () => getEmployerJob(jobId), enabled: validId, retry: (count, error) => normalizeApiError(error).status !== 404 && count < 1 })
  const ownedCompany = companies.data?.find((company) => company.id === job.data?.companyId)
  const refresh = async (updated: JobDetail) => {
    queryClient.setQueryData(employerJobKey(jobId), updated)
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: employerJobsKey }),
      queryClient.invalidateQueries({ queryKey: employerPublishedJobsKey }),
      queryClient.invalidateQueries({ queryKey: employerJobStatisticsKey }),
    ])
  }
  const publish = useMutation({ mutationFn: () => publishEmployerJob(jobId), onSuccess: refresh })
  const close = useMutation({ mutationFn: () => closeEmployerJob(jobId), onSuccess: refresh })
  const remove = useMutation({
    mutationFn: () => deleteEmployerJob(jobId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: employerJobsKey }),
        queryClient.invalidateQueries({ queryKey: employerPublishedJobsKey }),
        queryClient.invalidateQueries({ queryKey: employerJobStatisticsKey }),
      ])
      queryClient.removeQueries({ queryKey: employerJobKey(jobId) })
      navigate('/employer/jobs', { replace: true, state: { success: 'Đã ngừng kích hoạt việc làm.' } })
    },
  })

  if (!validId) return <main className="employer-job-state"><AlertCircle /><h1>ID việc làm không hợp lệ</h1><ButtonLink to="/employer/jobs">Quay lại danh sách</ButtonLink></main>
  if (job.isPending || companies.isPending) return <main className="employer-jobs-page"><div className="employer-job-skeleton" aria-label="Đang tải chi tiết"><span /><span /><span /></div></main>
  if (job.isError) { const status = normalizeApiError(job.error).status; return <main className="employer-job-state"><AlertCircle /><h1>{status === 403 ? 'Không có quyền quản lý việc làm' : status === 404 ? 'Không tìm thấy việc làm' : 'Không thể tải việc làm'}</h1><p>{getErrorMessage(job.error)}</p><div><ButtonLink to="/employer/jobs" variant="secondary"><ArrowLeft /> Danh sách</ButtonLink><Button type="button" onClick={() => void job.refetch()}><RefreshCw /> Thử lại</Button></div></main> }
  if (!ownedCompany) return <main className="employer-job-state"><ShieldAlert /><h1>Việc làm không thuộc doanh nghiệp của bạn</h1><p>Tin đang tuyển có thể đọc công khai, nhưng Employer Portal không cung cấp thao tác quản lý khi ownership không khớp.</p><ButtonLink to="/employer/jobs">Quay lại danh sách</ButtonLink></main>

  const actionError = publish.error ?? close.error ?? remove.error
  return <main className="employer-job-detail">
    <nav aria-label="Breadcrumb"><Link to="/employer/jobs"><ArrowLeft /> Quản lý tuyển dụng</Link><span>/</span><span aria-current="page">{job.data.jobCode}</span></nav>
    <header><div><span className={`employer-job-status employer-job-status--${job.data.status.toLowerCase()}`}>{statusLabels[job.data.status]}</span><h1>{job.data.title}</h1><p>{ownedCompany.name} · {job.data.jobCode}</p></div><div><ButtonLink to={`/employer/jobs/${jobId}/edit`} variant="secondary"><Edit3 /> Chỉnh sửa</ButtonLink>{job.data.status === 'DRAFT' && <Button type="button" disabled={publish.isPending || remove.isPending} onClick={() => { if (window.confirm('Đăng tuyển việc làm này? Nội dung sẽ xuất hiện công khai.')) publish.mutate() }}><CheckCircle2 /> {publish.isPending ? 'Đang đăng...' : 'Đăng tuyển'}</Button>}{job.data.status === 'PUBLISHED' && <Button type="button" variant="dark" disabled={close.isPending || remove.isPending} onClick={() => { if (window.confirm('Đóng việc làm này? Tin sẽ không còn hiển thị công khai.')) close.mutate() }}><XCircle /> {close.isPending ? 'Đang đóng...' : 'Đóng tin'}</Button>}<Button className="employer-job-deactivate" type="button" variant="secondary" disabled={publish.isPending || close.isPending || remove.isPending} onClick={() => { if (window.confirm(`Ngừng kích hoạt “${job.data.title}”? Thao tác này không thể hoàn tác trong Employer Portal.`)) remove.mutate() }}><Trash2 /> {remove.isPending ? 'Đang xử lý...' : 'Ngừng kích hoạt'}</Button></div></header>
    {actionError && <div className="employer-error" role="alert"><AlertCircle /><div><strong>Không thể cập nhật trạng thái</strong><p>{getErrorMessage(actionError)}</p></div></div>}
    <section className="employer-job-detail__facts"><article><BriefcaseBusiness /><small>Danh mục</small><strong>{job.data.categoryName ?? 'Chưa cập nhật'}</strong></article><article><RadioTower /><small>Hình thức</small><strong>{job.data.employmentType ? valueLabels[job.data.employmentType] : 'Chưa cập nhật'}</strong></article><article><UsersRound /><small>Số lượng</small><strong>{job.data.quantity ?? 1} vị trí</strong></article><article><CalendarDays /><small>Hạn ứng tuyển</small><strong>{job.data.applicationDeadline ? new Intl.DateTimeFormat('vi-VN').format(new Date(job.data.applicationDeadline)) : 'Không giới hạn'}</strong></article></section>
    <div className="employer-job-detail__layout"><article><TextSection title="Mô tả công việc" content={job.data.description} /><TextSection title="Trách nhiệm" content={job.data.responsibilities} /><TextSection title="Yêu cầu ứng viên" content={job.data.requirements} />{!job.data.description && !job.data.responsibilities && !job.data.requirements && <section className="employer-job-detail__text"><h2>Nội dung tuyển dụng</h2><p>Chưa cập nhật nội dung chi tiết.</p></section>}</article><aside><h2>Thông tin vận hành</h2><dl><div><dt>Mức lương</dt><dd>{salary(job.data)}</dd></div><div><dt>Kinh nghiệm</dt><dd>{job.data.experienceLevel ? valueLabels[job.data.experienceLevel] : 'Chưa cập nhật'}</dd></div><div><dt>Làm từ xa</dt><dd>{job.data.remoteAllowed ? 'Có' : 'Không'}</dd></div><div><dt>Ngày tạo</dt><dd>{date.format(new Date(job.data.createdAt))}</dd></div><div><dt>Ngày đăng</dt><dd>{job.data.publishedAt ? date.format(new Date(job.data.publishedAt)) : 'Chưa đăng'}</dd></div><div><dt>Cập nhật</dt><dd>{date.format(new Date(job.data.updatedAt))}</dd></div></dl></aside></div>
  </main>
}
