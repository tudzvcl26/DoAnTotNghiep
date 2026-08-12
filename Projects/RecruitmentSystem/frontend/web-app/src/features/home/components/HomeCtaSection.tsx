import { ArrowRight, BriefcaseBusiness, Sparkles } from 'lucide-react'
import { ButtonLink } from '../../../components/ui/Button'

export function HomeCtaSection() {
  return <section className="home-cta-section"><div className="container"><div className="home-cta"><span className="home-cta__shape" aria-hidden="true" /><div><span><Sparkles size={16} /> Bước tiến tiếp theo</span><h2>Đã đến lúc tìm cơ hội tiếp theo.</h2><p>Dù bạn đang tìm một vai trò mới hay một đồng đội mới, RecruitmentSystem giúp hành trình bắt đầu rõ ràng hơn.</p></div><div className="home-cta__actions"><ButtonLink to="/jobs" size="lg">Tìm việc ngay <ArrowRight size={18} /></ButtonLink><ButtonLink to="/employer" size="lg" variant="secondary"><BriefcaseBusiness size={18} /> Đăng tuyển</ButtonLink></div></div></div></section>
}
