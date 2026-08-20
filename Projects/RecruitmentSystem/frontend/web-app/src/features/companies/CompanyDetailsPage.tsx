import { useQuery } from '@tanstack/react-query'
import { ArrowRight, BriefcaseBusiness, RefreshCw } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useParams } from 'react-router-dom'
import { normalizeApiError } from '../../lib/api/error-adapter'
import { getJobs } from '../jobs/jobs.api'
import { JobCard } from '../jobs/components/JobCard'
import { getCompanyById } from './companies.api'
import { CompanyDetailContent } from './components/CompanyDetailContent'
import { CompanyDetailHero } from './components/CompanyDetailHero'
import { CompanyDetailError, CompanyDetailSkeleton } from './components/CompanyStates'
import './companies-page.css'
import '../jobs/jobs-page.css'

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

export function CompanyDetailsPage() {
  const { companyId } = useParams()
  const validCompanyId = Boolean(companyId && UUID_PATTERN.test(companyId))
  const company = useQuery({
    queryKey: ['company', companyId],
    queryFn: () => getCompanyById(companyId!),
    enabled: validCompanyId,
    retry: (count, error) => normalizeApiError(error).status !== 404 && count < 1,
  })
  const jobs = useQuery({
    queryKey: ['jobs', 'company', companyId],
    queryFn: () => getJobs({ keyword: '', companyId, page: 0, size: 6, sort: 'publishedAt,desc' }),
    enabled: validCompanyId && Boolean(company.data),
  })

  if (!validCompanyId) return <CompanyDetailError notFound onRetry={() => undefined} />
  if (company.isPending) return <CompanyDetailSkeleton />
  if (company.isError) return <CompanyDetailError notFound={normalizeApiError(company.error).status === 404} onRetry={() => company.refetch()} />

  return <>
    <CompanyDetailHero company={company.data} />
    <section className="company-detail-page"><div className="container"><CompanyDetailContent company={company.data} />
      <section className="company-open-jobs" aria-labelledby="company-open-jobs-title">
        <div className="company-open-jobs__heading"><div><span><BriefcaseBusiness size={16} /> Cơ hội đang mở</span><h2 id="company-open-jobs-title">Việc làm tại {company.data.name}</h2></div>{jobs.data && jobs.data.totalElements > jobs.data.content.length && <Link to={`/jobs?companyId=${company.data.id}`}>Xem tất cả <ArrowRight size={16} /></Link>}</div>
        {jobs.isPending && <div className="company-open-jobs__loading" aria-label="Đang tải việc làm"><span /><span /></div>}
        {jobs.isError && <div className="company-open-jobs__state" role="alert"><p>Chưa thể tải việc làm của doanh nghiệp.</p><button type="button" onClick={() => void jobs.refetch()}><RefreshCw size={15} /> Thử lại</button></div>}
        {jobs.data?.content.length === 0 && <div className="company-open-jobs__state"><p>Doanh nghiệp hiện chưa có việc làm công khai.</p><Link to="/jobs">Khám phá việc làm khác</Link></div>}
        {jobs.data && jobs.data.content.length > 0 && <div className="company-open-jobs__list">{jobs.data.content.map((job) => <JobCard key={job.id} job={job} company={company.data} />)}</div>}
      </section>
    </div></section>
  </>
}
