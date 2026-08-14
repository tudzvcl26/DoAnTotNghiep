import { useQuery } from '@tanstack/react-query'
import { AlertCircle, BriefcaseBusiness, CalendarDays, ChevronLeft, ChevronRight, Edit3, Eye, Plus, RefreshCw, Search, ShieldCheck } from 'lucide-react'
import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { ButtonLink } from '../../components/ui/Button'
import { getErrorMessage } from '../../lib/api/error-adapter'
import type { JobStatus, JobSummary } from '../../types/models/job'
import { useAuth } from '../auth/auth-context'
import { employerCompanyKey, employerJobsKey, getEmployerCompanies, getEmployerJobs } from './employer.api'

const statusLabels = { DRAFT: 'Bản nháp', PUBLISHED: 'Đang tuyển', CLOSED: 'Đã đóng', EXPIRED: 'Hết hạn' }
const date = new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium' })

function JobCard({ job }: { job: JobSummary }) {
  return <article className="employer-job-card"><div className="employer-job-card__icon"><BriefcaseBusiness /></div><div className="employer-job-card__main"><div><span className={`employer-job-status employer-job-status--${job.status.toLowerCase()}`}>{statusLabels[job.status]}</span><small>{job.jobCode}</small></div><h2>{job.title}</h2><dl><div><dt>Danh mục</dt><dd>{job.categoryName ?? 'Chưa cập nhật'}</dd></div><div><dt>Hạn ứng tuyển</dt><dd><CalendarDays /> {job.applicationDeadline ? date.format(new Date(job.applicationDeadline)) : 'Không giới hạn'}</dd></div><div><dt>Ngày đăng</dt><dd>{job.publishedAt ? date.format(new Date(job.publishedAt)) : 'Chưa đăng'}</dd></div></dl></div><div className="employer-job-card__actions"><Link to={`/employer/jobs/${job.id}`}><Eye /> Chi tiết</Link><Link to={`/employer/jobs/${job.id}/edit`}><Edit3 /> Chỉnh sửa</Link></div></article>
}

