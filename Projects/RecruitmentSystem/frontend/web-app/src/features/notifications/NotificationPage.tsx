import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Bell, BriefcaseBusiness, CheckCheck, ChevronLeft, ChevronRight, CircleAlert,
  Inbox, Megaphone, RefreshCw, ShieldCheck,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { getErrorMessage } from '../../lib/api/error-adapter'
import { normalizeRole } from '../../types/enums/auth'
import type { Notification, NotificationEventType } from '../../types/models/notification'
import { useAuth } from '../auth/auth-context'
import { notificationEventLabels } from '../admin/admin.labels'
import { getNotifications, getUnreadNotificationCount, markAllNotificationsRead, markNotificationRead } from './notifications.api'
import './notifications-page.css'

const allowedSizes = [10, 20, 30]
type ReadFilter = 'all' | 'unread'

function formatNotificationDate(value: string) {
  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function actionPath(notification: Notification, isEmployer: boolean) {
  return notification.relatedResourceType === 'APPLICATION' && notification.relatedResourceId
    ? `/${isEmployer ? 'employer' : 'candidate'}/applications/${notification.relatedResourceId}`
    : null
}

function NotificationIcon({ eventType }: { eventType: NotificationEventType }) {
  if (eventType.startsWith('APPLICATION_')) return <BriefcaseBusiness />
  if (eventType === 'SYSTEM_ANNOUNCEMENT') return <Megaphone />
  if (eventType === 'PASSWORD_CHANGED') return <ShieldCheck />
  return <Bell />
}

export function NotificationPage() {
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const isEmployer = currentUser?.roles.some((role) => normalizeRole(role) === 'EMPLOYER') ?? false
  const notificationScope = isEmployer ? 'employer' : 'candidate'
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const [actionError, setActionError] = useState('')
  const requestedPage = Number(searchParams.get('page'))
  const requestedSize = Number(searchParams.get('size'))
  const page = Number.isInteger(requestedPage) && requestedPage >= 0 ? requestedPage : 0
  const size = allowedSizes.includes(requestedSize) ? requestedSize : 10
  const filter: ReadFilter = searchParams.get('filter') === 'unread' ? 'unread' : 'all'
  const read = filter === 'unread' ? false : undefined

  useEffect(() => {
    if (searchParams.get('page') !== String(page) || searchParams.get('size') !== String(size) || searchParams.get('filter') !== filter) {
      setSearchParams({ page: String(page), size: String(size), filter }, { replace: true })
    }
  }, [filter, page, searchParams, setSearchParams, size])

  const notifications = useQuery({
    queryKey: [`${notificationScope}-notifications`, userId, { page, size, read }],
    queryFn: () => getNotifications({ page, size, read }),
    enabled: Boolean(userId),
  })
  const unread = useQuery({
    queryKey: [`${notificationScope}-notification-unread`, userId],
    queryFn: getUnreadNotificationCount,
    enabled: Boolean(userId),
  })

  const refreshNotificationQueries = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: [`${notificationScope}-notifications`, userId] }),
      queryClient.invalidateQueries({ queryKey: [`${notificationScope}-notification-unread`, userId] }),
    ])
  }

  const markOne = useMutation({
    mutationFn: markNotificationRead,
    onSuccess: refreshNotificationQueries,
  })
  const markAll = useMutation({
    mutationFn: markAllNotificationsRead,
    onSuccess: refreshNotificationQueries,
  })

  const openNotification = async (notification: Notification) => {
    setActionError('')
    try {
      if (!notification.read) await markOne.mutateAsync(notification.id)
      const target = actionPath(notification, isEmployer)
      if (target) navigate(target)
    } catch (error) {
      setActionError(getErrorMessage(error))
    }
  }

  const markEveryNotification = async () => {
    setActionError('')
    try {
      await markAll.mutateAsync()
    } catch (error) {
      setActionError(getErrorMessage(error))
    }
  }

  const changeFilter = (nextFilter: ReadFilter) => setSearchParams({ page: '0', size: String(size), filter: nextFilter })
  const changePage = (nextPage: number) => setSearchParams({ page: String(nextPage), size: String(size), filter })

  return <main className="notifications-page">
    <header className="notifications-page__header">
      <div><span>{isEmployer ? 'Employer Portal' : 'Candidate Portal'}</span><h1>Thông báo của tôi</h1><p>Cập nhật mới nhất từ hệ thống tuyển dụng, được sắp xếp theo thời gian.</p></div>
      <div className="notifications-page__count"><Bell /><strong>{unread.data?.unreadCount ?? 0}</strong><span>chưa đọc</span></div>
    </header>

    <div className="notifications-toolbar">
      <div className="notifications-filters" aria-label="Lọc thông báo">
        <button type="button" className={filter === 'all' ? 'is-active' : ''} onClick={() => changeFilter('all')}>Tất cả</button>
        <button type="button" className={filter === 'unread' ? 'is-active' : ''} onClick={() => changeFilter('unread')}>Chưa đọc</button>
      </div>
      <div className="notifications-toolbar__actions">
        <label>Hiển thị <select value={size} onChange={(event) => setSearchParams({ page: '0', size: event.target.value, filter })}>{allowedSizes.map((value) => <option key={value} value={value}>{value}</option>)}</select></label>
        <button type="button" onClick={() => void markEveryNotification()} disabled={!unread.data?.unreadCount || markAll.isPending}><CheckCheck /> {markAll.isPending ? 'Đang cập nhật...' : 'Đánh dấu tất cả đã đọc'}</button>
      </div>
    </div>

    {actionError && <div className="notifications-action-error" role="alert"><CircleAlert />{actionError}</div>}
    {notifications.isPending && <div className="notifications-state"><span className="notifications-loading" /><p>Đang tải thông báo...</p></div>}
    {notifications.isError && <div className="notifications-state notifications-state--error" role="alert"><CircleAlert /><h2>Chưa thể tải thông báo</h2><p>{getErrorMessage(notifications.error)}</p><button type="button" onClick={() => void notifications.refetch()}><RefreshCw /> Thử lại</button></div>}
    {notifications.data && notifications.data.content.length === 0 && <div className="notifications-state"><Inbox /><h2>{filter === 'unread' ? 'Bạn không có thông báo chưa đọc.' : 'Bạn chưa có thông báo nào.'}</h2><p>{isEmployer ? 'Các cập nhật về công ty, việc làm và ứng viên sẽ xuất hiện tại đây.' : 'Các cập nhật liên quan đến tài khoản và quá trình ứng tuyển sẽ xuất hiện tại đây.'}</p></div>}
    {notifications.data && notifications.data.content.length > 0 && <>
      <section className="notifications-list" aria-label="Danh sách thông báo">
        {notifications.data.content.map((notification) => {
          const target = actionPath(notification, isEmployer)
          return <article key={notification.id} className={`notification-item${notification.read ? '' : ' notification-item--unread'}`}>
            <span className="notification-item__icon"><NotificationIcon eventType={notification.eventType} /></span>
            <div className="notification-item__body"><div><span>{notificationEventLabels[notification.eventType] ?? 'Thông báo'}</span>{!notification.read && <strong>Chưa đọc</strong>}</div><h2>{notification.title}</h2><p>{notification.content}</p><time dateTime={notification.createdAt}>{formatNotificationDate(notification.createdAt)}</time></div>
            {(!notification.read || target) && <button type="button" onClick={() => void openNotification(notification)} disabled={markOne.isPending}>{notification.read ? 'Xem chi tiết' : target ? 'Đọc và xem' : 'Đánh dấu đã đọc'}</button>}
          </article>
        })}
      </section>
      <nav className="notifications-pagination" aria-label="Phân trang thông báo"><button type="button" disabled={!notifications.data.hasPrevious} onClick={() => changePage(page - 1)}><ChevronLeft /> Trước</button><span>Trang {notifications.data.page + 1} / {Math.max(1, notifications.data.totalPages)}</span><button type="button" disabled={!notifications.data.hasNext} onClick={() => changePage(page + 1)}>Sau <ChevronRight /></button></nav>
    </>}
  </main>
}
