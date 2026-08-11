import { BriefcaseBusiness, ChevronDown, Menu, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { BrandLogo } from '../ui/BrandLogo'
import { ButtonLink } from '../ui/Button'
import { MegaMenu } from './MegaMenu'
import { MobileUserMenu, UserMenu } from './UserMenu'
import { megaMenus } from './menu.config'
import './header.css'

export function Header() {
  const [activeMenu, setActiveMenu] = useState<string | null>(null)
  const [mobileOpen, setMobileOpen] = useState(false)
  const location = useLocation()

  useEffect(() => {
    setActiveMenu(null)
    setMobileOpen(false)
    document.body.classList.remove('nav-open')
  }, [location.pathname, location.search])

  const toggleMobile = () => {
    setMobileOpen((value) => {
      document.body.classList.toggle('nav-open', !value)
      return !value
    })
  }

  return (
    <header className="header">
      <div className="container header__inner">
        <BrandLogo />
        <nav className="header__desktop-nav" aria-label="Điều hướng chính">
          {megaMenus.map((menu) => (
            <button
              key={menu.label}
              type="button"
              className={activeMenu === menu.label ? 'is-active' : ''}
              onClick={() => setActiveMenu((current) => current === menu.label ? null : menu.label)}
              aria-expanded={activeMenu === menu.label}
            >
              {menu.label}<ChevronDown size={15} aria-hidden="true" />
            </button>
          ))}
        </nav>
        <div className="header__desktop-actions">
          <UserMenu />
          <ButtonLink className="header__employer-button" to="/employer" variant="dark" size="sm">
            <BriefcaseBusiness size={17} aria-hidden="true" /> Nhà tuyển dụng / Đăng tuyển
          </ButtonLink>
        </div>
        <button className="header__mobile-toggle" type="button" onClick={toggleMobile} aria-label={mobileOpen ? 'Đóng menu' : 'Mở menu'} aria-expanded={mobileOpen}>
          {mobileOpen ? <X /> : <Menu />}
        </button>
      </div>

      {activeMenu && (
        <div className="header__mega-wrap">
          <MegaMenu menu={megaMenus.find((menu) => menu.label === activeMenu)!} onNavigate={() => setActiveMenu(null)} />
        </div>
      )}

      {mobileOpen && (
        <>
          <button className="mobile-nav__overlay" type="button" aria-label="Đóng menu" onClick={toggleMobile} />
          <nav className="mobile-nav" aria-label="Điều hướng di động">
            <div className="mobile-nav__top"><BrandLogo /><button type="button" onClick={toggleMobile} aria-label="Đóng menu"><X /></button></div>
            <div className="mobile-nav__content">
              {megaMenus.map((menu) => (
                <details key={menu.label}>
                  <summary>{menu.label}<ChevronDown size={17} /></summary>
                  {menu.sections.map((section) => (
                    <div className="mobile-nav__section" key={section.title}>
                      <strong>{section.title}</strong>
                      {section.links.map((link) => <Link key={`${section.title}-${link.label}`} to={link.to}>{link.label}</Link>)}
                    </div>
                  ))}
                </details>
              ))}
            </div>
            <div className="mobile-nav__actions">
              <MobileUserMenu />
              <ButtonLink to="/employer" variant="dark" fullWidth><BriefcaseBusiness size={17} /> Nhà tuyển dụng / Đăng tuyển</ButtonLink>
            </div>
          </nav>
        </>
      )}
    </header>
  )
}
