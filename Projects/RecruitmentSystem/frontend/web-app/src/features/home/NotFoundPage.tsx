import { Compass } from 'lucide-react'
import { PortalPlaceholder } from '../../components/feedback/PortalPlaceholder'

export function NotFoundPage() {
  return <PortalPlaceholder eyebrow="404" title="Trang này chưa có trên bản đồ." description="Đường dẫn có thể chưa được triển khai trong foundation hiện tại hoặc đã thay đổi." icon={Compass} />
}
