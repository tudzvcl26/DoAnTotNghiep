import { useQueries, useQuery } from '@tanstack/react-query'
import {
  ArrowRight, Bell, BriefcaseBusiness, CalendarDays, CheckCircle2, CircleAlert,
  Clock3, FileText, RefreshCw, Search, Sparkles, UserRound, Workflow,
} from 'lucide-react'
import { type FormEvent, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ButtonLink } from '../../components/ui/Button'
import { getMyApplications } from '../applications/applications.api'
import { applicationStatusLabels } from '../applications/application-presenter'
import { useAuth } from '../auth/auth-context'
import { AppError, getErrorMessage } from '../../lib/api/error-adapter'
import type { ApplicationStatus, ApplicationSummary } from '../../types/models/application'
import { getUnreadNotificationCount } from '../notifications/notifications.api'
import { getCompanyById } from '../companies/companies.api'
import { getFeaturedJobs } from '../jobs/jobs.api'
import { JobCard } from '../jobs/components/JobCard'
import { getCandidateProfile, getCurrentResume } from './candidate.api'
import '../jobs/jobs-page.css'

const profileRoute = '/candidate/profile'
const resumeRoute = '/candidate/resumes'
const applicationsRoute = '/candidate/applications'

function isNotFound(error: unknown) {
  return error instanceof AppError && error.status === 404
}

function retryQuery(count: number, error: Error) {
  return !isNotFound(error) && count < 1
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(new Date(value))
}

