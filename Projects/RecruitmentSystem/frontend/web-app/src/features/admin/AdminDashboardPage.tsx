import { ArrowRight, BellRing, Bot, Boxes, Building2, FileClock, FileUser, LayoutTemplate, ShieldCheck, UsersRound } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { Link } from 'react-router-dom'

type AdminModule = { title: string; description: string; to: string; icon: LucideIcon }

const operations: AdminModule[] = [
  { title: 'Người dùng', description: 'Role và trạng thái tài khoản tại Auth Service.', to: '/admin/users', icon: UsersRound },
  { title: 'Công ty', description: 'Hồ sơ và trạng thái xác minh doanh nghiệp.', to: '/admin/companies', icon: Building2 },
  { title: 'Applications', description: 'Tra cứu Application toàn hệ thống.', to: '/admin/applications', icon: FileUser },
]

const platform: AdminModule[] = [
  { title: 'Catalog', description: 'Category, Skill và Benefit CRUD.', to: '/admin/catalog/categories', icon: Boxes },
  { title: 'Thông báo', description: 'Gửi cá nhân, broadcast và tra cứu recipient.', to: '/admin/notifications', icon: BellRing },
  { title: 'Templates', description: 'Quản lý template theo channel.', to: '/admin/notification-templates', icon: LayoutTemplate },
  { title: 'Delivery logs', description: 'Điều tra trạng thái và lỗi delivery.', to: '/admin/notification-delivery-logs', icon: FileClock },
]

const system: AdminModule[] = [
  { title: 'AI Provider', description: 'Provider và model read-only từ AI Service.', to: '/admin/ai-provider', icon: Bot },
]

function CommandSection({ title, description, items, wide }: { title: string; description: string; items: AdminModule[]; wide?: boolean }) {
  return <section className={`admin-operations-section${wide ? ' admin-operations-section--wide' : ''}`}>
    <header><h2>{title}</h2><span>{description}</span></header>
    <div className="admin-command-list">{items.map(({ title: itemTitle, description: itemDescription, to, icon: Icon }) => <Link to={to} key={to}>
      <span><Icon aria-hidden="true" /></span><div><strong>{itemTitle}</strong><small>{itemDescription}</small></div><ArrowRight aria-hidden="true" />
    </Link>)}</div>
  </section>
}

export function AdminDashboardPage() {
  return <main className="admin-page">
    <header className="admin-page__hero"><div><span>System Operations</span><h1>Trung tâm vận hành nền tảng</h1><p>Điều hướng các capability quản trị thật theo domain. Các module chưa có backend được đánh dấu Planned trong navigation và không tạo dữ liệu giả.</p></div><ShieldCheck /></header>
    <aside className="admin-feedback">Internal console · Mọi thao tác được xác thực bằng JWT và authorization tại service sở hữu dữ liệu.</aside>
    <div className="admin-operations-grid">
      <CommandSection title="Operations" description="Governance" items={operations} />
      <CommandSection title="System" description="Provider visibility" items={system} />
      <CommandSection title="Platform services" description="Catalog & communication" items={platform} wide />
    </div>
  </main>
}
