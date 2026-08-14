import { BellRing, Bot, Boxes, Building2, FileClock, FileUser, LayoutTemplate, ShieldCheck, UsersRound } from 'lucide-react'
import { Link } from 'react-router-dom'

const modules = [
  { title: 'Người dùng', description: 'Quản lý role và trạng thái account tại Auth Service.', to: '/admin/users', icon: UsersRound },
  { title: 'Công ty', description: 'Duyệt trạng thái xác minh doanh nghiệp.', to: '/admin/companies', icon: Building2 },
  { title: 'Applications', description: 'Truy vấn và kiểm tra Application toàn hệ thống.', to: '/admin/applications', icon: FileUser },
  { title: 'Catalog', description: 'Category, Skill và Benefit CRUD có search và phân trang.', to: '/admin/catalog/categories', icon: Boxes },
  { title: 'Thông báo', description: 'Gửi cá nhân, broadcast và xem danh sách theo recipient.', to: '/admin/notifications', icon: BellRing },
  { title: 'Templates', description: 'Tạo, sửa và bật/tắt template theo channel.', to: '/admin/notification-templates', icon: LayoutTemplate },
  { title: 'Delivery audit', description: 'Theo dõi trạng thái gửi và lỗi delivery.', to: '/admin/notification-delivery-logs', icon: FileClock },
  { title: 'AI Provider', description: 'Trạng thái provider/model read-only từ AI Service.', to: '/admin/ai-provider', icon: Bot },
]

export function AdminDashboardPage() {
  return <main className="admin-page"><header className="admin-page__hero"><div><span>Phase 5F</span><h1>Trung tâm quản trị</h1><p>Không gian vận hành bám hoàn toàn vào contract backend hiện có, với authorization tại từng business service.</p></div><ShieldCheck /></header><aside className="admin-feedback">User, Company và Application management hiện dùng API quản trị thật từ service sở hữu dữ liệu.</aside><section className="admin-dashboard-grid">{modules.map(({ title, description, to, icon: Icon }) => <Link className="admin-stat" to={to} key={to}><span><Icon /></span><div><strong>{title}</strong><small>{description}</small></div></Link>)}</section></main>
}
