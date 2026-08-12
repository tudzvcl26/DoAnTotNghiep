import { BriefcaseBusiness, CalendarDays, CheckCircle2, Clock3, RadioTower, UsersRound, WalletCards } from 'lucide-react'
import { Link } from 'react-router-dom'
import type { Company } from '../../../types/models/company'
import type { JobDetail } from '../../../types/models/job'

const money = new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 0 })
const date = new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })

function salary(job: JobDetail) {
  if (job.salaryMin == null && job.salaryMax == null) return 'Thỏa thuận'
  const currency = job.currency === 'VND' ? '₫' : (job.currency ?? '')
  if (job.salaryMin != null && job.salaryMax != null) return `${money.format(job.salaryMin)} – ${money.format(job.salaryMax)} ${currency}`
  return `Từ ${money.format(job.salaryMin ?? job.salaryMax ?? 0)} ${currency}`
}

export function JobDetailHero({ job, company }: { job: JobDetail; company?: Company }) {
  return (
    <section className="job-detail-hero">
      <div className="container">
        <nav className="job-breadcrumb" aria-label="Breadcrumb"><Link to="/jobs">Việc làm</Link><span>/</span><span aria-current="page">{job.title}</span></nav>
        <article className="job-hero-card">
          <div className="job-hero-card__brand">{company?.logoUrl ? <img src={company.logoUrl} alt={`Logo ${company.name}`} /> : <span>{(company?.name ?? job.categoryName ?? job.title).slice(0, 2).toUpperCase()}</span>}</div>
          <div className="job-hero-card__content">
            <div className="job-hero-card__label"><BriefcaseBusiness size={16} /> {job.categoryName ?? 'Cơ hội nghề nghiệp'}</div>
            <h1>{job.title}</h1>
            <p>{company ? <Link to={`/companies/${company.id}`}>{company.name}</Link> : `Mã doanh nghiệp: ${job.companyId.slice(0, 8)}`}{company?.verificationStatus === 'VERIFIED' && <CheckCircle2 aria-label="Doanh nghiệp đã xác minh" />}</p>
            <div className="job-hero-meta">
              <span><WalletCards /> <small>Mức lương</small><strong>{salary(job)}</strong></span>
              {job.employmentType && <span><Clock3 /> <small>Hình thức</small><strong>{job.employmentType.replaceAll('_', ' ')}</strong></span>}
              <span><RadioTower /> <small>Cách thức làm việc</small><strong>{job.remoteAllowed ? 'Có thể làm từ xa' : 'Tại văn phòng'}</strong></span>
              {job.quantity != null && <span><UsersRound /> <small>Số lượng</small><strong>{job.quantity} vị trí</strong></span>}
              {job.publishedAt && <span><CalendarDays /> <small>Ngày đăng</small><strong>{date.format(new Date(job.publishedAt))}</strong></span>}
            </div>
          </div>
        </article>
      </div>
    </section>
  )
}
