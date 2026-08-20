import { X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { EmployerHeader } from '../../features/employer/components/EmployerHeader'
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

  return <div className="employer-product-shell">
    <EmployerHeader onOpenMenu={() => setDrawerOpen(true)} menuOpen={drawerOpen} />
    <div className="employer-shell">
      <aside className="employer-shell__sidebar"><EmployerSidebar /></aside>
      <div className="employer-shell__workspace"><Outlet /></div>
    </div>
    {drawerOpen && <><button className="employer-drawer__overlay" type="button" aria-label="Đóng menu Employer" onClick={() => setDrawerOpen(false)} /><aside className="employer-drawer" id="employer-mobile-drawer" aria-label="Menu Employer trên di động" aria-modal="true" role="dialog"><div className="employer-drawer__top"><strong>Recruitment Workspace</strong><button type="button" onClick={() => setDrawerOpen(false)} aria-label="Đóng menu"><X /></button></div><EmployerSidebar onNavigate={() => setDrawerOpen(false)} /></aside></>}
  </div>
}
