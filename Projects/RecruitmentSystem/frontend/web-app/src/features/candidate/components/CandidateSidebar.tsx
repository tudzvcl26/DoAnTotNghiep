import { Bell, BrainCircuit, FileText, LayoutDashboard, Settings, UserRound, Workflow } from 'lucide-react'
import { NavLink } from 'react-router-dom'

const candidateNavigation = [
  { label: 'Tổng quan', to: '/candidate', icon: LayoutDashboard, end: true },
  { label: 'Hồ sơ của tôi', to: '/candidate/profile', icon: UserRound },
  { label: 'CV của tôi', to: '/candidate/resumes', icon: FileText },
  { label: 'Đơn ứng tuyển', to: '/candidate/applications', icon: Workflow },
  { label: 'Thông báo', to: '/candidate/notifications', icon: Bell },
  { label: 'AI Career', to: '/candidate/ai-career', icon: BrainCircuit },
  { label: 'Cài đặt', to: '/candidate/settings', icon: Settings },
]

export function CandidateSidebar({ onNavigate }: { onNavigate?: () => void }) {
  return (
    <div className="candidate-sidebar">
      <div className="candidate-sidebar__heading">
        <span>Không gian ứng viên</span>
        <strong>Career Management</strong>
      </div>
      <nav aria-label="Điều hướng Candidate Portal">
        {candidateNavigation.map(({ label, to, icon: Icon, end }) => (
          <NavLink key={to} to={to} end={end} onClick={onNavigate} className={({ isActive }) => isActive ? 'is-active' : ''}>
            <Icon size={19} aria-hidden="true" /><span>{label}</span>
          </NavLink>
        ))}
      </nav>
      <div className="candidate-sidebar__note">
        <strong>Phát triển sự nghiệp</strong>
        <p>Quản lý hồ sơ và theo dõi hành trình ứng tuyển tại một nơi.</p>
      </div>
    </div>
  )
}
