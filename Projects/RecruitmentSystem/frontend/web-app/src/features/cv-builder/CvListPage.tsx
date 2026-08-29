import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Download, Eye, FilePlus2, Pencil, Trash2 } from 'lucide-react'
import { Link } from 'react-router-dom'
import { cvApi, saveBlob } from './cv.api'
import { cvTemplates } from './cv.templates'
import './cv-builder.css'

export function CvListPage() {
  const queryClient = useQueryClient()
  const cvs = useQuery({ queryKey: ['candidate-cvs'], queryFn: cvApi.list })
  const remove = useMutation({ mutationFn: cvApi.remove, onSuccess: () => queryClient.invalidateQueries({ queryKey: ['candidate-cvs'] }) })
  const download = useMutation({ mutationFn: async (id: string) => ({ id, blob: await cvApi.download(id) }), onSuccess: ({ id, blob }) => saveBlob(blob, `cv-${id}.pdf`) })

  return <main className="cv-page">
    <header className="cv-page__hero"><div><span>CV Builder</span><h1>CV của tôi</h1><p>Generated CV được lưu riêng, không thay thế CV PDF/DOCX bạn đã tải lên.</p></div><Link className="cv-button cv-button--primary" to="/cv/templates"><FilePlus2 /> Tạo CV mới</Link></header>
    {cvs.isLoading && <div className="cv-state">Đang tải danh sách CV…</div>}
    {cvs.isError && <div className="cv-state cv-state--error">Không thể tải danh sách CV.</div>}
    {!cvs.isLoading && (cvs.data?.length ?? 0) === 0 && <div className="cv-empty"><FilePlus2 /><h2>Bắt đầu CV đầu tiên</h2><p>Chọn một mẫu, nhập nội dung và xem trước ngay khi chỉnh sửa.</p><Link className="cv-button cv-button--primary" to="/cv/templates">Khám phá mẫu CV</Link></div>}
    <section className="cv-list" aria-label="Danh sách CV đã tạo">
      {cvs.data?.map((cv) => <article className="cv-list-card" key={cv.id}>
        <div className={`cv-list-card__thumb cv-list-card__thumb--${cv.templateId}`}><span>{cv.content.personalInfo.fullName || 'CV'}</span><small>{cv.content.personalInfo.headline || cv.title}</small></div>
        <div className="cv-list-card__body"><span>{cvTemplates.find((item) => item.id === cv.templateId)?.name}</span><h2>{cv.title}</h2><p>Cập nhật {new Date(cv.updatedAt).toLocaleString('vi-VN')}</p></div>
        <div className="cv-list-card__actions">
          <Link to={`/cv/${cv.id}/edit`}><Pencil /> Chỉnh sửa</Link><Link to={`/cv/${cv.id}/preview`}><Eye /> Xem</Link>
          <button type="button" onClick={() => download.mutate(cv.id)} disabled={download.isPending}><Download /> PDF</button>
          <button className="is-danger" type="button" onClick={() => { if (window.confirm(`Xóa “${cv.title}”?`)) remove.mutate(cv.id) }} disabled={remove.isPending}><Trash2 /> Xóa</button>
        </div>
      </article>)}
    </section>
  </main>
}
