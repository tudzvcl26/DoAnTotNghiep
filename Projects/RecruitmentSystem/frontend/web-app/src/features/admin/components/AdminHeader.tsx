import { LockKeyhole, Menu } from 'lucide-react'
import { ProductAccountMenu } from '../../../components/navigation/ProductAccountMenu'
import { BrandLogo } from '../../../components/ui/BrandLogo'

const accountLinks = [
  { label: 'Operations dashboard', to: '/admin' },
  { label: 'Người dùng', to: '/admin/users' },
  { label: 'Công ty', to: '/admin/companies' },
  { label: 'Applications', to: '/admin/applications' },
  { label: 'AI Provider', to: '/admin/ai-provider' },
]

export function AdminHeader({ onOpenMenu, menuOpen }: { onOpenMenu: () => void; menuOpen: boolean }) {
  return (
    <header className="admin-product-header">
      <button className="admin-product-header__menu" type="button" onClick={onOpenMenu} aria-label="Mở menu System Operations" aria-expanded={menuOpen} aria-controls="admin-mobile-drawer"><Menu /></button>
      <div className="admin-product-header__brand"><BrandLogo /><span>Operations</span></div>
      <div className="admin-product-header__context"><LockKeyhole size={15} /><span>Internal administration console</span></div>
      <ProductAccountMenu label="Quản trị viên" links={accountLinks} />
    </header>
  )
}
