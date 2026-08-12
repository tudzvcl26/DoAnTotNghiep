import { ArrowRight, BrainCircuit, Check, FileSearch, Sparkles } from 'lucide-react'
import { ButtonLink } from '../../../components/ui/Button'

export function AiCareerSection() {
  return (
    <section className="home-section ai-career-section"><div className="container ai-career">
      <div className="ai-career__content"><span className="ai-career__eyebrow"><Sparkles size={16} /> AI Career Companion</span><h2>Không chỉ tìm việc.<br />Tìm công việc phù hợp với bạn.</h2><p>Phân tích hồ sơ, nhận diện kỹ năng và kết nối với những cơ hội phù hợp hơn trong một hành trình liền mạch.</p><ul><li><Check /> Phân tích cấu trúc và nội dung CV</li><li><Check /> Đánh giá điểm mạnh kỹ năng</li><li><Check /> Gợi ý cải thiện hồ sơ nghề nghiệp</li></ul><ButtonLink to="/candidate" size="lg">Khám phá AI Resume <ArrowRight size={18} /></ButtonLink><small>Kết quả minh họa bên cạnh là visual concept, không phải dữ liệu phân tích thật.</small></div>
      <div className="ai-insight" aria-label="Minh họa giao diện AI Career Companion"><div className="ai-insight__top"><span><BrainCircuit /> AI profile insight</span><span className="ai-insight__live">Concept</span></div><div className="ai-insight__score"><div><strong>87</strong><span>%</span></div><p>Mức độ hoàn thiện hồ sơ</p></div><div className="ai-skill"><span>Java</span><i><b style={{ width: '88%' }} /></i><strong>Strong</strong></div><div className="ai-skill"><span>Spring</span><i><b style={{ width: '78%' }} /></i><strong>Good</strong></div><div className="ai-skill"><span>SQL</span><i><b style={{ width: '70%' }} /></i><strong>Good</strong></div><div className="ai-insight__recommendation"><FileSearch /><div><small>Gợi ý tiếp theo</small><strong>Làm rõ tác động trong kinh nghiệm gần nhất</strong></div></div></div>
    </div></section>
  )
}
