import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertCircle, ChevronLeft, ChevronRight, Inbox, LayoutTemplate, Pencil, Plus, Power, RefreshCw } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { getErrorMessage } from '../../lib/api/error-adapter'
import type { NotificationEventType } from '../../types/models/notification'
import { adminTemplatesKey, createNotificationTemplate, getNotificationTemplates, updateNotificationTemplate, updateNotificationTemplateActive } from './admin.api'
import { templateSchema } from './admin.schemas'
import type { NotificationChannel, NotificationTemplate } from './admin.types'

type Values = z.infer<typeof templateSchema>
const events: NotificationEventType[] = ['APPLICATION_SUBMITTED','APPLICATION_WITHDRAWN','APPLICATION_STATUS_CHANGED','JOB_APPROVED','JOB_REJECTED','COMPANY_VERIFIED','COMPANY_REJECTED','PASSWORD_CHANGED','WELCOME','SYSTEM_ANNOUNCEMENT']
const channels: NotificationChannel[] = ['IN_APP','EMAIL','PUSH']
const defaults: Values = { code: '', eventType: 'SYSTEM_ANNOUNCEMENT', channel: 'IN_APP', titleTemplate: '', contentTemplate: '' }

export function AdminNotificationTemplatesPage() {
  const [page, setPage] = useState(0)
  const [active, setActive] = useState<boolean | undefined>()
  const [editing, setEditing] = useState<NotificationTemplate | null>(null)
  const [open, setOpen] = useState(false)
  const [feedback, setFeedback] = useState('')
  const client = useQueryClient()
  const form = useForm<Values>({ resolver: zodResolver(templateSchema), defaultValues: defaults })
  const templates = useQuery({ queryKey: [...adminTemplatesKey, { page, active }], queryFn: () => getNotificationTemplates({ page, size: 12, active }), placeholderData: (old) => old })
  useEffect(() => { if (open) form.reset(editing ? { code: editing.code, eventType: editing.eventType, channel: editing.channel, titleTemplate: editing.titleTemplate, contentTemplate: editing.contentTemplate } : defaults) }, [editing, form, open])
  const save = useMutation({ mutationFn: (values: Values) => editing ? updateNotificationTemplate(editing.id, { eventType: values.eventType, channel: values.channel, titleTemplate: values.titleTemplate, contentTemplate: values.contentTemplate }) : createNotificationTemplate(values), onSuccess: () => { setFeedback(editing ? 'Đã cập nhật template.' : 'Đã tạo template.'); setOpen(false); setEditing(null); void client.invalidateQueries({ queryKey: adminTemplatesKey }) } })
  const toggle = useMutation({ mutationFn: ({ id, value }: { id: string; value: boolean }) => updateNotificationTemplateActive(id, value), onSuccess: () => { setFeedback('Đã cập nhật trạng thái template.'); void client.invalidateQueries({ queryKey: adminTemplatesKey }) } })
  const edit = (item: NotificationTemplate) => { setEditing(item); setOpen(true); save.reset(); setFeedback('') }

  return <main className="admin-page"><header className="admin-page__hero"><div><span>Notification operations</span><h1>Mẫu thông báo</h1><p>Tạo, cập nhật và bật/tắt template. Backend không có delete endpoint nên UI không cung cấp thao tác xóa.</p></div><LayoutTemplate /></header>
    <section className="admin-toolbar"><label>Trạng thái<select value={active === undefined ? '' : String(active)} onChange={(e) => { setActive(e.target.value === '' ? undefined : e.target.value === 'true'); setPage(0) }}><option value="">Tất cả</option><option value="true">Đang hoạt động</option><option value="false">Ngừng hoạt động</option></select></label><button className="admin-button" onClick={() => { setEditing(null); setOpen(true); save.reset(); setFeedback('') }}><Plus />Tạo template</button></section>
    {feedback && <p className="admin-feedback">{feedback}</p>}
    {open && <section className="admin-panel"><form className="admin-form" onSubmit={form.handleSubmit((values) => save.mutate(values))}><div className="admin-form__grid"><label>Code<input {...form.register('code')} disabled={Boolean(editing)} />{form.formState.errors.code && <small>{form.formState.errors.code.message}</small>}</label><label>Event type<select {...form.register('eventType')}>{events.map((event) => <option key={event}>{event}</option>)}</select></label><label>Channel<select {...form.register('channel')}>{channels.map((channel) => <option key={channel}>{channel}</option>)}</select></label><label>Title template<input {...form.register('titleTemplate')} />{form.formState.errors.titleTemplate && <small>{form.formState.errors.titleTemplate.message}</small>}</label><label className="admin-form__wide">Content template<textarea {...form.register('contentTemplate')} />{form.formState.errors.contentTemplate && <small>{form.formState.errors.contentTemplate.message}</small>}</label></div>{save.isError && <p className="admin-feedback admin-feedback--error">{getErrorMessage(save.error)}</p>}<div className="admin-form__actions"><button className="admin-button admin-button--secondary" type="button" onClick={() => setOpen(false)}>Hủy</button><button className="admin-button" disabled={save.isPending}>{save.isPending ? 'Đang lưu…' : editing ? 'Lưu thay đổi' : 'Tạo template'}</button></div></form></section>}
    {templates.isPending && <div className="admin-skeleton"><span /><span /></div>}{templates.isError && <section className="admin-state"><AlertCircle /><h2>Không thể tải template</h2><p>{getErrorMessage(templates.error)}</p><button className="admin-button admin-button--secondary" onClick={() => void templates.refetch()}><RefreshCw />Thử lại</button></section>}{toggle.isError && <p className="admin-feedback admin-feedback--error">{getErrorMessage(toggle.error)}</p>}
    {templates.data?.content.length === 0 && <section className="admin-state"><Inbox /><h2>Chưa có template</h2><p>Tạo template đầu tiên cho kênh phù hợp.</p></section>}
    {templates.data && templates.data.content.length > 0 && <><section className="admin-grid">{templates.data.content.map((item) => <article className="admin-card" key={item.id}><div className="admin-card__top"><div><span className={`admin-badge${item.active ? '' : ' admin-badge--off'}`}>{item.active ? 'ACTIVE' : 'INACTIVE'}</span><h2>{item.code}</h2></div><small>{item.channel}</small></div><p><strong>{item.titleTemplate}</strong><br />{item.contentTemplate}</p><dl><div><dt>Event</dt><dd>{item.eventType}</dd></div><div><dt>ID</dt><dd>{item.id}</dd></div></dl><div className="admin-card__actions"><button className="admin-button admin-button--secondary" onClick={() => edit(item)}><Pencil />Sửa</button><button className={`admin-button ${item.active ? 'admin-button--danger' : ''}`} disabled={toggle.isPending} onClick={() => toggle.mutate({ id: item.id, value: !item.active })}><Power />{item.active ? 'Tắt' : 'Bật'}</button></div></article>)}</section><nav className="admin-pagination"><button className="admin-button admin-button--secondary" disabled={!templates.data.hasPrevious} onClick={() => setPage(page - 1)}><ChevronLeft />Trước</button><span>Trang {templates.data.page + 1}/{Math.max(1, templates.data.totalPages)}</span><button className="admin-button admin-button--secondary" disabled={!templates.data.hasNext} onClick={() => setPage(page + 1)}>Sau<ChevronRight /></button></nav></>}
  </main>
}
