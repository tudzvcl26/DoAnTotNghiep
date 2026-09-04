import { BarChart3, Bell, Bot, BriefcaseBusiness, Building2, FolderKanban, LayoutDashboard, Settings, UserSearch, UsersRound, Warehouse } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { NavLink } from 'react-router-dom'

type EmployerItem = { label: string; icon: LucideIcon; to?: string; end?: boolean; planned?: boolean }

const groups: { label: string; items: EmployerItem[] }[] = [
  { label: 'Workspace', items: [
    { label: 'Dashboard', to: '/employer', icon: LayoutDashboard, end: true },
    { label: 'Việc làm', to: '/employer/jobs', icon: BriefcaseBusiness },
    { label: 'Ứng viên & đơn', to: '/employer/applications', icon: UsersRound },
    { label: 'Thông báo', to: '/employer/notifications', icon: Bell },
  ] },
  { label: 'Recruitment', items: [
    { label: 'Chiến dịch', icon: FolderKanban, planned: true },
    { label: 'Ứng viên', icon: UserSearch, planned: true },
    { label: 'Talent Pool', icon: Warehouse, planned: true },
  ] },
  { label: 'Insights', items: [
    { label: 'Analytics', icon: BarChart3, planned: true },
    { label: 'AI Recruitment', icon: Bot, planned: true },
  ] },
  { label: 'Organization', items: [
    { label: 'Công ty', to: '/employer/company', icon: Building2 },
    { label: 'Cài đặt', icon: Settings, planned: true },
  ] },
]

export function EmployerSidebar({ onNavigate }: { onNavigate?: () => void }) {
  return <div className="employer-sidebar">
    <div className="employer-sidebar__heading"><span>Recruitment platform</span><strong>Hiring Workspace</strong></div>
    <nav aria-label="Điều hướng Recruitment Workspace">
      {groups.map((group) => <div className="employer-sidebar__group" key={group.label}>
        <strong>{group.label}</strong>
        {group.items.map(({ label, to, icon: Icon, end, planned }) => planned || !to
          ? <span className="employer-sidebar__planned" aria-disabled="true" key={label}><Icon aria-hidden="true" /><span>{label}</span><small>Chưa triển khai</small></span>
          : <NavLink key={to} to={to} end={end} onClick={onNavigate} className={({ isActive }) => isActive ? 'is-active' : ''}><Icon aria-hidden="true" /><span>{label}</span></NavLink>,
        )}
      </div>)}
    </nav>
    <div className="employer-sidebar__note"><strong>Owner-scoped workspace</strong><p>Company, Job và Application tiếp tục dùng dữ liệu thật và authorization hiện tại.</p></div>
  </div>
}
