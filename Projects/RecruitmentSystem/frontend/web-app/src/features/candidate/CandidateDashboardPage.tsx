import { useQuery } from '@tanstack/react-query'
import {
  ArrowRight, Bell, BriefcaseBusiness, CalendarDays, CheckCircle2, CircleAlert,
  Clock3, FileText, RefreshCw, Search, Sparkles, UserRound, Workflow,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { ButtonLink } from '../../components/ui/Button'
import { getMyApplications } from '../applications/applications.api'
import { useAuth } from '../auth/auth-context'
import { AppError, getErrorMessage } from '../../lib/api/error-adapter'
import type { ApplicationStatus, ApplicationSummary } from '../../types/models/application'
import { getUnreadNotificationCount } from '../notifications/notifications.api'
import { getCandidateProfile, getCurrentResume } from './candidate.api'

const profileRoute = '/candidate/profile'
const resumeRoute = '/candidate/resumes'
const applicationsRoute = '/candidate/applications'

const applicationStatusLabels: Record<ApplicationStatus, string> = {
  APPLIED: 'Đã ứng tuyển',
  SCREENING: 'Đang sàng lọc',
  INTERVIEW: 'Phỏng vấn',
  OFFER: 'Đề nghị',
  HIRED: 'Đã tuyển',
  REJECTED: 'Từ chối',
  WITHDRAWN: 'Đã rút',
}

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
          <div><strong>Mã việc làm: {application.jobId.slice(0, 8)}</strong><small>Ứng tuyển ngày {formatDate(application.appliedAt)}</small></div>
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
