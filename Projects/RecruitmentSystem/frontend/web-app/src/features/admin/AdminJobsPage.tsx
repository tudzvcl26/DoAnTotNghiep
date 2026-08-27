import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { BriefcaseBusiness, CircleStop, Rocket, Search, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { getErrorMessage } from '../../lib/api/error-adapter'
import type { JobStatus } from '../../types/models/job'
import { adminJobsKey, closeAdminJob, deleteAdminJob, getAdminJobs, publishAdminJob } from './admin.api'

type ModerationAction = 'publish' | 'close' | 'delete'

const statusLabels: Record<JobStatus, string> = {
  DRAFT: 'Bản nháp',
  PUBLISHED: 'Đang tuyển',
  CLOSED: 'Đã đóng',
  EXPIRED: 'Hết hạn',
}
const date = new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium' })

export function AdminJobsPage() {
  const queryClient = useQueryClient()
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Math.max(0, Number(searchParams.get('page') ?? 0) || 0)
  const keyword = searchParams.get('keyword')?.trim() ?? ''
  const status = (searchParams.get('status') ?? '') as JobStatus | ''
  const companyId = searchParams.get('companyId')?.trim() ?? ''
  const sort = searchParams.get('sort') ?? 'updatedAt,desc'
  const [draftKeyword, setDraftKeyword] = useState(keyword)
  const [draftCompanyId, setDraftCompanyId] = useState(companyId)
  const [feedback, setFeedback] = useState('')

  const jobs = useQuery({
    queryKey: [...adminJobsKey, { page, keyword, status, companyId, sort }],
    queryFn: () => getAdminJobs({
      page,
      size: 12,
      sort,
      ...(keyword ? { keyword } : {}),
      ...(status ? { status } : {}),
      ...(companyId ? { companyId } : {}),
    }),
  })

  const moderate = useMutation({
    mutationFn: async ({ id, action }: { id: string; action: ModerationAction }) => {
      if (action === 'publish') await publishAdminJob(id)
      else if (action === 'close') await closeAdminJob(id)
      else await deleteAdminJob(id)
    },
    onSuccess: async (_data, { action }) => {
      setFeedback(action === 'publish' ? 'Đã xuất bản việc làm.' : action === 'close' ? 'Đã đóng việc làm.' : 'Đã ngừng kích hoạt việc làm.')
      await queryClient.invalidateQueries({ queryKey: adminJobsKey })
    },
    onMutate: () => setFeedback(''),
  })

  const updateParams = (updates: Record<string, string>) => {
    const next = new URLSearchParams(searchParams)
    Object.entries(updates).forEach(([key, value]) => value ? next.set(key, value) : next.delete(key))
    if (!('page' in updates)) next.delete('page')
    setSearchParams(next)
  }

  return <main className="admin-page">
    <header className="admin-page__hero"><div><span>Recruitment operations</span><h1>Quản lý việc làm</h1><p>Kiểm duyệt việc làm toàn hệ thống qua contract chuyên biệt chỉ dành cho ADMIN.</p></div><BriefcaseBusiness /></header>
    <section className="admin-toolbar">
      <form role="search" onSubmit={(event) => { event.preventDefault(); updateParams({ keyword: draftKeyword.trim(), companyId: draftCompanyId.trim() }) }}>
        <label>Tìm kiếm<input value={draftKeyword} onChange={(event) => setDraftKeyword(event.target.value)} placeholder="Tiêu đề hoặc mã việc làm" /></label>
        <label>Company ID<input value={draftCompanyId} onChange={(event) => setDraftCompanyId(event.target.value)} placeholder="UUID doanh nghiệp" /></label>
        <button className="admin-button" type="submit"><Search /> Tìm</button>
      </form>
      <label>Trạng thái<select value={status} onChange={(event) => updateParams({ status: event.target.value })}><option value="">Tất cả</option><option value="DRAFT">Bản nháp</option><option value="PUBLISHED">Đang tuyển</option><option value="CLOSED">Đã đóng</option><option value="EXPIRED">Hết hạn</option></select></label>
      <label>Sắp xếp<select value={sort} onChange={(event) => updateParams({ sort: event.target.value })}><option value="updatedAt,desc">Cập nhật mới nhất</option><option value="createdAt,desc">Tạo mới nhất</option><option value="title,asc">Tên A–Z</option><option value="publishedAt,desc">Đăng mới nhất</option></select></label>
    </section>
    {feedback && <p className="admin-feedback" role="status">{feedback}</p>}
    {moderate.isError && <p className="admin-feedback admin-feedback--error" role="alert">{getErrorMessage(moderate.error)}</p>}
    {jobs.isPending && <div className="admin-skeleton" aria-label="Đang tải việc làm"><span /><span /><span /></div>}
    {jobs.isError && <p className="admin-feedback admin-feedback--error" role="alert">{getErrorMessage(jobs.error)}</p>}
    {jobs.isSuccess && jobs.data.content.length === 0 && <section className="admin-state"><BriefcaseBusiness /><h2>Không có việc làm phù hợp</h2><p>Hãy thay đổi từ khóa hoặc bộ lọc.</p></section>}
    {jobs.data && jobs.data.content.length > 0 && <section className="admin-grid admin-grid--three" aria-label="Danh sách việc làm">
      {jobs.data.content.map((job) => <article className="admin-card" key={job.id}><div className="admin-card__top"><h2>{job.title}</h2><span className={`admin-badge admin-badge--${job.status.toLowerCase()}`}>{statusLabels[job.status]}</span></div><p>{job.jobCode}</p><dl><div><dt>Company ID</dt><dd>{job.companyId}</dd></div><div><dt>Danh mục</dt><dd>{job.categoryName ?? '—'}</dd></div><div><dt>Hạn ứng tuyển</dt><dd>{job.applicationDeadline ? date.format(new Date(job.applicationDeadline)) : 'Không giới hạn'}</dd></div><div><dt>Ngày đăng</dt><dd>{job.publishedAt ? date.format(new Date(job.publishedAt)) : 'Chưa đăng'}</dd></div></dl><div className="admin-card__actions">{job.status === 'DRAFT' && <button className="admin-button" type="button" disabled={moderate.isPending} onClick={() => moderate.mutate({ id: job.id, action: 'publish' })}><Rocket />Xuất bản</button>}{job.status === 'PUBLISHED' && <button className="admin-button admin-button--secondary" type="button" disabled={moderate.isPending} onClick={() => moderate.mutate({ id: job.id, action: 'close' })}><CircleStop />Đóng</button>}<button className="admin-button admin-button--danger" type="button" disabled={moderate.isPending} onClick={() => { if (window.confirm(`Ngừng kích hoạt việc làm “${job.title}”? Thao tác này không thể hoàn tác trên màn hình quản trị.`)) moderate.mutate({ id: job.id, action: 'delete' }) }}><Trash2 />Ngừng kích hoạt</button></div></article>)}
    </section>}
    {jobs.data && <nav className="admin-pagination" aria-label="Phân trang việc làm"><button className="admin-button admin-button--secondary" type="button" disabled={!jobs.data.hasPrevious} onClick={() => updateParams({ page: String(page - 1) })}>Trước</button><span>Trang {jobs.data.page + 1}/{Math.max(1, jobs.data.totalPages)} · {jobs.data.totalElements} việc làm</span><button className="admin-button admin-button--secondary" type="button" disabled={!jobs.data.hasNext} onClick={() => updateParams({ page: String(page + 1) })}>Sau</button></nav>}
  </main>
}