function formatFileSize(bytes: number) {
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function SectionError({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <div className="candidate-section-error" role="alert">
      <CircleAlert size={21} aria-hidden="true" />
      <div><strong>Chưa thể tải dữ liệu</strong><p>{message}</p></div>
      <button type="button" onClick={onRetry}><RefreshCw size={16} /> Thử lại</button>
    </div>
  )
}

function CardSkeleton() {
  return <div className="candidate-card-skeleton" aria-label="Đang tải dữ liệu"><span /><span /><span /></div>
}

function ProfileCard() {
  const { currentUser } = useAuth()
  const profileQuery = useQuery({
    queryKey: ['candidate-profile'],
    queryFn: getCandidateProfile,
    retry: retryQuery,
  })

  return (
    <article className="candidate-dashboard-card candidate-dashboard-card--profile">
      <div className="candidate-card-heading"><span><UserRound size={18} /> Hồ sơ ứng viên</span><Link to={profileRoute}>Chi tiết <ArrowRight size={15} /></Link></div>
      {profileQuery.isLoading && <CardSkeleton />}
      {profileQuery.isError && !isNotFound(profileQuery.error) && (
        <SectionError message={getErrorMessage(profileQuery.error)} onRetry={() => void profileQuery.refetch()} />
      )}
      {profileQuery.isError && isNotFound(profileQuery.error) && (
        <div className="candidate-empty-card">
          <span className="candidate-empty-card__icon"><UserRound size={24} /></span>
          <div><h2>Bạn chưa tạo hồ sơ ứng viên.</h2><p>Hoàn thiện hồ sơ để tăng cơ hội tìm được công việc phù hợp.</p></div>
          <ButtonLink to={profileRoute} size="sm">Tạo hồ sơ</ButtonLink>
        </div>
      )}
      {profileQuery.data && (
        <div className="candidate-profile-content">
          <div className="candidate-profile-identity">
            <span className="candidate-profile-avatar">
              {currentUser?.avatarUrl ? <img src={currentUser.avatarUrl} alt={`Ảnh đại diện ${profileQuery.data.displayName}`} /> : profileQuery.data.displayName.trim().charAt(0).toUpperCase()}
            </span>
            <div><h2>{profileQuery.data.displayName}</h2><p>{profileQuery.data.headline || 'Hồ sơ đã được tạo'}</p></div>
          </div>
          {typeof profileQuery.data.completionScore === 'number' && (
            <div className="candidate-profile-score">
              <div><span>Mức độ hoàn thiện</span><strong>{profileQuery.data.completionScore}%</strong></div>
              <progress max="100" value={profileQuery.data.completionScore}>{profileQuery.data.completionScore}%</progress>
            </div>
          )}
          {profileQuery.data.summary && <p className="candidate-profile-summary">{profileQuery.data.summary}</p>}
          <ButtonLink to={profileRoute} variant="secondary" size="sm">Cập nhật hồ sơ</ButtonLink>
        </div>
      )}
    </article>
  )
}

function ResumeCard({ userId }: { userId: string }) {
  const resumeQuery = useQuery({
    queryKey: ['candidate-current-resume', userId],
    queryFn: () => getCurrentResume(userId),
    enabled: Boolean(userId),
    retry: retryQuery,
  })
  const currentResumeMissing = resumeQuery.isError && isNotFound(resumeQuery.error)

  return (
    <article className="candidate-dashboard-card candidate-dashboard-card--resume">
      <div className="candidate-card-heading"><span><FileText size={18} /> CV hiện tại</span><Link to={resumeRoute}>Quản lý <ArrowRight size={15} /></Link></div>
      {resumeQuery.isLoading && <CardSkeleton />}
      {resumeQuery.isError && !isNotFound(resumeQuery.error) && (
        <SectionError message={getErrorMessage(resumeQuery.error)} onRetry={() => void resumeQuery.refetch()} />
      )}
      {currentResumeMissing && (
        <div className="candidate-empty-card">
          <span className="candidate-empty-card__icon"><FileText size={24} /></span>
          <div><h2>Bạn chưa có CV.</h2><p>Tải CV lên để sẵn sàng ứng tuyển khi tìm thấy cơ hội phù hợp.</p></div>
          <ButtonLink to={resumeRoute} size="sm">Tải CV lên</ButtonLink>
        </div>
      )}
      {resumeQuery.data && !currentResumeMissing && (
        <div className="candidate-resume-content">
          <span className="candidate-resume-file"><FileText size={25} /></span>
          <div><h2>{resumeQuery.data.originalFilename}</h2><p>{formatFileSize(resumeQuery.data.sizeBytes)} · Phiên bản {resumeQuery.data.assetVersion} · Tải lên {formatDate(resumeQuery.data.createdAt)}</p></div>
          <span className="candidate-status-chip">CV hiện tại</span>
          <ButtonLink to={resumeRoute} variant="secondary" size="sm">Quản lý CV</ButtonLink>
        </div>
      )}
    </article>
  )
}

function NotificationSummary({ userId }: { userId: string }) {
  const notificationQuery = useQuery({
    queryKey: ['candidate-notification-unread', userId],
    queryFn: getUnreadNotificationCount,
    enabled: Boolean(userId),
  })

  return (
    <article className="candidate-notification-card">
      <span className="candidate-notification-card__icon"><Bell size={21} /></span>
      <div><span>Thông báo</span>
        {notificationQuery.isLoading && <p>Đang kiểm tra thông báo...</p>}
        {notificationQuery.data && <p>{notificationQuery.data.unreadCount > 0 ? `Bạn có ${notificationQuery.data.unreadCount} thông báo chưa đọc.` : 'Bạn không có thông báo mới.'}</p>}
        {notificationQuery.isError && <p className="candidate-inline-error">Chưa thể tải thông báo. <button type="button" onClick={() => void notificationQuery.refetch()}>Thử lại</button></p>}
      </div>
      <Link to="/candidate/notifications" aria-label="Xem tất cả thông báo"><ArrowRight size={17} /></Link>
    </article>
  )
}

function CandidateJobDiscovery() {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')
  const [location, setLocation] = useState('')
  const jobs = useQuery({ queryKey: ['jobs', 'candidate-dashboard', 'latest'], queryFn: getFeaturedJobs })
  const companyIds = useMemo(() => [...new Set(jobs.data?.content.map((job) => job.companyId) ?? [])], [jobs.data?.content])
  const companyQueries = useQueries({ queries: companyIds.map((companyId) => ({ queryKey: ['company', companyId], queryFn: () => getCompanyById(companyId), staleTime: 5 * 60_000 })) })
  const companies = new Map(companyQueries.flatMap((query, index) => query.data ? [[companyIds[index], query.data] as const] : []))

  const submit = (event: FormEvent) => {
    event.preventDefault()
    const params = new URLSearchParams()
    if (keyword.trim()) params.set('keyword', keyword.trim())
    if (location.trim()) params.set('location', location.trim())
    navigate(`/jobs${params.size ? `?${params}` : ''}`)
  }

  return <>
    <section className="candidate-job-search" aria-labelledby="candidate-job-search-title">
      <div><span><Search size={16} /> Tìm cơ hội tiếp theo</span><h2 id="candidate-job-search-title">Bắt đầu từ công việc bạn muốn</h2><p>Tìm theo vị trí và địa điểm trên danh sách tuyển dụng đang công khai.</p></div>
      <form onSubmit={submit} role="search"><label><span>Vị trí hoặc kỹ năng</span><input maxLength={120} value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="Ví dụ: Java Developer" /></label><label><span>Địa điểm</span><input maxLength={100} value={location} onChange={(event) => setLocation(event.target.value)} placeholder="Ví dụ: Hồ Chí Minh" /></label><button type="submit"><Search size={17} /> Tìm việc</button></form>
    </section>
    <section className="candidate-dashboard-section candidate-latest-jobs">
      <div className="candidate-section-heading"><div><span>Cơ hội mới</span><h2>Việc làm mới đăng</h2></div><Link to="/jobs">Xem tất cả <ArrowRight size={16} /></Link></div>
      {jobs.isPending && <CardSkeleton />}
      {jobs.isError && <SectionError message={getErrorMessage(jobs.error)} onRetry={() => void jobs.refetch()} />}
      {jobs.data?.content.length === 0 && <div className="candidate-applications-empty"><span><BriefcaseBusiness size={28} /></span><div><h3>Chưa có việc làm công khai.</h3><p>Quay lại sau để xem các cơ hội mới.</p></div></div>}
      {jobs.data && jobs.data.content.length > 0 && <div className="candidate-latest-jobs__grid">{jobs.data.content.slice(0, 4).map((job) => <JobCard key={job.id} job={job} company={companies.get(job.companyId)} />)}</div>}
      <div className="candidate-ai-recommendation"><Sparkles size={18} /><div><strong>Cần gợi ý cá nhân hóa?</strong><p>AI Career dùng CV đã phân tích và consent của bạn để tạo Job Recommendations có lưu trữ.</p></div><Link to="/candidate/ai-career">Mở AI Career <ArrowRight size={15} /></Link></div>
    </section>
  </>
}

function ApplicationOverview({ applications, total }: { applications: ApplicationSummary[]; total: number }) {
  const count = (statuses: ApplicationStatus[]) => applications.filter((application) => statuses.includes(application.status)).length
  const hasCompleteDataset = total <= applications.length
  const stats = [{ label: 'Tổng đơn', value: total, icon: BriefcaseBusiness }]
  if (hasCompleteDataset) {
    stats.push(
      { label: 'Đang xử lý', value: count(['APPLIED', 'SCREENING', 'OFFER']), icon: Clock3 },
      { label: 'Phỏng vấn', value: count(['INTERVIEW']), icon: CalendarDays },
      { label: 'Đã tuyển', value: count(['HIRED']), icon: CheckCircle2 },
      { label: 'Đã từ chối', value: count(['REJECTED']), icon: CircleAlert },
    )
  }
  return <div className="candidate-stats">{stats.map(({ label, value, icon: Icon }) => <article key={label}><span><Icon size={19} /></span><div><small>{label}</small><strong>{value}</strong></div></article>)}</div>
}

function RecentApplications({ applications }: { applications: ApplicationSummary[] }) {
  if (!applications.length) {
    return (
      <div className="candidate-applications-empty">
        <span><Workflow size={28} /></span><div><h3>Bạn chưa có đơn ứng tuyển nào.</h3><p>Khám phá các cơ hội đang mở và bắt đầu hành trình mới.</p></div>
        <ButtonLink to="/jobs" size="sm"><Search size={16} /> Tìm việc</ButtonLink>
      </div>
    )
  }

  return (
    <div className="candidate-application-list">
      {applications.slice(0, 5).map((application) => (
        <Link to={`/candidate/applications/${application.id}`} key={application.id}>
          <span className="candidate-application-list__icon"><BriefcaseBusiness size={19} /></span>
          <div><strong>Mã việc làm: {application.jobId.slice(0, 8)}</strong><small>Ứng tuyển ngày {formatDate(application.appliedAtInstant ?? application.appliedAt)}</small></div>
          <span className={`candidate-status-chip candidate-status-chip--${application.status.toLowerCase()}`}>{applicationStatusLabels[application.status]}</span>
          <ArrowRight size={17} aria-hidden="true" />
        </Link>
      ))}
    </div>
  )
}

const quickActions = [
  { label: 'Tìm việc', description: 'Khám phá cơ hội mới', to: '/jobs', icon: Search },
  { label: 'Cập nhật hồ sơ', description: 'Hoàn thiện thông tin nghề nghiệp', to: profileRoute, icon: UserRound },
  { label: 'Quản lý CV', description: 'Tải lên và quản lý CV', to: resumeRoute, icon: FileText },
  { label: 'Đơn ứng tuyển', description: 'Theo dõi hành trình', to: applicationsRoute, icon: Workflow },
  { label: 'AI Career', description: 'Phân tích CV và gợi ý nghề nghiệp', to: '/candidate/ai-career', icon: Sparkles },
]

export function CandidateDashboardPage() {
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const applicationsQuery = useQuery({
    queryKey: ['candidate-applications', userId, { page: 0, size: 20 }],
    queryFn: () => getMyApplications({ page: 0, size: 20 }),
    enabled: Boolean(userId),
  })

  return (
    <div className="candidate-dashboard">
      <section className="candidate-greeting">
        <div><span className="candidate-greeting__eyebrow"><Sparkles size={15} /> Career dashboard</span><h1>Xin chào, {currentUser?.fullName}</h1><p>Chuẩn bị hồ sơ, khám phá cơ hội và theo dõi hành trình ứng tuyển của bạn.</p></div>
        <NotificationSummary userId={userId} />
      </section>

      <CandidateJobDiscovery />

      <section className="candidate-primary-grid" aria-label="Trạng thái hồ sơ và CV">
        <ProfileCard />
        <ResumeCard userId={userId} />
      </section>

      <section className="candidate-dashboard-section">
        <div className="candidate-section-heading"><div><span>Hành trình ứng tuyển</span><h2>Tổng quan đơn ứng tuyển</h2></div><Link to={applicationsRoute}>Xem tất cả <ArrowRight size={16} /></Link></div>
        {applicationsQuery.isLoading && <CardSkeleton />}
        {applicationsQuery.isError && <SectionError message={getErrorMessage(applicationsQuery.error)} onRetry={() => void applicationsQuery.refetch()} />}
        {applicationsQuery.data && <ApplicationOverview applications={applicationsQuery.data.content} total={applicationsQuery.data.totalElements} />}
      </section>

      <section className="candidate-dashboard-section">
        <div className="candidate-section-heading"><div><span>Cập nhật gần nhất</span><h2>Đơn ứng tuyển gần đây</h2></div></div>
        {applicationsQuery.isLoading && <CardSkeleton />}
        {applicationsQuery.isError && <SectionError message={getErrorMessage(applicationsQuery.error)} onRetry={() => void applicationsQuery.refetch()} />}
        {applicationsQuery.data && <RecentApplications applications={applicationsQuery.data.content} />}
      </section>

      <section className="candidate-dashboard-section">
        <div className="candidate-section-heading"><div><span>Lối tắt hữu ích</span><h2>Thao tác nhanh</h2></div></div>
        <div className="candidate-quick-actions">{quickActions.map(({ label, description, to, icon: Icon }) => <Link key={to} to={to}><span><Icon size={20} /></span><div><strong>{label}</strong><small>{description}</small></div><ArrowRight size={17} /></Link>)}</div>
      </section>
    </div>
  )
}
