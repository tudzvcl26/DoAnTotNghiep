import { Building2, LayoutDashboard } from 'lucide-react'
import { NavLink } from 'react-router-dom'

const employerNavigation = [
  { label: 'Tổng quan', to: '/employer', icon: LayoutDashboard, end: true },
  { label: 'Công ty', to: '/employer/company', icon: Building2 },
]

export function EmployerSidebar({ onNavigate }: { onNavigate?: () => void }) {
  return <div className="employer-sidebar">
    <div className="employer-sidebar__heading"><span>Không gian nhà tuyển dụng</span><strong>Recruitment Workspace</strong></div>
    <nav aria-label="Điều hướng Employer Portal">{employerNavigation.map(({ label, to, icon: Icon, end }) =>
      <NavLink key={to} to={to} end={end} onClick={onNavigate} className={({ isActive }) => isActive ? 'is-active' : ''}><Icon aria-hidden="true" /><span>{label}</span></NavLink>,
    )}</nav>
    <div className="employer-sidebar__note"><strong>Phase 5A</strong><p>Dashboard và hồ sơ công ty. Quản lý tin tuyển dụng, ứng viên sẽ được mở ở phase tiếp theo.</p></div>
  </div>
}
