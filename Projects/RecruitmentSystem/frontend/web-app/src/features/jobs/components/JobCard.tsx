import { ArrowUpRight, CalendarClock, CircleDollarSign, Clock3, MapPin } from 'lucide-react'
import { Link } from 'react-router-dom'
import type { Company } from '../../../types/models/company'
import type { JobSummary } from '../../../types/models/job'

const formatter = new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 0 })

function formatSalary(job: JobSummary) {
  if (job.salaryMin == null && job.salaryMax == null) return 'Thỏa thuận'
  const currency = job.currency === 'VND' ? '₫' : (job.currency ?? '')
  if (job.salaryMin != null && job.salaryMax != null) return `${formatter.format(job.salaryMin)} – ${formatter.format(job.salaryMax)} ${currency}`
  return `Từ ${formatter.format(job.salaryMin ?? job.salaryMax ?? 0)} ${currency}`
}

function formatPublished(value: string | null) {
  if (!value) return 'Mới cập nhật'
  const days = Math.max(0, Math.floor((Date.now() - new Date(value).getTime()) / 86_400_000))
  return days === 0 ? 'Hôm nay' : `${days} ngày trước`
}

export function JobCard({ job, company }: { job: JobSummary; company?: Company }) {
  const brandName = company?.name ?? 'Doanh nghiệp đang tuyển'
  const initial = brandName.trim().charAt(0).toUpperCase() || 'R'
  return (
    <Link className="jobs-card" to={`/jobs/${job.id}`} aria-label={`${job.title} tại ${brandName}`}>
      <div className="jobs-card__brand"><span>{company?.logoUrl ? <img src={company.logoUrl} alt="" /> : initial}</span><div><small>{brandName}</small><strong>{job.categoryName ?? 'Việc làm đang tuyển'}</strong></div><ArrowUpRight aria-hidden="true" /></div>
      <div className="jobs-card__title"><h2>{job.title}</h2><p>Mã tuyển dụng: {job.jobCode}</p></div>
      <div className="jobs-card__meta"><span><CircleDollarSign size={17} /> {formatSalary(job)}</span><span><MapPin size={17} /> {job.location ?? (job.remoteAllowed ? 'Có hỗ trợ làm việc từ xa' : 'Địa điểm chưa cập nhật')}</span>{job.applicationDeadline && <span><CalendarClock size={17} /> Hạn nộp {new Intl.DateTimeFormat('vi-VN').format(new Date(job.applicationDeadline))}</span>}</div>
      <div className="jobs-card__bottom"><div>{job.employmentType && <span>{job.employmentType.replaceAll('_', ' ')}</span>}{job.experienceLevel && <span>{job.experienceLevel.replaceAll('_', ' ')}</span>}{job.quantity != null && <span>{job.quantity} vị trí</span>}</div><time dateTime={job.publishedAt ?? undefined}><Clock3 size={15} /> {formatPublished(job.publishedAt)}</time></div>
    </Link>
  )
}
