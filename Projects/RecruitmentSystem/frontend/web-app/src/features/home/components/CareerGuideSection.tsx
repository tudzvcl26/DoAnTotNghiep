import { ArrowRight, BookOpenCheck, MessagesSquare, SearchCheck, TrendingUp } from 'lucide-react'
import { Link } from 'react-router-dom'
import { SectionHeading } from './SectionHeading'

const guides = [
  { tag: 'CV', title: 'Bí quyết viết CV rõ ràng và thuyết phục', text: 'Biến kinh nghiệm thành những giá trị dễ nhận biết.', icon: BookOpenCheck },
  { tag: 'Phỏng vấn', title: 'Chuẩn bị phỏng vấn với tâm thế chủ động', text: 'Một checklist ngắn cho buổi trao đổi hiệu quả.', icon: MessagesSquare },
  { tag: 'Tìm việc', title: 'Tìm việc hiệu quả trong thị trường mới', text: 'Tập trung vào vai trò phù hợp thay vì ứng tuyển dàn trải.', icon: SearchCheck },
  { tag: 'Phát triển', title: 'Xây lộ trình nghề nghiệp có định hướng', text: 'Nhìn xa hơn chức danh tiếp theo của bạn.', icon: TrendingUp },
]

export function CareerGuideSection() {
  return <section className="home-section career-guide-section"><div className="container"><SectionHeading eyebrow="Kiến thức thực hành" title="Cẩm nang nghề nghiệp" description="Nội dung định hướng tĩnh trong khi nền tảng chưa có Article Service." /><div className="career-guide-grid">{guides.map(({ tag, title, text, icon: Icon }) => <article className="career-guide-card" key={title}><span className="career-guide-card__icon"><Icon /></span><small>{tag}</small><h3>{title}</h3><p>{text}</p><Link to="/jobs" aria-label={`${title} - khám phá việc làm`}>Khám phá cơ hội <ArrowRight size={16} /></Link></article>)}</div></div></section>
}
