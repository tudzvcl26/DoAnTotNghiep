import { ShieldCheck } from 'lucide-react'
import { PortalPlaceholder } from '../../components/feedback/PortalPlaceholder'

export function AdminDashboardPage() {
  return <PortalPlaceholder eyebrow="Admin workspace" title="Không gian quản trị đã sẵn sàng." description="Các module danh mục, thông báo và giám sát AI sẽ chỉ hiển thị theo API backend hiện có." icon={ShieldCheck} />
}
