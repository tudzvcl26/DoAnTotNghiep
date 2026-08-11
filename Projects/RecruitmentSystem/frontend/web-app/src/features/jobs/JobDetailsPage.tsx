import { FileSearch } from 'lucide-react'
import { useParams } from 'react-router-dom'
import { PublicPageIntro } from '../../components/data-display/PublicPageIntro'

export function JobDetailsPage() {
  const { jobId } = useParams()
  return <PublicPageIntro eyebrow="Chi tiết việc làm" title="Thông tin cơ hội nghề nghiệp" description={`Route chi tiết đã sẵn sàng cho job ${jobId ?? ''}. Dữ liệu sẽ được kết nối trong Phase 2.`} icon={FileSearch} />
}
