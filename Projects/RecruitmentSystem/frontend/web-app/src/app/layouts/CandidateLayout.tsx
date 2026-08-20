import { X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { CandidateHeader } from '../../features/candidate/components/CandidateHeader'
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
    <div className="candidate-product-shell">
      <CandidateHeader onOpenMenu={() => setDrawerOpen(true)} menuOpen={drawerOpen} />
      <div className="candidate-shell">
        <aside className="candidate-shell__sidebar">
          <CandidateSidebar />
        </aside>
        <div className="candidate-shell__workspace"><Outlet /></div>
      </div>

      {drawerOpen && (
        <>
          <button className="candidate-drawer__overlay" type="button" aria-label="Đóng menu Candidate" onClick={() => setDrawerOpen(false)} />
          <aside className="candidate-drawer" id="candidate-mobile-drawer" aria-label="Menu Career trên di động" aria-modal="true" role="dialog">
            <div className="candidate-drawer__top">
              <strong>Career</strong>
              <button type="button" onClick={() => setDrawerOpen(false)} aria-label="Đóng menu"><X size={21} /></button>
            </div>
            <CandidateSidebar onNavigate={() => setDrawerOpen(false)} />
          </aside>
        </>
      )}
    </div>
  )
}
