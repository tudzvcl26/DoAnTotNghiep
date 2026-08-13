import { useQuery } from '@tanstack/react-query'
import { AlertCircle, BriefcaseBusiness, ChevronLeft, ChevronRight, FileUser, Inbox, RefreshCw, ShieldCheck } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { ButtonLink } from '../../components/ui/Button'
import { getErrorMessage } from '../../lib/api/error-adapter'
import { APPLICATION_STATUSES, type ApplicationSummary, type ApplicationStatus } from '../../types/models/application'
import type { JobSummary } from '../../types/models/job'
import { useAuth } from '../auth/auth-context'
import { employerApplicationStatusLabels, formatEmployerApplicationDate } from './employer-application.presenter'
import {
  employerApplicationsKey, employerCompanyKey, getEmployerCompanies, getEmployerJobApplications, getPublishedCompanyJobs,
} from './employer.api'

const allowedSizes = [10, 20, 30]

function ApplicationCard({ application, job }: { application: ApplicationSummary; job: JobSummary }) {
  return <article className="employer-application-card"><span><FileUser /></span><div><div><span className={`employer-application-status employer-application-status--${application.status.toLowerCase()}`}>{employerApplicationStatusLabels[application.status]}</span><small>Ứng viên {application.candidateId.slice(0, 8)}</small></div><h2>{job.title}</h2><dl><div><dt>Ngày ứng tuyển</dt><dd>{formatEmployerApplicationDate(application.appliedAt)}</dd></div><div><dt>Cập nhật</dt><dd>{formatEmployerApplicationDate(application.updatedAt)}</dd></div><div><dt>Mã đơn</dt><dd>{application.id.slice(0, 8)}</dd></div></dl></div><Link to={`/employer/applications/${application.id}`}>Xem hồ sơ</Link></article>
}

export function EmployerApplicationsPage() {
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const [searchParams, setSearchParams] = useSearchParams()
  const requestedPage = Number(searchParams.get('page') ?? 0)
  const requestedSize = Number(searchParams.get('size') ?? 10)
  const rawStatus = searchParams.get('status') as ApplicationStatus | null
  const page = Number.isInteger(requestedPage) && requestedPage >= 0 ? requestedPage : 0
  const size = allowedSizes.includes(requestedSize) ? requestedSize : 10
  const status = rawStatus && APPLICATION_STATUSES.includes(rawStatus) ? rawStatus : undefined
  const companies = useQuery({ queryKey: employerCompanyKey(userId), queryFn: () => getEmployerCompanies(userId), enabled: Boolean(userId) })
  const companyIds = companies.data?.map((company) => company.id) ?? []
  const jobs = useQuery({ queryKey: ['employer-published-jobs', companyIds], queryFn: () => getPublishedCompanyJobs(companyIds), enabled: companies.isSuccess })
  const requestedJobId = searchParams.get('jobId') ?? ''
  const selectedJob = jobs.data?.find((job) => job.id === requestedJobId) ?? jobs.data?.[0]
  const applications = useQuery({
    queryKey: [...employerApplicationsKey(selectedJob?.id ?? ''), { page, size, status }],
    queryFn: () => getEmployerJobApplications(selectedJob!.id, { page, size, status }), enabled: Boolean(selectedJob), placeholderData: (previous) => previous,
  })
  const updateParams = (values: Record<string, string | number | undefined>) => {
    const next = new URLSearchParams(searchParams)
    Object.entries(values).forEach(([key, value]) => value == null || value === '' ? next.delete(key) : next.set(key, String(value)))
    setSearchParams(next)
  }

  return <main className="employer-applications-page">
    <header><div><span>Employer Portal</span><h1>Quản lý ứng viên</h1><p>Xem đơn ứng tuyển theo từng Job thuộc doanh nghiệp và xử lý đúng state machine backend.</p></div><FileUser /></header>
    <aside className="employer-job-contract"><ShieldCheck /><p>Application Service chỉ cung cấp danh sách theo từng Job. Job selector chỉ chứa tin PUBLISHED đã xác định ownership; không có company-wide application endpoint.</p></aside>
    {(companies.isPending || jobs.isPending) && <div className="employer-job-skeleton" aria-label="Đang tải dữ liệu ứng tuyển"><span /><span /><span /></div>}
    {(companies.isError || jobs.isError) && <div className="employer-error" role="alert"><AlertCircle /><div><strong>Không thể chuẩn bị danh sách</strong><p>{getErrorMessage(companies.error ?? jobs.error)}</p></div><button type="button" onClick={() => { void companies.refetch(); void jobs.refetch() }}><RefreshCw /> Thử lại</button></div>}
    {jobs.isSuccess && jobs.data.length === 0 && <section className="employer-company-empty"><span><BriefcaseBusiness /></span><h2>Không có Job PUBLISHED để truy vấn</h2><p>Backend chưa có danh sách Application toàn Company và không cho truy vấn Draft/Closed Job qua endpoint Job-scoped hiện tại.</p><ButtonLink to="/employer/jobs">Quản lý việc làm</ButtonLink></section>}
    {selectedJob && <section className="employer-application-toolbar">
      <label>Việc làm<select value={selectedJob.id} onChange={(event) => updateParams({ jobId: event.target.value, page: 0 })}>{jobs.data?.map((job) => <option value={job.id} key={job.id}>{job.title} · {job.jobCode}</option>)}</select></label>
      <label>Trạng thái<select value={status ?? ''} onChange={(event) => updateParams({ status: event.target.value, page: 0 })}><option value="">Tất cả trạng thái</option>{APPLICATION_STATUSES.map((value) => <option value={value} key={value}>{employerApplicationStatusLabels[value]}</option>)}</select></label>
      <label>Hiển thị<select value={size} onChange={(event) => updateParams({ size: event.target.value, page: 0 })}>{allowedSizes.map((value) => <option value={value} key={value}>{value} đơn</option>)}</select></label>
    </section>}
    {applications.isPending && selectedJob && <div className="employer-job-skeleton" aria-label="Đang tải danh sách ứng viên"><span /><span /><span /></div>}
    {applications.isError && <div className="employer-error" role="alert"><AlertCircle /><div><strong>Không thể tải đơn ứng tuyển</strong><p>{getErrorMessage(applications.error)}</p></div><button type="button" onClick={() => void applications.refetch()}><RefreshCw /> Thử lại</button></div>}
    {applications.data && applications.data.content.length === 0 && <section className="employer-company-empty"><span><Inbox /></span><h2>Chưa có đơn ứng tuyển</h2><p>Không có Application phù hợp với Job và trạng thái đã chọn.</p></section>}
    {applications.data && applications.data.content.length > 0 && <><section className="employer-application-cards" aria-label="Danh sách ứng viên">{applications.data.content.map((application) => <ApplicationCard key={application.id} application={application} job={selectedJob!} />)}</section><nav className="employer-application-pagination" aria-label="Phân trang ứng viên"><button type="button" disabled={!applications.data.hasPrevious} onClick={() => updateParams({ page: page - 1 })}><ChevronLeft /> Trước</button><span>Trang {applications.data.page + 1} / {Math.max(1, applications.data.totalPages)} · {applications.data.totalElements} đơn</span><button type="button" disabled={!applications.data.hasNext} onClick={() => updateParams({ page: page + 1 })}>Sau <ChevronRight /></button></nav></>}
  </main>
}
