import { LogOut, Menu, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { AdminSidebar } from '../../features/admin/components/AdminSidebar'
import { useAuth } from '../../features/auth/auth-context'
import '../../features/admin/admin-portal.css'

export function AdminLayout() {
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [loggingOut, setLoggingOut] = useState(false)
  const location = useLocation()
  const { currentUser, logout } = useAuth()

  useEffect(() => setDrawerOpen(false), [location.pathname])
  useEffect(() => {
    if (!drawerOpen) return
    const close = (event: KeyboardEvent) => { if (event.key === 'Escape') setDrawerOpen(false) }
    document.addEventListener('keydown', close)
    document.body.classList.add('admin-drawer-open')
    return () => { document.removeEventListener('keydown', close); document.body.classList.remove('admin-drawer-open') }
  }, [drawerOpen])

  const handleLogout = async () => {
    setLoggingOut(true)
    try { await logout() } finally { window.location.assign('/login') }
  }

  return <div className="admin-shell">
    <aside className="admin-shell__sidebar"><AdminSidebar /></aside>
    <div className="admin-shell__workspace">
      <header className="admin-topbar">
        <button className="admin-topbar__menu" type="button" onClick={() => setDrawerOpen(true)} aria-label="Mở menu quản trị"><Menu /></button>
        <div><span>Quản trị viên</span><strong>{currentUser?.fullName ?? currentUser?.email}</strong></div>
        <button className="admin-topbar__logout" type="button" disabled={loggingOut} onClick={() => void handleLogout()}><LogOut />{loggingOut ? 'Đang thoát…' : 'Đăng xuất'}</button>
      </header>
      <Outlet />
    </div>
    {drawerOpen && <><button className="admin-drawer__overlay" type="button" aria-label="Đóng menu quản trị" onClick={() => setDrawerOpen(false)} /><aside className="admin-drawer" aria-label="Menu Admin trên di động"><div className="admin-drawer__top"><strong>Admin Portal</strong><button type="button" onClick={() => setDrawerOpen(false)} aria-label="Đóng menu"><X /></button></div><AdminSidebar onNavigate={() => setDrawerOpen(false)} /></aside></>}
  </div>
}
