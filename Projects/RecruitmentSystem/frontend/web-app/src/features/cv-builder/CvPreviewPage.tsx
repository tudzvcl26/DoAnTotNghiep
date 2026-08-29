import { useMutation, useQuery } from '@tanstack/react-query'
import { ArrowLeft, Download, Pencil } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { cvApi, saveBlob } from './cv.api'
import { CvPreview } from './components/CvPreview'
import './cv-builder.css'

export function CvPreviewPage() {
  const { id = '' } = useParams()
  const cv = useQuery({ queryKey: ['candidate-cv', id], queryFn: () => cvApi.get(id), enabled: Boolean(id) })
  const download = useMutation({ mutationFn: () => cvApi.download(id), onSuccess: (blob) => saveBlob(blob, `${cv.data?.title || 'cv'}.pdf`) })
  if (cv.isLoading) return <main className="cv-page"><div className="cv-state">Đang tải bản xem trước…</div></main>
  if (!cv.data) return <main className="cv-page"><div className="cv-state cv-state--error">Không tìm thấy CV hoặc bạn không có quyền truy cập.</div></main>
  return <main className="cv-preview-page"><div className="cv-preview-toolbar"><Link to="/cv"><ArrowLeft /> CV của tôi</Link><strong>{cv.data.title}</strong><div><Link to={`/cv/${id}/edit`}><Pencil /> Chỉnh sửa</Link><button type="button" onClick={() => download.mutate()} disabled={download.isPending}><Download /> Tải PDF</button></div></div><div className="cv-preview-stage"><CvPreview content={cv.data.content} templateId={cv.data.templateId} /></div></main>
}
