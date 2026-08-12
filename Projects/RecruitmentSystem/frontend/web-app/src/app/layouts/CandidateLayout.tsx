import { Menu, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { CandidateSidebar } from '../../features/candidate/components/CandidateSidebar'
import '../../features/candidate/candidate-page.css'

export function CandidateLayout() {
  const [drawerOpen, setDrawerOpen] = useState(false)
  const location = useLocation()

  useEffect(() => {
    setDrawerOpen(false)
  }, [location.pathname, location.search])

  useEffect(() => {
    if (!drawerOpen) return
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setDrawerOpen(false)
    }
    document.addEventListener('keydown', closeOnEscape)
    document.body.classList.add('candidate-drawer-open')
    return () => {
      document.removeEventListener('keydown', closeOnEscape)
      document.body.classList.remove('candidate-drawer-open')
    }
  }, [drawerOpen])

  return (
    <div className="candidate-shell">
      <aside className="candidate-shell__sidebar">
        <CandidateSidebar />
      </aside>

      <div className="candidate-shell__workspace">
        <div className="candidate-mobile-bar">
          <button type="button" onClick={() => setDrawerOpen(true)} aria-expanded={drawerOpen} aria-controls="candidate-mobile-drawer">
            <Menu size={20} aria-hidden="true" /> Menu quản lý
          </button>
          <span>Candidate Portal</span>
        </div>
        <Outlet />
      </div>

      {drawerOpen && (
        <>
          <button className="candidate-drawer__overlay" type="button" aria-label="Đóng menu Candidate" onClick={() => setDrawerOpen(false)} />
          <aside className="candidate-drawer" id="candidate-mobile-drawer" aria-label="Menu Candidate trên di động">
            <div className="candidate-drawer__top">
              <strong>Career Management</strong>
              <button type="button" onClick={() => setDrawerOpen(false)} aria-label="Đóng menu"><X size={21} /></button>
            </div>
            <CandidateSidebar onNavigate={() => setDrawerOpen(false)} />
          </aside>
        </>
      )}
    </div>
  )
}