export function EmployerJobsPage() {
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const [searchParams, setSearchParams] = useSearchParams()
  const keyword = searchParams.get('keyword')?.trim() ?? ''
  const status = (searchParams.get('status') ?? '') as JobStatus | ''
  const companyId = searchParams.get('companyId') ?? ''
  const sort = searchParams.get('sort') ?? 'updatedAt,desc'
  const page = Math.max(0, Number(searchParams.get('page') ?? 0) || 0)
  const [draftKeyword, setDraftKeyword] = useState(keyword)
  const companies = useQuery({ queryKey: employerCompanyKey(userId), queryFn: () => getEmployerCompanies(userId), enabled: Boolean(userId) })
  const companyIds = companies.data?.map((company) => company.id) ?? []
  const jobs = useQuery({
    queryKey: [...employerJobsKey, { page, keyword, status, companyId, sort }],
    queryFn: () => getEmployerJobs({ page, size: 10, sort, ...(keyword ? { keyword } : {}), ...(status ? { status } : {}), ...(companyId ? { companyId } : {}) }),
    enabled: companies.isSuccess,
  })

  const updateParams = (updates: Record<string, string>) => {
    const next = new URLSearchParams(searchParams)
    Object.entries(updates).forEach(([key, value]) => value ? next.set(key, value) : next.delete(key))
    if (!('page' in updates)) next.delete('page')
    setSearchParams(next)
  }
  const submitSearch = (event: React.FormEvent) => { event.preventDefault(); updateParams({ keyword: draftKeyword.trim() }) }

  return <main className="employer-jobs-page">
    <header><div><span>Employer Portal</span><h1>Quản lý tuyển dụng</h1><p>Quản lý bản nháp, tin đang tuyển và tin đã đóng thuộc doanh nghiệp của bạn.</p></div><ButtonLink to="/employer/jobs/new"><Plus /> Tạo việc làm</ButtonLink></header>
    <section className="employer-job-toolbar">
      <form onSubmit={submitSearch} role="search"><label><span className="sr-only">Tìm theo tiêu đề hoặc mã việc làm</span><Search /><input value={draftKeyword} onChange={(event) => setDraftKeyword(event.target.value)} placeholder="Tìm theo tiêu đề hoặc mã..." /></label><button type="submit">Tìm kiếm</button></form>
      <div className="employer-job-filters"><label>Trạng thái<select value={status} onChange={(event) => updateParams({ status: event.target.value })}><option value="">Tất cả</option><option value="DRAFT">Bản nháp</option><option value="PUBLISHED">Đang tuyển</option><option value="CLOSED">Đã đóng</option></select></label><label>Doanh nghiệp<select value={companyId} onChange={(event) => updateParams({ companyId: event.target.value })}><option value="">Tất cả</option>{companies.data?.map((company) => <option key={company.id} value={company.id}>{company.name}</option>)}</select></label><label>Sắp xếp<select value={sort} onChange={(event) => updateParams({ sort: event.target.value })}><option value="updatedAt,desc">Cập nhật mới nhất</option><option value="createdAt,desc">Tạo mới nhất</option><option value="title,asc">Tên A–Z</option><option value="publishedAt,desc">Đăng mới nhất</option></select></label></div>
      <div><strong>{jobs.data?.totalElements ?? 0}</strong><span>việc làm thuộc sở hữu</span></div>
    </section>
    <aside className="employer-job-contract"><ShieldCheck /><p>Recruitment Service giới hạn danh sách theo ownership từ JWT; tìm kiếm, lọc và phân trang được thực hiện tại backend.</p></aside>
    {companies.isPending && <div className="employer-job-skeleton" aria-label="Đang tải doanh nghiệp"><span /><span /><span /></div>}
    {companies.isError && <div className="employer-error" role="alert"><AlertCircle /><div><strong>Không thể xác định doanh nghiệp</strong><p>{getErrorMessage(companies.error)}</p></div><button type="button" onClick={() => void companies.refetch()}><RefreshCw /> Thử lại</button></div>}
    {companies.isSuccess && companyIds.length === 0 && <section className="employer-company-empty"><span><BriefcaseBusiness /></span><h2>Cần có doanh nghiệp trước</h2><p>Tạo Company hợp lệ để backend có thể xác minh ownership khi tạo việc làm.</p><ButtonLink to="/employer/company">Quản lý công ty</ButtonLink></section>}
    {jobs.isPending && companies.isSuccess && companyIds.length > 0 && <div className="employer-job-skeleton" aria-label="Đang tải việc làm"><span /><span /><span /></div>}
    {jobs.isError && <div className="employer-error" role="alert"><AlertCircle /><div><strong>Không thể tải việc làm</strong><p>{getErrorMessage(jobs.error)}</p></div><button type="button" onClick={() => void jobs.refetch()}><RefreshCw /> Thử lại</button></div>}
    {jobs.isSuccess && jobs.data.content.length === 0 && companyIds.length > 0 && <section className="employer-company-empty"><span><BriefcaseBusiness /></span><h2>Không có kết quả phù hợp</h2><p>Thay đổi từ khóa hoặc bộ lọc, hoặc tạo một bản nháp mới.</p><ButtonLink to="/employer/jobs/new"><Plus /> Tạo bản nháp</ButtonLink></section>}
    {jobs.data && jobs.data.content.length > 0 && <><section className="employer-job-cards" aria-label="Danh sách việc làm">{jobs.data.content.map((job) => <JobCard key={job.id} job={job} />)}</section><nav className="employer-application-pagination" aria-label="Phân trang việc làm"><button type="button" disabled={!jobs.data.hasPrevious} onClick={() => updateParams({ page: String(page - 1) })}><ChevronLeft /> Trang trước</button><span>Trang {jobs.data.page + 1} / {Math.max(1, jobs.data.totalPages)}</span><button type="button" disabled={!jobs.data.hasNext} onClick={() => updateParams({ page: String(page + 1) })}>Trang sau <ChevronRight /></button></nav></>}
  </main>
}
