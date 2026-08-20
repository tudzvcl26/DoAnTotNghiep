import { ChevronDown, LogOut, UserRound } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../features/auth/auth-context'
import './product-account-menu.css'

export type ProductAccountLink = {
  label: string
  to: string
}

export function ProductAccountMenu({ label, links }: { label: string; links: ProductAccountLink[] }) {
  const { currentUser, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const [loggingOut, setLoggingOut] = useState(false)
  const wrapperRef = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()

  useEffect(() => {
    const closeOnPointer = (event: MouseEvent) => {
      if (!wrapperRef.current?.contains(event.target as Node)) setOpen(false)
    }
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', closeOnPointer)
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('mousedown', closeOnPointer)
      document.removeEventListener('keydown', closeOnEscape)
    }
  }, [])

  if (!currentUser) return null

  const initials = currentUser.fullName.trim().split(/\s+/).slice(-2).map((part) => part[0]).join('').toUpperCase()

  const handleLogout = async () => {
    setLoggingOut(true)
    try {
      await logout()
      navigate('/')
    } finally {
      setLoggingOut(false)
    }
  }

  return (
    <div className="product-account" ref={wrapperRef}>
      <button
        className="product-account__trigger"
        type="button"
        onClick={() => setOpen((value) => !value)}
        aria-expanded={open}
        aria-haspopup="menu"
      >
        <span className="product-account__avatar">{initials || <UserRound size={17} />}</span>
        <span className="product-account__identity"><strong>{currentUser.fullName}</strong><small>{label}</small></span>
        <ChevronDown size={15} aria-hidden="true" />
      </button>
      {open && (
        <div className="product-account__menu" role="menu">
          <div className="product-account__summary"><strong>{currentUser.fullName}</strong><small>{currentUser.email}</small></div>
          <nav aria-label={`Điều hướng tài khoản ${label}`}>
            {links.map((link) => <Link role="menuitem" key={link.to} to={link.to} onClick={() => setOpen(false)}>{link.label}</Link>)}
          </nav>
          <button role="menuitem" type="button" disabled={loggingOut} onClick={() => void handleLogout()}>
            <LogOut size={16} aria-hidden="true" /> {loggingOut ? 'Đang đăng xuất…' : 'Đăng xuất'}
          </button>
        </div>
      )}
    </div>
  )
}
