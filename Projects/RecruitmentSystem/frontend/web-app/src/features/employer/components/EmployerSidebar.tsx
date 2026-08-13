import { BriefcaseBusiness, Building2, LayoutDashboard } from 'lucide-react'
import { NavLink } from 'react-router-dom'

const employerNavigation = [
  { label: 'Tổng quan', to: '/employer', icon: LayoutDashboard, end: true },
  { label: 'Công ty', to: '/employer/company', icon: Building2 },
  { label: 'Việc làm', to: '/employer/jobs', icon: BriefcaseBusiness },
]

export function EmployerSidebar({ onNavigate }: { onNavigate?: () => void }) {
  return <div className="employer-sidebar">
    <div className="employer-sidebar__heading"><span>Không gian nhà tuyển dụng</span><strong>Recruitment Workspace</strong></div>
    <nav aria-label="Điều hướng Employer Portal">{employerNavigation.map(({ label, to, icon: Icon, end }) =>
      <NavLink key={to} to={to} end={end} onClick={onNavigate} className={({ isActive }) => isActive ? 'is-active' : ''}><Icon aria-hidden="true" /><span>{label}</span></NavLink>,
    )}</nav>
    <div className="employer-sidebar__note"><strong>Phase 5C</strong><p>Quản lý Company và vòng đời Job theo ownership thực tế. Applicant Management thuộc phase sau.</p></div>
  </div>
}
