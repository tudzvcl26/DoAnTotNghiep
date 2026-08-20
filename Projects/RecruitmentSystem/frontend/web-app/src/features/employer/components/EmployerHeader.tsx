import { BriefcaseBusiness, Building2, Menu, Plus, UsersRound } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { Link, NavLink } from 'react-router-dom'
import { ProductAccountMenu } from '../../../components/navigation/ProductAccountMenu'
import { BrandLogo } from '../../../components/ui/BrandLogo'
import { useAuth } from '../../auth/auth-context'
import { employerCompanyKey, getEmployerCompanies } from '../employer.api'

const accountLinks = [
  { label: 'Recruitment dashboard', to: '/employer' },
  { label: 'Công ty', to: '/employer/company' },
  { label: 'Việc làm', to: '/employer/jobs' },
  { label: 'Ứng viên & đơn', to: '/employer/applications' },
]

export function EmployerHeader({ onOpenMenu, menuOpen }: { onOpenMenu: () => void; menuOpen: boolean }) {
  const { currentUser } = useAuth()
  const companies = useQuery({
    queryKey: employerCompanyKey(currentUser?.id ?? ''),
    queryFn: () => getEmployerCompanies(currentUser?.id ?? ''),
    enabled: Boolean(currentUser?.id),
  })
  const companyName = companies.data?.[0]?.name ?? (companies.isPending ? 'Đang tải doanh nghiệp…' : 'Chưa có doanh nghiệp')

  return (
    <header className="employer-product-header">
      <button className="employer-product-header__menu" type="button" onClick={onOpenMenu} aria-label="Mở menu Recruitment Workspace" aria-expanded={menuOpen} aria-controls="employer-mobile-drawer"><Menu /></button>
      <div className="employer-product-header__brand"><BrandLogo /><span>Recruitment</span></div>
      <div className="employer-product-header__company"><Building2 size={16} /><span><small>Doanh nghiệp</small><strong>{companyName}</strong></span></div>
      <nav className="employer-product-header__nav" aria-label="Điều hướng tuyển dụng nhanh">
        <NavLink to="/employer/jobs"><BriefcaseBusiness size={16} /> Việc làm</NavLink>
        <NavLink to="/employer/applications"><UsersRound size={16} /> Ứng viên</NavLink>
      </nav>
      <div className="employer-product-header__actions">
        <Link className="employer-product-header__create" to="/employer/jobs/new"><Plus size={17} /> <span>Tạo việc làm</span></Link>
        <ProductAccountMenu label="Nhà tuyển dụng" links={accountLinks} />
      </div>
    </header>
  )
}
