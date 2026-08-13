import { Menu, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { EmployerSidebar } from '../../features/employer/components/EmployerSidebar'
import '../../features/employer/employer-portal.css'

export function EmployerLayout() {
  const [drawerOpen, setDrawerOpen] = useState(false)
  const location = useLocation()

  useEffect(() => setDrawerOpen(false), [location.pathname])
  useEffect(() => {
    if (!drawerOpen) return
    const close = (event: KeyboardEvent) => { if (event.key === 'Escape') setDrawerOpen(false) }
    document.addEventListener('keydown', close)
    document.body.classList.add('employer-drawer-open')
    return () => { document.removeEventListener('keydown', close); document.body.classList.remove('employer-drawer-open') }
  }, [drawerOpen])

  return <div className="employer-shell">
    <aside className="employer-shell__sidebar"><EmployerSidebar /></aside>
    <div className="employer-shell__workspace">
      <div className="employer-mobile-bar"><button type="button" onClick={() => setDrawerOpen(true)} aria-expanded={drawerOpen} aria-controls="employer-mobile-drawer"><Menu aria-hidden="true" /> Menu quản lý</button><span>Employer Portal</span></div>
      <Outlet />
    </div>
    {drawerOpen && <><button className="employer-drawer__overlay" type="button" aria-label="Đóng menu Employer" onClick={() => setDrawerOpen(false)} /><aside className="employer-drawer" id="employer-mobile-drawer" aria-label="Menu Employer trên di động"><div className="employer-drawer__top"><strong>Recruitment Workspace</strong><button type="button" onClick={() => setDrawerOpen(false)} aria-label="Đóng menu"><X /></button></div><EmployerSidebar onNavigate={() => setDrawerOpen(false)} /></aside></>}
  </div>
}
