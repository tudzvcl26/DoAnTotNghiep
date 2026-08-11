import { Building } from 'lucide-react'
import { useParams } from 'react-router-dom'
import { PublicPageIntro } from '../../components/data-display/PublicPageIntro'

export function CompanyDetailsPage() {
  const { companyId } = useParams()
  return <PublicPageIntro eyebrow="Hồ sơ doanh nghiệp" title="Không gian thương hiệu tuyển dụng" description={`Route công ty ${companyId ?? ''} đã sẵn sàng để nhận dữ liệu thực.`} icon={Building} />
}
