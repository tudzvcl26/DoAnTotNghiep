import { BriefcaseBusiness } from 'lucide-react'
import { PublicPageIntro } from '../../components/data-display/PublicPageIntro'

export function JobsPage() {
  return <><PublicPageIntro eyebrow="Cơ hội nghề nghiệp" title="Tìm công việc dành cho bước tiến tiếp theo." description="Bộ lọc và dữ liệu việc làm thực tế sẽ được kết nối với API Gateway trong Phase 2." icon={BriefcaseBusiness} /><section className="page-section"><div className="container page-panel"><h2>Danh sách việc làm</h2><p className="page-description">Không sử dụng dữ liệu giả. Khu vực này đã sẵn sàng để tích hợp API công khai <strong>/api/v1/jobs</strong>.</p></div></section></>
}
