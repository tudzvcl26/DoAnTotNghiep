import { ArrowUpRight, CalendarClock, CircleDollarSign, Clock3, RadioTower } from 'lucide-react'
import { Link } from 'react-router-dom'
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

export function JobCard({ job }: { job: JobSummary }) {
  return (
    <Link className="jobs-card" to={`/jobs/${job.id}`}>
      <div className="jobs-card__brand"><span>{(job.categoryName ?? job.title).slice(0, 2).toUpperCase()}</span><div><small>{job.jobCode}</small><strong>{job.categoryName ?? 'Việc làm đang tuyển'}</strong></div><ArrowUpRight aria-hidden="true" /></div>
      <div className="jobs-card__title"><h2>{job.title}</h2><p>Mã doanh nghiệp: {job.companyId.slice(0, 8)}</p></div>
      <div className="jobs-card__meta"><span><CircleDollarSign size={17} /> {formatSalary(job)}</span><span><RadioTower size={17} /> {job.remoteAllowed ? 'Hỗ trợ làm việc từ xa' : 'Làm việc tại văn phòng'}</span>{job.applicationDeadline && <span><CalendarClock size={17} /> Hạn nộp {new Intl.DateTimeFormat('vi-VN').format(new Date(job.applicationDeadline))}</span>}</div>
      <div className="jobs-card__bottom"><div>{job.employmentType && <span>{job.employmentType.replaceAll('_', ' ')}</span>}{job.experienceLevel && <span>{job.experienceLevel.replaceAll('_', ' ')}</span>}{job.quantity != null && <span>{job.quantity} vị trí</span>}</div><time dateTime={job.publishedAt ?? undefined}><Clock3 size={15} /> {formatPublished(job.publishedAt)}</time></div>
    </Link>
  )
}
