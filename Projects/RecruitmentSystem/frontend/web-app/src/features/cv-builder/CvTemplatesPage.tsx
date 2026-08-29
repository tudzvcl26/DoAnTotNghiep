import { Search, Sparkles } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { CvPreview } from './components/CvPreview'
import { cvTemplates, sampleCv } from './cv.templates'
import './cv-builder.css'

const styles = ['Tất cả', 'Đơn giản', 'Chuyên nghiệp', 'Hiện đại', 'ATS', 'Sinh viên'] as const

export function CvTemplatesPage() {
  const [keyword, setKeyword] = useState('')
  const [style, setStyle] = useState<(typeof styles)[number]>('Tất cả')
  const visible = useMemo(() => cvTemplates.filter((item) => (style === 'Tất cả' || item.style === style) && `${item.name} ${item.style} ${item.bestFor}`.toLowerCase().includes(keyword.toLowerCase())), [keyword, style])
  return <main className="cv-page cv-template-page">
    <header className="cv-page__hero"><div><span><Sparkles /> 5 thiết kế từ dữ liệu thật</span><h1>Mẫu CV</h1><p>Chọn phong cách phù hợp. Một bộ dữ liệu có thể đổi mẫu bất cứ lúc nào.</p></div><Link className="cv-button" to="/cv">CV của tôi</Link></header>
    <div className="cv-template-filters"><label><Search /><span className="sr-only">Tìm mẫu CV</span><input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="Tìm theo tên hoặc vị trí…" /></label><div role="group" aria-label="Lọc phong cách">{styles.map((item) => <button type="button" className={style === item ? 'is-active' : ''} onClick={() => setStyle(item)} key={item}>{item}</button>)}</div></div>
    <section className="cv-template-grid">
      {visible.map((template) => <article className="cv-template-card" key={template.id}><div className="cv-template-card__preview"><CvPreview compact content={sampleCv} templateId={template.id} /></div><div className="cv-template-card__content"><span>{template.style}</span><h2>{template.name}</h2><p>{template.description}</p><small>Phù hợp: {template.bestFor}</small><Link className="cv-button cv-button--primary" to={`/cv/new?template=${template.id}`}>Dùng mẫu này</Link></div></article>)}
    </section>
    <section className="cv-writing-guide" id="guide"><span>Hướng dẫn viết CV</span><h2>Ngắn gọn, có bằng chứng, đúng vị trí</h2><div><article><strong>01</strong><p>Mở đầu bằng mục tiêu nghề nghiệp và giá trị bạn có thể mang lại.</p></article><article><strong>02</strong><p>Mô tả kinh nghiệm bằng hành động, kết quả và số liệu cụ thể.</p></article><article><strong>03</strong><p>Giữ từ khóa kỹ thuật bằng tiếng Anh khi đó là tên công nghệ chuẩn.</p></article></div></section>
  </main>
}
