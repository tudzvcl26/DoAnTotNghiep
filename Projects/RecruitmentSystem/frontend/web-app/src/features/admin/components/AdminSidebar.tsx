import { BellRing, Bot, Boxes, CircleGauge, FileClock, LayoutTemplate, ListTree, Sparkles, Tags } from 'lucide-react'
import { NavLink } from 'react-router-dom'

const navigation = [
  { label: 'Tổng quan', to: '/admin', icon: CircleGauge, end: true },
  { label: 'Ngành nghề', to: '/admin/catalog/categories', icon: ListTree },
  { label: 'Kỹ năng', to: '/admin/catalog/skills', icon: Sparkles },
  { label: 'Phúc lợi', to: '/admin/catalog/benefits', icon: Tags },
  { label: 'Thông báo', to: '/admin/notifications', icon: BellRing },
  { label: 'Mẫu thông báo', to: '/admin/notification-templates', icon: LayoutTemplate },
  { label: 'Delivery logs', to: '/admin/notification-delivery-logs', icon: FileClock },
  { label: 'AI Provider', to: '/admin/ai-provider', icon: Bot },
]

export function AdminSidebar({ onNavigate }: { onNavigate?: () => void }) {
  return <div className="admin-sidebar">
    <div className="admin-sidebar__heading"><Boxes /><div><span>System operations</span><strong>Admin Portal</strong></div></div>
    <nav aria-label="Điều hướng Admin Portal">{navigation.map(({ label, to, icon: Icon, end }) => <NavLink key={to} to={to} end={end} onClick={onNavigate} className={({ isActive }) => isActive ? 'is-active' : ''}><Icon /><span>{label}</span></NavLink>)}</nav>
    <aside><strong>Backend là source of truth</strong><p>Mọi mutation vẫn được kiểm soát bởi JWT và `ADMIN` authorization tại service.</p></aside>
  </div>
}
