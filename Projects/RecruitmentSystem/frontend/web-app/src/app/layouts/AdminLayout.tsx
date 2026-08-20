import { X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { AdminHeader } from '../../features/admin/components/AdminHeader'
import { AdminSidebar } from '../../features/admin/components/AdminSidebar'
import '../../features/admin/admin-portal.css'

export function AdminLayout() {
  const [drawerOpen, setDrawerOpen] = useState(false)
  const location = useLocation()

  useEffect(() => setDrawerOpen(false), [location.pathname])
  useEffect(() => {
    if (!drawerOpen) return
    const close = (event: KeyboardEvent) => { if (event.key === 'Escape') setDrawerOpen(false) }
    document.addEventListener('keydown', close)
    document.body.classList.add('admin-drawer-open')
    return () => { document.removeEventListener('keydown', close); document.body.classList.remove('admin-drawer-open') }
  }, [drawerOpen])

  return <div className="admin-product-shell">
    <AdminHeader onOpenMenu={() => setDrawerOpen(true)} menuOpen={drawerOpen} />
    <div className="admin-shell">
      <aside className="admin-shell__sidebar"><AdminSidebar /></aside>
      <div className="admin-shell__workspace"><Outlet /></div>
    </div>
    {drawerOpen && <><button className="admin-drawer__overlay" type="button" aria-label="Đóng menu quản trị" onClick={() => setDrawerOpen(false)} /><aside className="admin-drawer" id="admin-mobile-drawer" aria-label="Menu Admin trên di động" aria-modal="true" role="dialog"><div className="admin-drawer__top"><strong>System Operations</strong><button type="button" onClick={() => setDrawerOpen(false)} aria-label="Đóng menu"><X /></button></div><AdminSidebar onNavigate={() => setDrawerOpen(false)} /></aside></>}
  </div>
}
