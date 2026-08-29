import { Bell, BrainCircuit, BriefcaseBusiness, Building2, FilePlus2, FileText, LayoutDashboard, Settings, UserRound, Workflow } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { NavLink } from 'react-router-dom'

type CandidateItem = { label: string; icon: LucideIcon; to?: string; end?: boolean; planned?: boolean }

const groups: { label: string; items: CandidateItem[] }[] = [
  { label: 'Khám phá', items: [
    { label: 'Việc làm', to: '/jobs', icon: BriefcaseBusiness },
    { label: 'Công ty', to: '/companies', icon: Building2 },
  ] },
  { label: 'Sự nghiệp của tôi', items: [
    { label: 'Career dashboard', to: '/candidate', icon: LayoutDashboard, end: true },
    { label: 'Hồ sơ của tôi', to: '/candidate/profile', icon: UserRound },
    { label: 'CV đã tạo', to: '/cv', icon: FileText },
    { label: 'Tạo CV mới', to: '/cv/templates', icon: FilePlus2 },
    { label: 'CV tải lên', to: '/candidate/resumes', icon: FileText },
    { label: 'Đơn ứng tuyển', to: '/candidate/applications', icon: Workflow },
    { label: 'AI Career', to: '/candidate/ai-career', icon: BrainCircuit },
  ] },
  { label: 'Tài khoản', items: [
    { label: 'Thông báo', to: '/candidate/notifications', icon: Bell },
    { label: 'Cài đặt', icon: Settings, planned: true },
  ] },
]

export function CandidateSidebar({ onNavigate }: { onNavigate?: () => void }) {
  return (
    <div className="candidate-sidebar">
      <div className="candidate-sidebar__heading"><span>Job seeker product</span><strong>Career workspace</strong></div>
      <nav aria-label="Điều hướng Career">
        {groups.map((group) => <div className="candidate-sidebar__group" key={group.label}>
          <strong>{group.label}</strong>
          {group.items.map(({ label, to, icon: Icon, end, planned }) => planned || !to
            ? <span className="candidate-sidebar__planned" aria-disabled="true" key={label}><Icon aria-hidden="true" /><span>{label}</span><small>Planned</small></span>
            : <NavLink key={to} to={to} end={end} onClick={onNavigate} className={({ isActive }) => isActive ? 'is-active' : ''}><Icon aria-hidden="true" /><span>{label}</span></NavLink>,
          )}
        </div>)}
      </nav>
      <div className="candidate-sidebar__note"><strong>Sẵn sàng cho cơ hội mới</strong><p>Hồ sơ, CV và hành trình ứng tuyển của bạn ở cùng một nơi.</p></div>
    </div>
  )
}
