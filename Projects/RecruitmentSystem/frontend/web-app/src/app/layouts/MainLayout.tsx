import { Outlet } from 'react-router-dom'
import { Footer } from '../../components/navigation/Footer'
import { Header } from '../../components/navigation/Header'

export function MainLayout() {
  return (
    <div className="app-shell">
      <Header />
      <main className="app-main"><Outlet /></main>
      <Footer />
    </div>
  )
}
