import { ArrowUpRight, Building, Landmark, Map, MapPinned, Waves } from 'lucide-react'
import { Link } from 'react-router-dom'
import { SectionHeading } from './SectionHeading'

const locations = [
  { name: 'Hồ Chí Minh', text: 'Thị trường năng động', icon: Building },
  { name: 'Hà Nội', text: 'Trung tâm cơ hội phía Bắc', icon: Landmark },
  { name: 'Đà Nẵng', text: 'Thành phố công nghệ biển', icon: Waves },
  { name: 'Bình Dương', text: 'Sản xuất và logistics', icon: MapPinned },
  { name: 'Đồng Nai', text: 'Công nghiệp phát triển', icon: Map },
]

export function LocationSection() {
  return (
    <section className="home-section location-section"><div className="container"><SectionHeading eyebrow="Thị trường việc làm" title="Cơ hội trên khắp Việt Nam" description="Khám phá thị trường nổi bật. Bộ lọc địa điểm chi tiết sẽ được mở khi API tìm kiếm hỗ trợ." /><div className="location-grid">{locations.map(({ name, text, icon: Icon }) => <Link to="/jobs" className="location-card" key={name}><span><Icon size={21} /></span><div><strong>{name}</strong><small>{text}</small></div><ArrowUpRight size={18} /></Link>)}</div></div></section>
  )
}
