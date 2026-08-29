import { ArrowLeft, Check, FileText, Sparkles, UserRound } from 'lucide-react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { CvPreview } from './components/CvPreview'
import { cvTemplates, sampleCv } from './cv.templates'
import './cv-builder.css'

export function CvTemplatePreviewPage() {
  const { templateId } = useParams()
  const template = cvTemplates.find((item) => item.id === templateId)
  if (!template) return <Navigate to="/cv/templates" replace />

  return <main className="cv-page cv-template-detail">
    <Link className="cv-template-detail__back" to="/cv/templates"><ArrowLeft /> Quay lại thư viện mẫu</Link>
    <div className="cv-template-detail__layout">
      <section className="cv-template-detail__preview" aria-label={`Xem trước mẫu ${template.name}`}><CvPreview content={sampleCv} templateId={template.id} /></section>
      <aside className="cv-template-detail__decision">
        <span>{template.style}</span><h1>{template.name}</h1><p>{template.description}</p>
        <div className="cv-template-detail__chips">{template.highlights.map((item) => <span key={item}><Check /> {item}</span>)}</div>
        <div className="cv-template-detail__choice"><div><Sparkles /><div><strong>Bắt đầu CV này như thế nào?</strong><p>Cả hai lựa chọn đều dùng thiết kế {template.name} và có thể đổi mẫu sau.</p></div></div>
          <Link className="cv-choice-card" to={`/cv/new?template=${template.id}&source=profile`}><UserRound /><div><strong>Tạo từ hồ sơ của tôi</strong><span>Điền trước thông tin hồ sơ hiện có, sau đó bạn tự kiểm tra và chỉnh sửa.</span></div></Link>
          <Link className="cv-choice-card" to={`/cv/new?template=${template.id}&source=blank`}><FileText /><div><strong>Tạo CV trống</strong><span>Chỉ lấy thiết kế và bắt đầu nhập nội dung từ đầu.</span></div></Link>
        </div>
        <small>Dữ liệu CV đã tạo được lưu riêng, không thay thế CV PDF/DOCX dùng để ứng tuyển.</small>
      </aside>
    </div>
  </main>
}
