import { useQuery } from '@tanstack/react-query'
import { BriefcaseBusiness, CalendarClock, MapPin, RotateCw, WalletCards } from 'lucide-react'
import { Link } from 'react-router-dom'
import { getFeaturedCompanies } from '../../companies/companies.api'
import { getFeaturedJobs } from '../../jobs/jobs.api'
import type { JobSummary } from '../../../types/models/job'
import { SectionHeading } from './SectionHeading'

const money = new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 0 })

function salary(job: JobSummary) {
  if (job.salaryMin == null && job.salaryMax == null) return 'Thỏa thuận'
  const unit = job.currency === 'VND' ? '₫' : (job.currency ?? '')
  if (job.salaryMin != null && job.salaryMax != null) return `${money.format(job.salaryMin)} – ${money.format(job.salaryMax)} ${unit}`
  return `Từ ${money.format(job.salaryMin ?? job.salaryMax ?? 0)} ${unit}`
}

function published(value: string | null) {
  if (!value) return 'Mới cập nhật'
  const days = Math.max(0, Math.floor((Date.now() - new Date(value).getTime()) / 86_400_000))
  return days === 0 ? 'Hôm nay' : `${days} ngày trước`
}

export function FeaturedJobsSection() {
  const jobs = useQuery({ queryKey: ['home', 'featured-jobs'], queryFn: getFeaturedJobs })
  const companies = useQuery({ queryKey: ['home', 'featured-companies'], queryFn: getFeaturedCompanies })
  const companyNames = new Map(companies.data?.content.map((company) => [company.id, company.name]))

  return (
    <section className="home-section featured-jobs-section">
      <div className="container">
        <SectionHeading eyebrow="Cơ hội mới nhất" title="Việc làm nổi bật" description="Những vị trí đang mở được lấy trực tiếp từ hệ thống tuyển dụng." to="/jobs" />
        {jobs.isPending && <div className="job-grid" aria-label="Đang tải việc làm">{Array.from({ length: 6 }, (_, index) => <div className="home-skeleton home-skeleton--job" key={index} />)}</div>}
        {jobs.isError && <div className="home-state"><BriefcaseBusiness /><h3>Chưa thể tải việc làm</h3><p>Hệ thống dữ liệu có thể đang tạm dừng. Bạn vẫn có thể mở trang tìm việc để thử lại.</p><button type="button" onClick={() => jobs.refetch()}><RotateCw size={16} /> Thử lại</button></div>}
        {jobs.data && jobs.data.content.length === 0 && <div className="home-state"><BriefcaseBusiness /><h3>Chưa có vị trí đang mở</h3><p>Các cơ hội mới sẽ xuất hiện tại đây khi nhà tuyển dụng đăng tin.</p></div>}
        {jobs.data && jobs.data.content.length > 0 && (
          <div className="job-grid">
            {jobs.data.content.map((job) => (
              <Link className="job-card" to={`/jobs/${job.id}`} key={job.id}>
                <div className="job-card__top"><span className="job-card__logo">{(companyNames.get(job.companyId) ?? job.title).slice(0, 2).toUpperCase()}</span><span className="job-card__fresh">Mới</span></div>
                <div><h3>{job.title}</h3><p className="job-card__company">{companyNames.get(job.companyId) ?? 'Thông tin doanh nghiệp'}</p></div>
                <div className="job-card__meta"><span><WalletCards size={16} /> {salary(job)}</span><span><MapPin size={16} /> {job.remoteAllowed ? 'Có thể làm từ xa' : 'Tại văn phòng'}</span></div>
                <div className="job-card__tags">{job.categoryName && <span>{job.categoryName}</span>}{job.employmentType && <span>{job.employmentType.replaceAll('_', ' ')}</span>}</div>
                <div className="job-card__footer"><span><CalendarClock size={15} /> {published(job.publishedAt)}</span><span>Xem chi tiết</span></div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </section>
  )
}
