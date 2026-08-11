import { Bell, ChevronDown, LogOut, UserRound } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../features/auth/auth-context'
import type { UserRole } from '../../types/enums/auth'
import { Button, ButtonLink } from '../ui/Button'

const menus: Record<UserRole, { label: string; to: string }[]> = {
  CANDIDATE: [
    { label: 'Dashboard', to: '/candidate' }, { label: 'Hồ sơ', to: '/candidate' },
    { label: 'CV của tôi', to: '/candidate' }, { label: 'Đơn ứng tuyển', to: '/candidate' },
    { label: 'Việc đã lưu', to: '/candidate' }, { label: 'Thông báo', to: '/candidate' },
    { label: 'Cài đặt', to: '/candidate' },
  ],
  EMPLOYER: [
    { label: 'Dashboard', to: '/employer' }, { label: 'Công ty', to: '/employer' },
    { label: 'Tin tuyển dụng', to: '/employer' }, { label: 'Ứng viên', to: '/employer' },
    { label: 'Thông báo', to: '/employer' }, { label: 'Cài đặt', to: '/employer' },
  ],
  ADMIN: [
    { label: 'Dashboard', to: '/admin' }, { label: 'Người dùng', to: '/admin' },
    { label: 'Công ty', to: '/admin' }, { label: 'Việc làm', to: '/admin' },
    { label: 'Danh mục', to: '/admin' }, { label: 'Kỹ năng', to: '/admin' },
    { label: 'Thông báo', to: '/admin' }, { label: 'Cài đặt', to: '/admin' },
  ],
}

function resolveRole(roles: string[]): UserRole {
  if (roles.some((role) => role.replace('ROLE_', '') === 'ADMIN')) return 'ADMIN'
  if (roles.some((role) => role.replace('ROLE_', '') === 'EMPLOYER')) return 'EMPLOYER'
  return 'CANDIDATE'
}

export function UserMenu() {
  const { currentUser, isAuthenticated, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const wrapperRef = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()

  useEffect(() => {
    const close = (event: MouseEvent) => {
      if (!wrapperRef.current?.contains(event.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', close)
    return () => document.removeEventListener('mousedown', close)
  }, [])

  if (!isAuthenticated || !currentUser) {
    return (
      <div className="header__guest-actions">
        <Link className="header__notification" to="/login" aria-label="Đăng nhập để xem thông báo"><Bell size={20} /></Link>
        <ButtonLink to="/login" variant="ghost" size="sm">Đăng nhập</ButtonLink>
        <ButtonLink to="/register" variant="secondary" size="sm">Đăng ký</ButtonLink>
      </div>
    )
  }

  const role = resolveRole(currentUser.roles)
  const initials = currentUser.fullName.trim().split(/\s+/).slice(-2).map((part) => part[0]).join('').toUpperCase()

  const handleLogout = async () => {
    setOpen(false)
    await logout()
    navigate('/')
  }

  return (
    <div className="user-menu" ref={wrapperRef}>
      <Link className="header__notification" to={`/${role.toLowerCase()}`} aria-label="Thông báo"><Bell size={20} /></Link>
      <button className="user-menu__trigger" type="button" onClick={() => setOpen((value) => !value)} aria-expanded={open}>
        <span className="user-menu__avatar">{initials || <UserRound size={18} />}</span>
        <span className="user-menu__identity"><strong>{currentUser.fullName}</strong><small>{role}</small></span>
        <ChevronDown size={16} aria-hidden="true" />
      </button>
      {open && (
        <div className="user-menu__panel">
          {menus[role].map((item) => <Link key={item.label} to={item.to} onClick={() => setOpen(false)}>{item.label}</Link>)}
          <button type="button" onClick={handleLogout}><LogOut size={16} /> Đăng xuất</button>
        </div>
      )}
    </div>
  )
}

export function MobileUserMenu() {
  const { currentUser, isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()

  if (!isAuthenticated || !currentUser) {
    return (
      <>
        <ButtonLink to="/login" variant="ghost" fullWidth>Đăng nhập</ButtonLink>
        <ButtonLink to="/register" variant="secondary" fullWidth>Đăng ký</ButtonLink>
      </>
    )
  }

  const role = resolveRole(currentUser.roles)
  return (
    <div className="mobile-user-menu">
      <div className="mobile-user-menu__identity"><span>{currentUser.fullName}</span><small>{role}</small></div>
      <div className="mobile-user-menu__links">
        {menus[role].map((item) => <Link key={item.label} to={item.to}>{item.label}</Link>)}
      </div>
      <Button type="button" variant="ghost" fullWidth onClick={async () => { await logout(); navigate('/') }}><LogOut size={17} /> Đăng xuất</Button>
    </div>
  )
}
