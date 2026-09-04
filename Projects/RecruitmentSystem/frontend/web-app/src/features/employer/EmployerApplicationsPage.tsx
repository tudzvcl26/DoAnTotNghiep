import { useQuery } from '@tanstack/react-query'
import { AlertCircle, ChevronLeft, ChevronRight, FileUser, Inbox, RefreshCw, ShieldCheck } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { getErrorMessage } from '../../lib/api/error-adapter'
import { APPLICATION_STATUSES, type ApplicationSummary, type ApplicationStatus } from '../../types/models/application'
import { employerApplicationStatusLabels, formatEmployerApplicationDate } from './employer-application.presenter'
import { employerApplicationsKey, getEmployerApplications } from './employer.api'

const allowedSizes = [10, 20, 30]
const allowedSorts = ['appliedAt,desc', 'appliedAt,asc', 'updatedAt,desc']
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

function ApplicationCard({ application }: { application: ApplicationSummary }) {
  const candidate = application.candidateProfileSnapshot
  return <article className="employer-application-card"><span><FileUser /></span><div><div><span className={`employer-application-status employer-application-status--${application.status.toLowerCase()}`}>{employerApplicationStatusLabels[application.status]}</span><small>{candidate?.headline || `Candidate ${application.candidateId.slice(0, 8)}`}</small></div><h2>{candidate?.displayName || `Ứng viên ${application.candidateId.slice(0, 8)}`}</h2><dl><div><dt>Ngày ứng tuyển</dt><dd>{formatEmployerApplicationDate(application.appliedAtInstant ?? application.appliedAt)}</dd></div><div><dt>Cập nhật</dt><dd>{formatEmployerApplicationDate(application.updatedAtInstant ?? application.updatedAt)}</dd></div><div><dt>Mã đơn</dt><dd>{application.id.slice(0, 8)}</dd></div></dl></div><Link to={`/employer/applications/${application.id}`}>Xem hồ sơ</Link></article>
}

export function EmployerApplicationsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const requestedPage = Number(searchParams.get('page') ?? 0)
  const requestedSize = Number(searchParams.get('size') ?? 10)
  const rawStatus = searchParams.get('status') as ApplicationStatus | null
  const page = Number.isInteger(requestedPage) && requestedPage >= 0 ? requestedPage : 0
  const size = allowedSizes.includes(requestedSize) ? requestedSize : 10
  const requestedSort = searchParams.get('sort') ?? 'appliedAt,desc'
  const sort = allowedSorts.includes(requestedSort) ? requestedSort : 'appliedAt,desc'
  const status = rawStatus && APPLICATION_STATUSES.includes(rawStatus) ? rawStatus : undefined
  const rawJobId = searchParams.get('jobId') ?? ''
  const jobId = UUID.test(rawJobId) ? rawJobId : undefined
  const applications = useQuery({
    queryKey: [...employerApplicationsKey, { page, size, sort, status, jobId }],
    queryFn: () => getEmployerApplications({ page, size, sort, status, jobId }), placeholderData: (previous) => previous,
  })
  const updateParams = (values: Record<string, string | number | undefined>) => {
    const next = new URLSearchParams(searchParams)
    Object.entries(values).forEach(([key, value]) => value == null || value === '' ? next.delete(key) : next.set(key, String(value)))
    setSearchParams(next)
  }

  return <main className="employer-applications-page">
    <header><div><span>Employer Portal</span><h1>Quản lý ứng viên</h1><p>Xem đơn ứng tuyển theo từng Job thuộc doanh nghiệp và xử lý đúng state machine backend.</p></div><FileUser /></header>
    <aside className="employer-job-contract"><ShieldCheck /><p>Danh sách được phân trang trực tiếp trên Application Service và tự xác định Company từ Employer đang đăng nhập. Frontend không gửi ownerId để quyết định quyền.</p></aside>
    <section className="employer-application-toolbar">
      <label>Job ID<input value={rawJobId} placeholder="Để trống để xem toàn Company" onChange={(event) => updateParams({ jobId: event.target.value.trim(), page: 0 })} />{rawJobId && !jobId && <small>Job ID chưa đúng định dạng UUID.</small>}</label>
      <label>Trạng thái<select value={status ?? ''} onChange={(event) => updateParams({ status: event.target.value, page: 0 })}><option value="">Tất cả trạng thái</option>{APPLICATION_STATUSES.map((value) => <option value={value} key={value}>{employerApplicationStatusLabels[value]}</option>)}</select></label>
      <label>Hiển thị<select value={size} onChange={(event) => updateParams({ size: event.target.value, page: 0 })}>{allowedSizes.map((value) => <option value={value} key={value}>{value} đơn</option>)}</select></label>
      <label>Sắp xếp<select value={sort} onChange={(event) => updateParams({ sort: event.target.value, page: 0 })}><option value="appliedAt,desc">Mới ứng tuyển trước</option><option value="appliedAt,asc">Cũ nhất trước</option><option value="updatedAt,desc">Mới cập nhật trước</option></select></label>
    </section>
    {applications.isPending && <div className="employer-job-skeleton" aria-label="Đang tải danh sách ứng viên"><span /><span /><span /></div>}
    {applications.isError && <div className="employer-error" role="alert"><AlertCircle /><div><strong>Không thể tải đơn ứng tuyển</strong><p>{getErrorMessage(applications.error)}</p></div><button type="button" onClick={() => void applications.refetch()}><RefreshCw /> Thử lại</button></div>}
    {applications.data && applications.data.content.length === 0 && <section className="employer-company-empty"><span><Inbox /></span><h2>Chưa có đơn ứng tuyển</h2><p>Không có Application phù hợp với Job và trạng thái đã chọn.</p></section>}
    {applications.data && applications.data.content.length > 0 && <><section className="employer-application-cards" aria-label="Danh sách ứng viên">{applications.data.content.map((application) => <ApplicationCard key={application.id} application={application} />)}</section><nav className="employer-application-pagination" aria-label="Phân trang ứng viên"><button type="button" disabled={!applications.data.hasPrevious} onClick={() => updateParams({ page: page - 1 })}><ChevronLeft /> Trước</button><span>Trang {applications.data.page + 1} / {Math.max(1, applications.data.totalPages)} · {applications.data.totalElements} đơn</span><button type="button" disabled={!applications.data.hasNext} onClick={() => updateParams({ page: page + 1 })}>Sau <ChevronRight /></button></nav></>}
  </main>
}
