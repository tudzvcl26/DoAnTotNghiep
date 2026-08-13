import { useQuery } from '@tanstack/react-query'
import { AlertCircle, BriefcaseBusiness, CalendarDays, Edit3, Eye, Plus, RefreshCw, Search, ShieldCheck } from 'lucide-react'
import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { ButtonLink } from '../../components/ui/Button'
import { getErrorMessage } from '../../lib/api/error-adapter'
import type { JobSummary } from '../../types/models/job'
import { useAuth } from '../auth/auth-context'
import { employerCompanyKey, employerJobsKey, getEmployerCompanies, searchEmployerPublishedJobs } from './employer.api'

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
  const [draftKeyword, setDraftKeyword] = useState(keyword)
  const companies = useQuery({ queryKey: employerCompanyKey(userId), queryFn: () => getEmployerCompanies(userId), enabled: Boolean(userId) })
  const companyIds = companies.data?.map((company) => company.id) ?? []
  const jobs = useQuery({ queryKey: [...employerJobsKey(companyIds), keyword], queryFn: () => searchEmployerPublishedJobs(companyIds, keyword), enabled: companies.isSuccess })
  const submitSearch = (event: React.FormEvent) => { event.preventDefault(); const next = new URLSearchParams(); if (draftKeyword.trim()) next.set('keyword', draftKeyword.trim()); setSearchParams(next) }

  return <main className="employer-jobs-page">
    <header><div><span>Employer Portal</span><h1>Quản lý tuyển dụng</h1><p>Quản lý các tin PUBLISHED thuộc doanh nghiệp và khởi tạo bản nháp mới.</p></div><ButtonLink to="/employer/jobs/new"><Plus /> Tạo việc làm</ButtonLink></header>
    <section className="employer-job-toolbar"><form onSubmit={submitSearch} role="search"><label><span className="sr-only">Tìm theo tiêu đề việc làm</span><Search /><input value={draftKeyword} onChange={(event) => setDraftKeyword(event.target.value)} placeholder="Tìm theo tiêu đề..." /></label><button type="submit">Tìm kiếm</button></form><div><strong>{jobs.data?.length ?? 0}</strong><span>tin PUBLISHED thuộc sở hữu</span></div></section>
    <aside className="employer-job-contract"><ShieldCheck /><p>Recruitment Service chưa có endpoint danh sách theo owner. Trang này gọi search/list thật, đọc toàn bộ trang PUBLISHED rồi đối chiếu <code>companyId</code>; Draft và Closed chỉ truy cập được qua URL chi tiết đã biết.</p></aside>
    {companies.isPending && <div className="employer-job-skeleton" aria-label="Đang tải doanh nghiệp"><span /><span /><span /></div>}
    {companies.isError && <div className="employer-error" role="alert"><AlertCircle /><div><strong>Không thể xác định doanh nghiệp</strong><p>{getErrorMessage(companies.error)}</p></div><button type="button" onClick={() => void companies.refetch()}><RefreshCw /> Thử lại</button></div>}
    {companies.isSuccess && companyIds.length === 0 && <section className="employer-company-empty"><span><BriefcaseBusiness /></span><h2>Cần có doanh nghiệp trước</h2><p>Tạo Company hợp lệ để backend có thể xác minh ownership khi tạo việc làm.</p><ButtonLink to="/employer/company">Quản lý công ty</ButtonLink></section>}
    {jobs.isPending && companies.isSuccess && companyIds.length > 0 && <div className="employer-job-skeleton" aria-label="Đang tải việc làm"><span /><span /><span /></div>}
    {jobs.isError && <div className="employer-error" role="alert"><AlertCircle /><div><strong>Không thể tải việc làm</strong><p>{getErrorMessage(jobs.error)}</p></div><button type="button" onClick={() => void jobs.refetch()}><RefreshCw /> Thử lại</button></div>}
    {jobs.isSuccess && jobs.data.length === 0 && companyIds.length > 0 && <section className="employer-company-empty"><span><BriefcaseBusiness /></span><h2>{keyword ? 'Không có kết quả thuộc sở hữu' : 'Chưa có tin đang tuyển'}</h2><p>{keyword ? 'Backend search không trả về tin PUBLISHED phù hợp của doanh nghiệp.' : 'Tạo một bản nháp, kiểm tra nội dung rồi đăng tuyển khi sẵn sàng.'}</p><ButtonLink to="/employer/jobs/new"><Plus /> Tạo bản nháp</ButtonLink></section>}
    {jobs.data && jobs.data.length > 0 && <section className="employer-job-cards" aria-label="Danh sách việc làm">{jobs.data.map((job) => <JobCard key={job.id} job={job} />)}</section>}
  </main>
}
