import { useQuery } from '@tanstack/react-query'
import { ArrowRight, BriefcaseBusiness, ChevronLeft, ChevronRight, CircleAlert, Inbox, RefreshCw } from 'lucide-react'
import { useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { getErrorMessage } from '../../lib/api/error-adapter'
import type { ApplicationSummary } from '../../types/models/application'
import { useAuth } from '../auth/auth-context'
import { getCompanyById } from '../companies/companies.api'
import { getJobById } from '../jobs/jobs.api'
import { applicationStatusLabels, formatApplicationDate } from './application-presenter'
import { getMyApplications } from './applications.api'
import './applications-page.css'

const allowedSizes = [10, 20, 30]

function ApplicationRow({ application }: { application: ApplicationSummary }) {
  const job = useQuery({ queryKey: ['job', application.jobId], queryFn: () => getJobById(application.jobId), retry: false })
  const company = useQuery({ queryKey: ['company', job.data?.companyId], queryFn: () => getCompanyById(job.data!.companyId), enabled: Boolean(job.data?.companyId), retry: false })

  return <article className="applications-list__item">
    <span className="applications-list__icon"><BriefcaseBusiness /></span>
    <div className="applications-list__main">
      {job.data ? <><h2>{job.data.title}</h2>{company.data && <p>{company.data.name}</p>}</> : <><h2>Đơn ứng tuyển {application.id.slice(0, 8)}</h2><p>Mã công việc: {application.jobId}</p></>}
      <small>Ứng tuyển: {formatApplicationDate(application.appliedAt)}</small>
    </div>
    <span className={`candidate-status-chip candidate-status-chip--${application.status.toLowerCase()}`}>{applicationStatusLabels[application.status]}</span>
    <Link to={`/candidate/applications/${application.id}`}>Xem <ArrowRight /></Link>
  </article>
}

export function ApplicationListPage() {
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const [searchParams, setSearchParams] = useSearchParams()
  const requestedPage = Number(searchParams.get('page'))
  const requestedSize = Number(searchParams.get('size'))
  const page = Number.isInteger(requestedPage) && requestedPage >= 0 ? requestedPage : 0
  const size = allowedSizes.includes(requestedSize) ? requestedSize : 10

  useEffect(() => {
    if (searchParams.get('page') !== String(page) || searchParams.get('size') !== String(size)) {
      setSearchParams({ page: String(page), size: String(size) }, { replace: true })
    }
  }, [page, searchParams, setSearchParams, size])

  const applications = useQuery({
    queryKey: ['candidate-applications', userId, { page, size }],
    queryFn: () => getMyApplications({ page, size }),
    enabled: Boolean(userId),
  })
  const changePage = (nextPage: number) => setSearchParams({ page: String(nextPage), size: String(size) })

  return <main className="applications-page">
    <header className="applications-page__header"><span>Candidate Portal</span><h1>Đơn ứng tuyển của tôi</h1><p>Theo dõi các cơ hội bạn đã ứng tuyển bằng dữ liệu trực tiếp từ hệ thống.</p></header>

    {applications.isPending && <div className="applications-state"><span className="applications-loading" /><p>Đang tải đơn ứng tuyển...</p></div>}
    {applications.isError && <div className="applications-state applications-state--error" role="alert"><CircleAlert /><h2>Chưa thể tải đơn ứng tuyển</h2><p>{getErrorMessage(applications.error)}</p><button type="button" onClick={() => void applications.refetch()}><RefreshCw /> Thử lại</button></div>}
    {applications.data && applications.data.content.length === 0 && <div className="applications-state"><Inbox /><h2>Bạn chưa có đơn ứng tuyển nào.</h2><p>Khám phá các công việc đang tuyển và bắt đầu hành trình mới.</p><Link to="/jobs">Tìm việc ngay</Link></div>}
    {applications.data && applications.data.content.length > 0 && <>
      <div className="applications-toolbar"><strong>{applications.data.totalElements} đơn ứng tuyển</strong><label>Hiển thị <select value={size} onChange={(event) => setSearchParams({ page: '0', size: event.target.value })}>{allowedSizes.map((value) => <option key={value} value={value}>{value}</option>)}</select></label></div>
      <section className="applications-list" aria-label="Danh sách đơn ứng tuyển">{applications.data.content.map((item) => <ApplicationRow key={item.id} application={item} />)}</section>
      <nav className="applications-pagination" aria-label="Phân trang đơn ứng tuyển"><button type="button" disabled={!applications.data.hasPrevious} onClick={() => changePage(page - 1)}><ChevronLeft /> Trước</button><span>Trang {applications.data.page + 1} / {Math.max(1, applications.data.totalPages)}</span><button type="button" disabled={!applications.data.hasNext} onClick={() => changePage(page + 1)}>Sau <ChevronRight /></button></nav>
    </>}
  </main>
}
