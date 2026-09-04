import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertCircle, ChevronLeft, ChevronRight, Inbox, Pencil, Plus, RefreshCw, Search, Trash2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useSearchParams } from 'react-router-dom'
import { z } from 'zod'
import { getErrorMessage } from '../../lib/api/error-adapter'
import { adminCatalogKey, createCatalogItem, deleteCatalogItem, getCatalog, updateCatalogItem } from './admin.api'
import { catalogSchema } from './admin.schemas'
import { activeStateLabels } from './admin.labels'
import type { CatalogItem, CatalogKind } from './admin.types'

type Values = z.infer<typeof catalogSchema>
const config: Record<CatalogKind, { title: string; singular: string; description: string }> = {
  categories: { title: 'Ngành nghề', singular: 'ngành nghề', description: 'Quản lý taxonomy và quan hệ danh mục theo contract Recruitment Service.' },
  skills: { title: 'Kỹ năng', singular: 'kỹ năng', description: 'Duy trì danh mục kỹ năng dùng trong Job và hồ sơ ứng viên.' },
  benefits: { title: 'Phúc lợi', singular: 'phúc lợi', description: 'Duy trì danh mục quyền lợi hiển thị trên tin tuyển dụng.' },
}

const defaults: Values = { name: '', slug: '', description: '', icon: '', active: true, displayOrder: 0, parentId: '' }

