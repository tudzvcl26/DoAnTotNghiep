import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  AlertCircle, ArrowRight, Bell, BriefcaseBusiness, Building2, CheckCheck, CircleUserRound,
  Clock3, FileSearch, RefreshCw, ShieldCheck, Sparkles, UsersRound,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { getErrorMessage } from '../../lib/api/error-adapter'
import type { Notification } from '../../types/models/notification'
import { useAuth } from '../auth/auth-context'
import { getNotifications, getUnreadNotificationCount, markAllNotificationsRead, markNotificationRead } from '../notifications/notifications.api'
import { employerCompanyKey, getEmployerApplicationSummary, getEmployerCompanies, getPublishedCompanyJobs } from './employer.api'

function formatDate(value: string) {
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

function humanize(value: string | null) {
  return value ? value.replaceAll('_', ' ').toLocaleLowerCase('vi-VN') : 'Chưa cập nhật'
}

function Skeleton() {
  return <div className="employer-skeleton" aria-label="Đang tải dữ liệu"><span /><span /><span /></div>
}

function SectionError({ error, retry }: { error: unknown; retry: () => void }) {
  return <div className="employer-error" role="alert"><AlertCircle aria-hidden="true" /><div><strong>Chưa thể tải dữ liệu</strong><p>{getErrorMessage(error)}</p></div><button type="button" onClick={retry}><RefreshCw /> Thử lại</button></div>
}

function NotificationRow({ notification, onOpen, pending }: { notification: Notification; onOpen: (item: Notification) => void; pending: boolean }) {
  return <button type="button" className={`employer-activity${notification.read ? '' : ' is-unread'}`} onClick={() => onOpen(notification)} disabled={pending}>
    <span><Bell aria-hidden="true" /></span><span><strong>{notification.title}</strong><small>{notification.content}</small></span><time dateTime={notification.createdAt}>{formatDate(notification.createdAt)}</time>
  </button>
}

export function EmployerDashboardPage() {
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const queryClient = useQueryClient()
  const companies = useQuery({ queryKey: employerCompanyKey(userId), queryFn: () => getEmployerCompanies(userId), enabled: Boolean(userId) })
  const companyIds = companies.data?.map((company) => company.id) ?? []
  const jobs = useQuery({ queryKey: ['employer-published-jobs', companyIds], queryFn: () => getPublishedCompanyJobs(companyIds), enabled: companies.isSuccess })
  const applications = useQuery({ queryKey: ['employer-application-summary'], queryFn: getEmployerApplicationSummary })
  const notifications = useQuery({ queryKey: ['employer-notifications', userId], queryFn: () => getNotifications({ page: 0, size: 5 }), enabled: Boolean(userId) })
  const unread = useQuery({ queryKey: ['employer-notification-unread', userId], queryFn: getUnreadNotificationCount, enabled: Boolean(userId) })

  const refreshNotifications = async () => Promise.all([
    queryClient.invalidateQueries({ queryKey: ['employer-notifications', userId] }),
    queryClient.invalidateQueries({ queryKey: ['employer-notification-unread', userId] }),
  ])
  const markOne = useMutation({ mutationFn: markNotificationRead, onSuccess: refreshNotifications })
  const markAll = useMutation({ mutationFn: markAllNotificationsRead, onSuccess: refreshNotifications })
  const primaryCompany = companies.data?.[0]

  return <main className="employer-dashboard">
    <header className="employer-hero">
      <div><span><Sparkles aria-hidden="true" /> Employer workspace</span><h1>Chào {currentUser?.fullName ?? 'nhà tuyển dụng'},</h1><p>Theo dõi hiện trạng tuyển dụng bằng dữ liệu thực từ doanh nghiệp của bạn.</p></div>
      <div className="employer-hero__notice"><span><Bell aria-hidden="true" /></span><div><strong>{unread.isPending ? 'Đang cập nhật' : `${unread.data?.unreadCount ?? 0} thông báo chưa đọc`}</strong><p>Mọi cập nhật tài khoản và tuyển dụng sẽ xuất hiện tại đây.</p></div></div>
    </header>

    <section className="employer-metrics" aria-label="Tổng quan tuyển dụng">
      <article><span><Building2 /></span><div><small>Công ty sở hữu</small><strong>{companies.isPending ? '—' : companies.data?.length ?? 0}</strong></div></article>
      <article><span><BriefcaseBusiness /></span><div><small>Tin đang hiển thị</small><strong>{jobs.isPending ? '—' : jobs.data?.length ?? 0}</strong></div></article>
      <article><span><UsersRound /></span><div><small>Ứng tuyển toàn doanh nghiệp</small><strong>{applications.isPending ? '—' : applications.data?.total ?? 0}</strong></div></article>
      <article><span><Bell /></span><div><small>Thông báo chưa đọc</small><strong>{unread.isPending ? '—' : unread.data?.unreadCount ?? 0}</strong></div></article>
    </section>

    <div className="employer-dashboard__grid">
      <section className="employer-card employer-card--company" aria-labelledby="employer-company-title">
        <div className="employer-card__heading"><div><span>Hồ sơ doanh nghiệp</span><h2 id="employer-company-title">Công ty của bạn</h2></div><Link to="/employer/company">Xem chi tiết <ArrowRight /></Link></div>
        {companies.isPending && <Skeleton />}
        {companies.isError && <SectionError error={companies.error} retry={() => void companies.refetch()} />}
        {companies.isSuccess && !primaryCompany && <div className="employer-empty"><span><Building2 /></span><div><h3>Bạn chưa tạo công ty</h3><p>Company Service chưa có doanh nghiệp active thuộc tài khoản hiện tại.</p><Link to="/employer/company">Xem hướng dẫn <ArrowRight /></Link></div></div>}
        {primaryCompany && <div className="employer-company-summary"><div className="employer-company-summary__brand">{primaryCompany.logoUrl ? <img src={primaryCompany.logoUrl} alt="" /> : primaryCompany.name.slice(0, 2).toUpperCase()}</div><div><div className="employer-company-summary__title"><h3>{primaryCompany.name}</h3><span className={`employer-chip employer-chip--${primaryCompany.verificationStatus?.toLowerCase()}`}>{humanize(primaryCompany.verificationStatus)}</span></div><p>{primaryCompany.description || 'Doanh nghiệp chưa cập nhật phần giới thiệu.'}</p><dl><div><dt>Loại hình</dt><dd>{humanize(primaryCompany.companyType)}</dd></div><div><dt>Quy mô</dt><dd>{humanize(primaryCompany.companySize)}</dd></div><div><dt>Email</dt><dd>{primaryCompany.email ?? 'Chưa cập nhật'}</dd></div></dl></div></div>}
      </section>

      <section className="employer-card" aria-labelledby="employer-jobs-title">
        <div className="employer-card__heading"><div><span>Recruitment snapshot</span><h2 id="employer-jobs-title">Tin tuyển dụng</h2></div><Link to="/employer/jobs">Quản lý <ArrowRight /></Link></div>
        {companies.isSuccess && companyIds.length === 0 && <div className="employer-unavailable"><BriefcaseBusiness /><p>Cần có công ty trước khi hệ thống xác định tin tuyển dụng thuộc sở hữu.</p></div>}
        {jobs.isPending && <Skeleton />}
        {jobs.isError && <SectionError error={jobs.error} retry={() => void jobs.refetch()} />}
        {jobs.isSuccess && companyIds.length > 0 && jobs.data.length === 0 && <div className="employer-empty employer-empty--compact"><span><BriefcaseBusiness /></span><div><h3>Chưa có tin đang hiển thị</h3><p>API hiện chỉ expose tin PUBLISHED cho Employer. Draft và Closed chưa có contract truy vấn theo chủ sở hữu.</p></div></div>}
        {jobs.data && jobs.data.length > 0 && <div className="employer-job-list">{jobs.data.slice(0, 4).map((job) => <Link to={`/employer/jobs/${job.id}`} key={job.id}><span><BriefcaseBusiness /></span><div><strong>{job.title}</strong><small>{job.jobCode} · Đăng {job.publishedAt ? formatDate(job.publishedAt) : 'chưa xác định'}</small></div><em>PUBLISHED</em></Link>)}</div>}
        <p className="employer-contract-note"><ShieldCheck /> Số liệu chỉ phản ánh toàn bộ tin PUBLISHED được API hiện tại trả về, không bao gồm Draft/Closed.</p>
      </section>
    </div>

    <div className="employer-dashboard__grid">
      <section className="employer-card" aria-labelledby="employer-applications-title">
        <div className="employer-card__heading"><div><span>Ứng viên gần đây</span><h2 id="employer-applications-title">Ứng tuyển toàn doanh nghiệp</h2></div><Link to="/employer/applications">Quản lý <ArrowRight /></Link></div>
        {applications.isPending && <Skeleton />}
        {applications.isError && <SectionError error={applications.error} retry={() => void applications.refetch()} />}
        {applications.data?.recent.length === 0 && <div className="employer-empty employer-empty--compact"><span><CircleUserRound /></span><div><h3>Chưa có ứng tuyển</h3><p>Chưa có Application trên các Job thuộc doanh nghiệp.</p></div></div>}
        {applications.data && applications.data.recent.length > 0 && <div className="employer-application-list">{applications.data.recent.map((application) => <Link to={`/employer/applications/${application.id}`} key={application.id}><span><CircleUserRound /></span><div><strong>Ứng viên {application.candidateId.slice(0, 8)}</strong><small>Nộp lúc {formatDate(application.appliedAt)}</small></div><em>{humanize(application.status)}</em></Link>)}</div>}
        <p className="employer-contract-note"><FileSearch /> Số liệu được phân trang trực tiếp trên company-wide Application endpoint với ownership từ JWT.</p>
      </section>

      <section className="employer-card" aria-labelledby="employer-notifications-title">
        <div className="employer-card__heading"><div><span>Hoạt động mới</span><h2 id="employer-notifications-title">Thông báo</h2></div><button type="button" onClick={() => markAll.mutate()} disabled={!unread.data?.unreadCount || markAll.isPending}><CheckCheck /> Đọc tất cả</button></div>
        {notifications.isPending && <Skeleton />}
        {notifications.isError && <SectionError error={notifications.error} retry={() => void notifications.refetch()} />}
        {notifications.data?.content.length === 0 && <div className="employer-empty employer-empty--compact"><span><Bell /></span><div><h3>Chưa có thông báo</h3><p>Các cập nhật dành cho tài khoản Employer sẽ xuất hiện tại đây.</p></div></div>}
        {notifications.data && <div className="employer-activity-list">{notifications.data.content.map((item) => <NotificationRow key={item.id} notification={item} pending={markOne.isPending} onOpen={(notification) => { if (!notification.read) markOne.mutate(notification.id) }} />)}</div>}
      </section>
    </div>

    <section className="employer-card employer-next-phase" aria-labelledby="next-phase-title"><div><span><Clock3 /></span><div><h2 id="next-phase-title">Quy trình tuyển dụng</h2><p>Job Management và Application Management đã kết nối bằng dữ liệu backend có ownership.</p></div></div><Link to="/employer/applications">Xem ứng viên</Link></section>
  </main>
}
