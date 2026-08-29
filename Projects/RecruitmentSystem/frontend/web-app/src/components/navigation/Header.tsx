import { BriefcaseBusiness, ChevronDown, Menu, X } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
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
  const headerRef = useRef<HTMLElement>(null)
  const closeTimer = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    setActiveMenu(null)
    setMobileOpen(false)
    document.body.classList.remove('nav-open')
  }, [location.hash, location.pathname, location.search])

  useEffect(() => {
    const closeOnOutside = (event: PointerEvent) => {
      if (headerRef.current && !headerRef.current.contains(event.target as Node)) setActiveMenu(null)
    }
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setActiveMenu(null)
    }
    document.addEventListener('pointerdown', closeOnOutside)
    document.addEventListener('keydown', closeOnEscape)
    return () => { document.removeEventListener('pointerdown', closeOnOutside); document.removeEventListener('keydown', closeOnEscape) }
  }, [])

  const cancelClose = () => { if (closeTimer.current) clearTimeout(closeTimer.current) }
  const scheduleClose = () => { cancelClose(); closeTimer.current = setTimeout(() => setActiveMenu(null), 180) }
  const openMenu = (label: string) => { cancelClose(); setActiveMenu(label) }

  const toggleMobile = () => {
    setMobileOpen((value) => {
      document.body.classList.toggle('nav-open', !value)
      return !value
    })
  }

  return (
    <header className="header" ref={headerRef} onMouseEnter={cancelClose} onMouseLeave={scheduleClose}>
      <div className="container header__inner">
        <BrandLogo />
        <nav className="header__desktop-nav" aria-label="Điều hướng chính">
          {megaMenus.map((menu) => (
            <button
              key={menu.label}
              type="button"
              className={`${activeMenu === menu.label ? 'is-open ' : ''}${menu.activePrefixes.some((prefix) => location.pathname.startsWith(prefix)) ? 'is-active' : ''}`.trim()}
              onMouseEnter={() => openMenu(menu.label)}
              onClick={() => openMenu(menu.label)}
              onKeyDown={(event) => {
                if (event.key === 'ArrowDown') {
                  event.preventDefault(); openMenu(menu.label)
                  requestAnimationFrame(() => headerRef.current?.querySelector<HTMLAnchorElement>('.mega-menu a')?.focus())
                }
              }}
              aria-expanded={activeMenu === menu.label}
              aria-haspopup="true"
            >
              {menu.label}<ChevronDown className="header__chevron" size={15} aria-hidden="true" />
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
                  <summary>{menu.label}<ChevronDown className="header__chevron" size={17} /></summary>
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
