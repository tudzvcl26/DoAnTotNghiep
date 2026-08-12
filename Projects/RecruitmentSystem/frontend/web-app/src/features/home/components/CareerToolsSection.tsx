import { ArrowUpRight, Calculator, FileCheck2, FilePenLine, ScanSearch, WandSparkles } from 'lucide-react'
import { Link } from 'react-router-dom'
import { SectionHeading } from './SectionHeading'

const tools = [
  { title: 'AI Resume Analysis', text: 'Phân tích CV và nhận gợi ý cải thiện có cấu trúc.', icon: ScanSearch, to: '/candidate', badge: 'AI' },
  { title: 'CV Matching', text: 'Đánh giá mức độ phù hợp giữa hồ sơ và công việc.', icon: FileCheck2, to: '/candidate', badge: 'AI' },
  { title: 'AI Cover Letter', text: 'Chuẩn bị thư ứng tuyển theo định hướng nghề nghiệp.', icon: WandSparkles, to: '/candidate', badge: 'Planned' },
  { title: 'Hồ sơ nghề nghiệp', text: 'Quản lý thông tin và tài liệu ứng tuyển tập trung.', icon: FilePenLine, to: '/candidate', badge: 'Profile' },
  { title: 'Công cụ thu nhập', text: 'Khu vực quy hoạch cho tiện ích lương và thuế.', icon: Calculator, to: '/candidate', badge: 'Planned' },
]

export function CareerToolsSection() {
  return <section className="home-section career-tools-section"><div className="container"><SectionHeading eyebrow="Làm chủ hành trình" title="Công cụ nghề nghiệp" description="Những điểm đến đang có hoặc được quy hoạch trong hệ sinh thái RecruitmentSystem." /><div className="career-tools-grid">{tools.map(({ title, text, icon: Icon, to, badge }) => <Link className="career-tool-card" to={to} key={title}><div><span><Icon size={22} /></span><small>{badge}</small></div><h3>{title}</h3><p>{text}</p><strong>Khám phá <ArrowUpRight size={16} /></strong></Link>)}</div></div></section>
}
