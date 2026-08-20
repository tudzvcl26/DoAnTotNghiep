import { Bell, BriefcaseBusiness, Building2, Menu, Search } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { Link, NavLink } from 'react-router-dom'
import { BrandLogo } from '../../../components/ui/BrandLogo'
import { ProductAccountMenu } from '../../../components/navigation/ProductAccountMenu'
import { getUnreadNotificationCount } from '../../notifications/notifications.api'
import { useAuth } from '../../auth/auth-context'

const accountLinks = [
  { label: 'Career dashboard', to: '/candidate' },
  { label: 'Hồ sơ của tôi', to: '/candidate/profile' },
  { label: 'CV của tôi', to: '/candidate/resumes' },
  { label: 'Đơn ứng tuyển', to: '/candidate/applications' },
  { label: 'AI Career', to: '/candidate/ai-career' },
]

export function CandidateHeader({ onOpenMenu, menuOpen }: { onOpenMenu: () => void; menuOpen: boolean }) {
  const { currentUser } = useAuth()
  const unread = useQuery({
    queryKey: ['candidate-notification-unread', currentUser?.id],
    queryFn: getUnreadNotificationCount,
    enabled: Boolean(currentUser?.id),
  })
  const unreadCount = unread.data?.unreadCount ?? 0

  return (
    <header className="candidate-product-header">
      <div className="candidate-product-header__inner">
        <button className="candidate-product-header__menu" type="button" onClick={onOpenMenu} aria-label="Mở menu Career" aria-expanded={menuOpen} aria-controls="candidate-mobile-drawer"><Menu /></button>
        <div className="candidate-product-header__brand"><BrandLogo /><span>Career</span></div>
        <nav className="candidate-product-header__nav" aria-label="Điều hướng tìm việc">
          <NavLink to="/jobs"><BriefcaseBusiness size={17} /> Việc làm</NavLink>
          <NavLink to="/companies"><Building2 size={17} /> Công ty</NavLink>
        </nav>
        <div className="candidate-product-header__actions">
          <Link className="candidate-product-header__search" to="/jobs"><Search size={17} /> <span>Tìm việc</span></Link>
          <Link className="candidate-product-header__notification" to="/candidate/notifications" aria-label={unreadCount ? `${unreadCount} thông báo chưa đọc` : 'Thông báo'}>
            <Bell size={19} />{unreadCount > 0 && <span>{unreadCount > 99 ? '99+' : unreadCount}</span>}
          </Link>
          <ProductAccountMenu label="Ứng viên" links={accountLinks} />
        </div>
      </div>
    </header>
  )
}
