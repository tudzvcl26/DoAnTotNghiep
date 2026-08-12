import { useQuery } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { normalizeApiError } from '../../lib/api/error-adapter'
import { getCompanyById } from './companies.api'
import { CompanyDetailContent } from './components/CompanyDetailContent'
import { CompanyDetailHero } from './components/CompanyDetailHero'
import { CompanyDetailError, CompanyDetailSkeleton } from './components/CompanyStates'
import './companies-page.css'

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

  if (!validCompanyId) return <CompanyDetailError notFound onRetry={() => undefined} />
  if (company.isPending) return <CompanyDetailSkeleton />
  if (company.isError) return <CompanyDetailError notFound={normalizeApiError(company.error).status === 404} onRetry={() => company.refetch()} />

  return <>
    <CompanyDetailHero company={company.data} />
    <section className="company-detail-page"><div className="container"><CompanyDetailContent company={company.data} /></div></section>
  </>
}