export function AdminCatalogPage({ kind }: { kind: CatalogKind }) {
  const [params, setParams] = useSearchParams()
  const [editing, setEditing] = useState<CatalogItem | null>(null)
  const [formOpen, setFormOpen] = useState(false)
  const [feedback, setFeedback] = useState('')
  const page = Math.max(0, Number(params.get('page') ?? 0) || 0)
  const keyword = params.get('q')?.trim() ?? ''
  const queryClient = useQueryClient()
  const form = useForm<Values>({ resolver: zodResolver(catalogSchema), defaultValues: defaults })
  const items = useQuery({ queryKey: [...adminCatalogKey(kind), { page, keyword }], queryFn: () => getCatalog(kind, { page, size: 12, keyword: keyword || undefined }), placeholderData: (old) => old })

  useEffect(() => {
    setEditing(null)
    setFormOpen(false)
    setFeedback('')
    form.reset(defaults)
  }, [form, kind])

  useEffect(() => {
    if (!formOpen) return
    form.reset(editing ? {
      name: editing.name, slug: editing.slug, description: editing.description ?? '', icon: editing.icon ?? '', active: editing.active,
      displayOrder: editing.displayOrder ?? 0, parentId: editing.parentId ?? '',
    } : defaults)
  }, [editing, form, formOpen])

  const save = useMutation({
    mutationFn: (values: Values) => {
      const common = { name: values.name, description: values.description || undefined, icon: values.icon || undefined, active: values.active,
        ...(kind === 'categories' ? { displayOrder: values.displayOrder, parentId: values.parentId || null } : {}) }
      return editing ? updateCatalogItem(kind, editing.id, common) : createCatalogItem(kind, { ...common, slug: values.slug })
    },
    onSuccess: () => { setFeedback(`${editing ? 'Cập nhật' : 'Tạo'} ${config[kind].singular} thành công.`); setFormOpen(false); setEditing(null); void queryClient.invalidateQueries({ queryKey: adminCatalogKey(kind) }) },
  })
  const remove = useMutation({ mutationFn: (id: string) => deleteCatalogItem(kind, id), onSuccess: () => { setFeedback(`Đã ngừng kích hoạt ${config[kind].singular}.`); void queryClient.invalidateQueries({ queryKey: adminCatalogKey(kind) }) } })

  const startCreate = () => { setEditing(null); setFormOpen(true); setFeedback(''); save.reset() }
  const startEdit = (item: CatalogItem) => { setEditing(item); setFormOpen(true); setFeedback(''); save.reset() }
  const updateParams = (next: Record<string, string | number>) => { const copy = new URLSearchParams(params); Object.entries(next).forEach(([key, value]) => value === '' ? copy.delete(key) : copy.set(key, String(value))); setParams(copy) }

  return <main className="admin-page">
    <header className="admin-page__hero"><div><span>Catalog management</span><h1>{config[kind].title}</h1><p>{config[kind].description}</p></div><Search /></header>
    <section className="admin-toolbar"><label>Tìm theo tên<input defaultValue={keyword} placeholder={`Tìm ${config[kind].singular}…`} onKeyDown={(event) => { if (event.key === 'Enter') updateParams({ q: event.currentTarget.value.trim(), page: 0 }) }} /></label><button className="admin-button admin-button--secondary" type="button" onClick={(event) => { const input = event.currentTarget.parentElement?.querySelector('input'); updateParams({ q: input?.value.trim() ?? '', page: 0 }) }}><Search />Tìm</button><button className="admin-button" type="button" onClick={startCreate}><Plus />Thêm mới</button></section>
    {feedback && <p className="admin-feedback" role="status">{feedback}</p>}
    {formOpen && <section className="admin-panel"><form className="admin-form" onSubmit={form.handleSubmit((values) => save.mutate(values))}><div className="admin-form__grid">
      <label>Tên<input {...form.register('name')} />{form.formState.errors.name && <small>{form.formState.errors.name.message}</small>}</label>
      <label>Slug<input {...form.register('slug')} disabled={Boolean(editing)} />{form.formState.errors.slug && <small>{form.formState.errors.slug.message}</small>}</label>
      <label>Icon<input {...form.register('icon')} placeholder="Tên icon hoặc URL theo dữ liệu hiện tại" />{form.formState.errors.icon && <small>{form.formState.errors.icon.message}</small>}</label>
      {kind === 'categories' && <><label>Thứ tự<input type="number" {...form.register('displayOrder', { valueAsNumber: true })} />{form.formState.errors.displayOrder && <small>{form.formState.errors.displayOrder.message}</small>}</label><label>Parent UUID<input {...form.register('parentId')} placeholder="Để trống nếu là danh mục gốc" />{form.formState.errors.parentId && <small>{form.formState.errors.parentId.message}</small>}</label></>}
      <label className="admin-form__wide">Mô tả<textarea {...form.register('description')} />{form.formState.errors.description && <small>{form.formState.errors.description.message}</small>}</label>
      <label><span>Trạng thái</span><select {...form.register('active', { setValueAs: (value) => value === true || value === 'true' })}><option value="true">Đang hoạt động</option><option value="false">Ngừng hoạt động</option></select></label>
    </div>{save.isError && <p className="admin-feedback admin-feedback--error" role="alert">{getErrorMessage(save.error)}</p>}<div className="admin-form__actions"><button className="admin-button admin-button--secondary" type="button" onClick={() => setFormOpen(false)}>Hủy</button><button className="admin-button" disabled={save.isPending}>{save.isPending ? 'Đang lưu…' : editing ? 'Lưu thay đổi' : 'Tạo mới'}</button></div></form></section>}
    {items.isPending && <div className="admin-skeleton"><span /><span /><span /></div>}
    {items.isError && <section className="admin-state"><AlertCircle /><h2>Không thể tải danh mục</h2><p>{getErrorMessage(items.error)}</p><button className="admin-button admin-button--secondary" type="button" onClick={() => void items.refetch()}><RefreshCw />Thử lại</button></section>}
    {remove.isError && <p className="admin-feedback admin-feedback--error" role="alert">{getErrorMessage(remove.error)}</p>}
    {items.data?.content.length === 0 && <section className="admin-state"><Inbox /><h2>Chưa có dữ liệu</h2><p>Không tìm thấy {config[kind].singular} phù hợp.</p></section>}
    {items.data && items.data.content.length > 0 && <><section className="admin-grid admin-grid--three">{items.data.content.map((item) => <article className="admin-card" key={item.id}><div className="admin-card__top"><div><span className="admin-badge">{activeStateLabels[item.active ? 'ACTIVE' : 'INACTIVE']}</span><h2>{item.name}</h2></div><small>{item.slug}</small></div><p>{item.description || 'Chưa có mô tả.'}</p><dl>{kind === 'categories' && <div><dt>Parent</dt><dd>{item.parentName ?? 'Danh mục gốc'} · thứ tự {item.displayOrder}</dd></div>}<div><dt>ID</dt><dd>{item.id}</dd></div></dl><div className="admin-card__actions"><button className="admin-button admin-button--secondary" type="button" onClick={() => startEdit(item)}><Pencil />Sửa</button><button className="admin-button admin-button--danger" type="button" disabled={remove.isPending} onClick={() => { if (window.confirm(`Ngừng kích hoạt ${item.name}?`)) remove.mutate(item.id) }}><Trash2 />Xóa</button></div></article>)}</section><nav className="admin-pagination"><button className="admin-button admin-button--secondary" disabled={!items.data.hasPrevious} onClick={() => updateParams({ page: page - 1 })}><ChevronLeft />Trước</button><span>Trang {items.data.page + 1}/{Math.max(1, items.data.totalPages)} · {items.data.totalElements} mục</span><button className="admin-button admin-button--secondary" disabled={!items.data.hasNext} onClick={() => updateParams({ page: page + 1 })}>Sau<ChevronRight /></button></nav></>}
  </main>
}
