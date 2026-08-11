import { ArrowRight, Briefcase, Building2, Search, ShieldCheck, Sparkles } from 'lucide-react'
import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, ButtonLink } from '../../components/ui/Button'
import './home-page.css'

export function HomePage() {
  const [keyword, setKeyword] = useState('')
  const navigate = useNavigate()

  const search = (event: FormEvent) => {
    event.preventDefault()
    navigate(keyword.trim() ? `/jobs?keyword=${encodeURIComponent(keyword.trim())}` : '/jobs')
  }

  return (
    <>
      <section className="home-hero">
        <div className="container home-hero__grid">
          <div className="home-hero__content">
            <span className="home-hero__eyebrow"><Sparkles size={16} /> Nơi cơ hội và tài năng gặp nhau</span>
            <h1>Công việc phù hợp.<br /><em>Sự nghiệp xứng đáng.</em></h1>
            <p>Khám phá cơ hội mới và xây dựng hồ sơ nghề nghiệp nổi bật trên một nền tảng tuyển dụng hiện đại.</p>
            <form className="home-search" onSubmit={search}>
              <Search size={21} aria-hidden="true" />
              <label className="sr-only" htmlFor="home-keyword">Từ khóa việc làm</label>
              <input id="home-keyword" value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="Vị trí, kỹ năng hoặc công ty..." />
              <Button type="submit" size="lg">Tìm việc ngay <ArrowRight size={18} /></Button>
            </form>
            <div className="home-hero__trust">
              <span><ShieldCheck size={18} /> Thông tin minh bạch</span>
              <span><Sparkles size={18} /> Công cụ AI hỗ trợ</span>
            </div>
          </div>
          <div className="home-hero__visual" aria-label="Không gian kết nối nghề nghiệp">
            <div className="home-hero__orb" />
            <div className="career-card career-card--primary">
              <span className="career-card__icon"><Briefcase /></span>
              <div><small>Cơ hội hôm nay</small><strong>Tìm đúng vai trò</strong><span>Khám phá thị trường tuyển dụng</span></div>
            </div>
            <div className="career-card career-card--company">
              <span className="career-card__icon"><Building2 /></span>
              <div><small>Doanh nghiệp</small><strong>Nơi bạn thuộc về</strong></div>
            </div>
            <div className="career-card career-card--ai">
              <Sparkles size={20} /><span>AI career companion</span>
            </div>
          </div>
        </div>
      </section>
      <section className="home-foundation">
        <div className="container home-foundation__inner">
          <div><span className="page-eyebrow">Foundation đã sẵn sàng</span><h2>Một trải nghiệm tuyển dụng được xây từ nền tảng vững chắc.</h2></div>
          <p>Trang chủ đầy đủ sẽ được phát triển ở Phase 2. Hiện tại, cấu trúc điều hướng, responsive layout và nền tảng xác thực đã sẵn sàng.</p>
          <ButtonLink to="/jobs" variant="secondary">Khám phá việc làm <ArrowRight size={17} /></ButtonLink>
        </div>
      </section>
    </>
  )
}
