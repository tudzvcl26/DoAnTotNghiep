import { Building2 } from 'lucide-react'
import { PublicPageIntro } from '../../components/data-display/PublicPageIntro'

export function CompaniesPage() {
  return <><PublicPageIntro eyebrow="Doanh nghiệp" title="Khám phá nơi làm việc phù hợp với bạn." description="Danh sách doanh nghiệp sẽ lấy dữ liệu công khai từ API Gateway trong Phase 2." icon={Building2} /><section className="page-section"><div className="container page-panel"><h2>Danh sách công ty</h2><p className="page-description">Không sử dụng dữ liệu giả. Khu vực này đã sẵn sàng cho <strong>/api/v1/companies</strong>.</p></div></section></>
}
