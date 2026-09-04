import { Activity, BellRing, Bot, Boxes, Building2, CircleGauge, ClipboardList, FileClock, FileUser, LayoutTemplate, ListTree, Settings, ShieldCheck, Sparkles, Tags, UsersRound } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { NavLink } from 'react-router-dom'

type AdminItem = { label: string; icon: LucideIcon; to?: string; end?: boolean; planned?: boolean }

const groups: { label: string; items: AdminItem[] }[] = [
  { label: 'Operations', items: [
    { label: 'Dashboard', to: '/admin', icon: CircleGauge, end: true },
    { label: 'Người dùng', to: '/admin/users', icon: UsersRound },
    { label: 'Công ty', to: '/admin/companies', icon: Building2 },
    { label: 'Việc làm', to: '/admin/jobs', icon: ClipboardList },
    { label: 'Applications', to: '/admin/applications', icon: FileUser },
  ] },
  { label: 'Platform', items: [
    { label: 'Ngành nghề', to: '/admin/catalog/categories', icon: ListTree },
    { label: 'Kỹ năng', to: '/admin/catalog/skills', icon: Sparkles },
    { label: 'Phúc lợi', to: '/admin/catalog/benefits', icon: Tags },
    { label: 'Thông báo', to: '/admin/notifications', icon: BellRing },
    { label: 'Mẫu thông báo', to: '/admin/notification-templates', icon: LayoutTemplate },
    { label: 'Delivery logs', to: '/admin/notification-delivery-logs', icon: FileClock },
  ] },
  { label: 'System', items: [
    { label: 'AI Provider', to: '/admin/ai-provider', icon: Bot },
    { label: 'Audit', icon: ShieldCheck, planned: true },
    { label: 'System Health', icon: Activity, planned: true },
    { label: 'Cài đặt', icon: Settings, planned: true },
  ] },
]

export function AdminSidebar({ onNavigate }: { onNavigate?: () => void }) {
  return <div className="admin-sidebar">
    <div className="admin-sidebar__heading"><Boxes /><div><span>Internal console</span><strong>System Operations</strong></div></div>
    <nav aria-label="Điều hướng System Operations">
      {groups.map((group) => <div className="admin-sidebar__group" key={group.label}>
        <strong>{group.label}</strong>
        {group.items.map(({ label, to, icon: Icon, end, planned }) => planned || !to
          ? <span className="admin-sidebar__planned" aria-disabled="true" key={label}><Icon aria-hidden="true" /><span>{label}</span><small>Chưa triển khai</small></span>
          : <NavLink key={to} to={to} end={end} onClick={onNavigate} className={({ isActive }) => isActive ? 'is-active' : ''}><Icon aria-hidden="true" /><span>{label}</span></NavLink>,
        )}
      </div>)}
    </nav>
    <aside><strong>Backend là source of truth</strong><p>Frontend guard chỉ hỗ trợ UX; mọi mutation vẫn được kiểm soát tại business service.</p></aside>
  </div>
}
