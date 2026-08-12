import { ArrowUpRight, BriefcaseBusiness, CheckCircle2, Sparkles, UserRound } from 'lucide-react'
import { JobSearch } from './JobSearch'

export function HeroSection() {
  return (
    <section className="home-hero">
      <div className="container home-hero__grid">
        <div className="home-hero__content">
          <span className="home-hero__eyebrow"><Sparkles size={16} aria-hidden="true" /> Cơ hội mới. Sự nghiệp mới.</span>
          <h1>Tìm đúng việc.<br /><em>Phát triển đúng hướng.</em></h1>
          <p>Khám phá cơ hội việc làm và tìm công việc phù hợp với kỹ năng, kinh nghiệm và mục tiêu nghề nghiệp của bạn.</p>
          <JobSearch />
          <div className="home-hero__trust"><span><CheckCircle2 size={17} /> Việc làm minh bạch</span><span><CheckCircle2 size={17} /> Kết nối qua API Gateway an toàn</span></div>
        </div>

        <div className="career-visual" aria-label="Minh họa kết nối nghề nghiệp thông minh">
          <div className="career-visual__mesh" aria-hidden="true" />
          <article className="career-profile-card">
            <span className="career-profile-card__avatar"><UserRound /></span>
            <div><small>Hồ sơ ứng viên</small><strong>Sẵn sàng cho cơ hội mới</strong></div>
            <span className="career-profile-card__status">Active</span>
          </article>
          <article className="career-match-card">
            <span className="career-match-card__label"><Sparkles size={16} /> AI career match</span>
            <strong>87%</strong>
            <div className="career-match-card__bar"><span /></div>
            <p>Kỹ năng và vai trò đang tiến gần nhau.</p>
          </article>
          <article className="career-job-card">
            <span><BriefcaseBusiness size={19} /></span>
            <div><small>Cơ hội phù hợp</small><strong>Software Engineer</strong><p>Technology · Full-time</p></div>
            <ArrowUpRight size={18} />
          </article>
        </div>
      </div>
    </section>
  )
}
