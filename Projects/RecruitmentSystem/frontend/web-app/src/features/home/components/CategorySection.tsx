import { BarChart3, Boxes, Brush, Code2, Headphones, Landmark, Megaphone, UsersRound } from 'lucide-react'
import { Link } from 'react-router-dom'
import { SectionHeading } from './SectionHeading'

const categories = [
  { name: 'IT - Phần mềm', keyword: 'IT', icon: Code2 },
  { name: 'Marketing', keyword: 'Marketing', icon: Megaphone },
  { name: 'Kinh doanh', keyword: 'Kinh doanh', icon: BarChart3 },
  { name: 'Tài chính - Ngân hàng', keyword: 'Tài chính', icon: Landmark },
  { name: 'Nhân sự', keyword: 'Nhân sự', icon: UsersRound },
  { name: 'Logistics', keyword: 'Logistics', icon: Boxes },
  { name: 'Thiết kế', keyword: 'Thiết kế', icon: Brush },
  { name: 'Chăm sóc khách hàng', keyword: 'Chăm sóc khách hàng', icon: Headphones },
]

export function CategorySection() {
  return (
    <section className="home-section category-section">
      <div className="container">
        <SectionHeading eyebrow="Lĩnh vực nghề nghiệp" title="Khám phá theo ngành nghề" description="Bắt đầu từ lĩnh vực bạn quan tâm và xem những vai trò đang có trên hệ thống." />
        <div className="category-grid">{categories.map(({ name, keyword, icon: Icon }) => <Link to={`/jobs?keyword=${encodeURIComponent(keyword)}`} className="category-card" key={name}><span><Icon size={22} /></span><strong>{name}</strong><small>Tìm vị trí phù hợp</small></Link>)}</div>
      </div>
    </section>
  )
}
