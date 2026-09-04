import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertCircle, BellRing, ChevronLeft, ChevronRight, Inbox, Radio, RefreshCw, Send } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { getErrorMessage } from '../../lib/api/error-adapter'
import type { NotificationEventType } from '../../types/models/notification'
import { adminNotificationsKey, broadcastAdminNotification, createAdminNotification, getAdminNotifications } from './admin.api'
import { broadcastSchema, personalNotificationSchema } from './admin.schemas'
import { notificationEventLabels, notificationResourceLabel } from './admin.labels'

const events: NotificationEventType[] = ['APPLICATION_SUBMITTED','APPLICATION_WITHDRAWN','APPLICATION_STATUS_CHANGED','JOB_APPROVED','JOB_REJECTED','COMPANY_VERIFIED','COMPANY_REJECTED','PASSWORD_CHANGED','WELCOME','SYSTEM_ANNOUNCEMENT']
type PersonalValues = z.infer<typeof personalNotificationSchema>
type BroadcastValues = z.infer<typeof broadcastSchema>

export function AdminNotificationsPage() {
  const [page, setPage] = useState(0)
  const [recipient, setRecipient] = useState('')
  const [eventType, setEventType] = useState<NotificationEventType | ''>('')
  const [query, setQuery] = useState('')
  const [feedback, setFeedback] = useState('')
  const client = useQueryClient()
  const personal = useForm<PersonalValues>({ resolver: zodResolver(personalNotificationSchema), defaultValues: { recipientUserId: '', eventType: 'SYSTEM_ANNOUNCEMENT', title: '', content: '', relatedResourceType: '', relatedResourceId: '' } })
  const broadcast = useForm<BroadcastValues>({ resolver: zodResolver(broadcastSchema), defaultValues: { title: '', content: '' } })
  const notifications = useQuery({ queryKey: [...adminNotificationsKey, { page, recipient, eventType, query }], queryFn: () => getAdminNotifications({ page, size: 12, recipientUserId: recipient || undefined, eventType: eventType || undefined, q: query || undefined }), placeholderData: (old) => old, retry: false })
  const create = useMutation({ mutationFn: createAdminNotification, onSuccess: () => { personal.reset(); setFeedback('Đã tạo thông báo cá nhân.'); void client.invalidateQueries({ queryKey: adminNotificationsKey }) } })
  const sendBroadcast = useMutation({ mutationFn: broadcastAdminNotification, onSuccess: () => { broadcast.reset(); setFeedback('Đã phát thông báo toàn hệ thống.'); void client.invalidateQueries({ queryKey: adminNotificationsKey }) } })

  return <main className="admin-page">
    <header className="admin-page__hero"><div><span>Vận hành thông báo</span><h1>Thông báo hệ thống</h1><p>Tạo thông báo cho một người dùng hoặc phát toàn hệ thống theo đúng hợp đồng dịch vụ.</p></div><BellRing /></header>
    {feedback && <p className="admin-feedback" role="status">{feedback}</p>}
    <section className="admin-dashboard-grid">
      <article className="admin-panel"><form className="admin-form" onSubmit={personal.handleSubmit((values) => create.mutate({ ...values, relatedResourceType: values.relatedResourceType || undefined, relatedResourceId: values.relatedResourceId || undefined }))}><div><span className="admin-badge">CÁ NHÂN</span><h2>Thông báo cá nhân</h2></div><label>UUID người nhận<input {...personal.register('recipientUserId')} />{personal.formState.errors.recipientUserId && <small>{personal.formState.errors.recipientUserId.message}</small>}</label><label>Loại sự kiện<select {...personal.register('eventType')}>{events.map((event) => <option key={event} value={event}>{notificationEventLabels[event]}</option>)}</select></label><label>Tiêu đề<input {...personal.register('title')} />{personal.formState.errors.title && <small>{personal.formState.errors.title.message}</small>}</label><label>Nội dung<textarea {...personal.register('content')} />{personal.formState.errors.content && <small>{personal.formState.errors.content.message}</small>}</label><div className="admin-form__grid"><label>Loại tài nguyên<input {...personal.register('relatedResourceType')} /></label><label>UUID tài nguyên<input {...personal.register('relatedResourceId')} />{personal.formState.errors.relatedResourceId && <small>{personal.formState.errors.relatedResourceId.message}</small>}</label></div>{create.isError && <p className="admin-feedback admin-feedback--error">{getErrorMessage(create.error)}</p>}<button className="admin-button" disabled={create.isPending}><Send />{create.isPending ? 'Đang gửi…' : 'Gửi cá nhân'}</button></form></article>
      <article className="admin-panel"><form className="admin-form" onSubmit={broadcast.handleSubmit((values) => sendBroadcast.mutate(values))}><div><span className="admin-badge">TẤT CẢ NGƯỜI DÙNG</span><h2>Phát toàn hệ thống</h2><p>Dịch vụ tự động dùng loại sự kiện thông báo hệ thống.</p></div><label>Tiêu đề<input {...broadcast.register('title')} />{broadcast.formState.errors.title && <small>{broadcast.formState.errors.title.message}</small>}</label><label>Nội dung<textarea {...broadcast.register('content')} />{broadcast.formState.errors.content && <small>{broadcast.formState.errors.content.message}</small>}</label>{sendBroadcast.isError && <p className="admin-feedback admin-feedback--error">{getErrorMessage(sendBroadcast.error)}</p>}<button className="admin-button" disabled={sendBroadcast.isPending}><Radio />{sendBroadcast.isPending ? 'Đang phát…' : 'Phát toàn hệ thống'}</button></form></article>
    </section>
    <section className="admin-toolbar"><label>UUID người nhận<input value={recipient} onChange={(e) => { setRecipient(e.target.value.trim()); setPage(0) }} placeholder="Để trống: thông báo của Admin" /></label><label>Sự kiện<select value={eventType} onChange={(e) => { setEventType(e.target.value as NotificationEventType | ''); setPage(0) }}><option value="">Tất cả</option>{events.map((event) => <option key={event} value={event}>{notificationEventLabels[event]}</option>)}</select></label><label>Từ khóa<input value={query} onChange={(e) => { setQuery(e.target.value); setPage(0) }} /></label></section>
    {notifications.isPending && <div className="admin-skeleton"><span /><span /></div>}
    {notifications.isError && <section className="admin-state"><AlertCircle /><h2>Không thể tải thông báo</h2><p>{getErrorMessage(notifications.error)}</p><button className="admin-button admin-button--secondary" onClick={() => void notifications.refetch()}><RefreshCw />Thử lại</button></section>}
    {notifications.data?.content.length === 0 && <section className="admin-state"><Inbox /><h2>Không có thông báo</h2><p>Thay đổi bộ lọc hoặc tạo thông báo mới.</p></section>}
    {notifications.data && notifications.data.content.length > 0 && <><section className="admin-grid">{notifications.data.content.map((item) => <article className="admin-card" key={item.id}><div className="admin-card__top"><span className="admin-badge">{notificationEventLabels[item.eventType]}</span><small>{new Date(item.createdAt).toLocaleString('vi-VN')}</small></div><h2>{item.title}</h2><p>{item.content}</p><dl><div><dt>ID thông báo</dt><dd>{item.id}</dd></div>{item.relatedResourceId && <div><dt>Tài nguyên</dt><dd>{notificationResourceLabel(item.relatedResourceType)}: {item.relatedResourceId}</dd></div>}</dl></article>)}</section><nav className="admin-pagination"><button className="admin-button admin-button--secondary" disabled={!notifications.data.hasPrevious} onClick={() => setPage(page - 1)}><ChevronLeft />Trước</button><span>Trang {notifications.data.page + 1}/{Math.max(1, notifications.data.totalPages)}</span><button className="admin-button admin-button--secondary" disabled={!notifications.data.hasNext} onClick={() => setPage(page + 1)}>Sau<ChevronRight /></button></nav></>}
  </main>
}
