import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { listCompanies, type CompaniesQueryParams } from './companies.api'
import { CompanyCard } from './components/CompanyCard'
import { CompanyPagination } from './components/CompanyPagination'
import { CompanySearchHeader } from './components/CompanySearchHeader'
import { CompanyEmptyState, CompanyErrorState, CompanyGridSkeleton } from './components/CompanyStates'
import './companies-page.css'

const allowedSizes = new Set([6, 12, 24])

export function CompaniesPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const pageValue = Number(searchParams.get('page') ?? 0)
  const sizeValue = Number(searchParams.get('size') ?? 12)
  const params: CompaniesQueryParams = {
    keyword: searchParams.get('keyword')?.trim() ?? '',
    page: Number.isInteger(pageValue) && pageValue >= 0 ? pageValue : 0,
    size: allowedSizes.has(sizeValue) ? sizeValue : 12,
  }

  const companies = useQuery({
    queryKey: ['companies', params],
    queryFn: () => listCompanies(params),
    placeholderData: (previous) => previous,
  })

  const updateParams = (updates: Partial<CompaniesQueryParams>, resetPage = true) => {
    const next = new URLSearchParams(searchParams)
    Object.entries(updates).forEach(([key, value]) => {
      if (value === '' || value == null) next.delete(key)
      else next.set(key, String(value))
    })
    if (resetPage && !('page' in updates)) next.set('page', '0')
    setSearchParams(next)
  }

  const clearKeyword = () => updateParams({ keyword: '', page: 0 }, false)

  return <>
    <CompanySearchHeader keyword={params.keyword} onSearch={(keyword) => updateParams({ keyword, page: 0 }, false)} />
    <section className="companies-page-section">
      <div className="container companies-results">
        <header className="companies-results__header">
          <div><span>Doanh nghiệp nổi bật</span><h2>{companies.data ? `${companies.data.totalElements} doanh nghiệp phù hợp` : 'Nơi làm việc dành cho bạn'}</h2></div>
          <label>Hiển thị<select value={params.size} onChange={(event) => updateParams({ size: Number(event.target.value), page: 0 }, false)} aria-label="Số doanh nghiệp trên mỗi trang"><option value={6}>6</option><option value={12}>12</option><option value={24}>24</option></select></label>
        </header>
        {companies.isPending && <CompanyGridSkeleton />}
        {companies.isError && <CompanyErrorState onRetry={() => companies.refetch()} />}
        {companies.data && companies.data.content.length === 0 && <CompanyEmptyState keyword={params.keyword} onClear={clearKeyword} />}
        {companies.data && companies.data.content.length > 0 && <div className={`companies-grid${companies.isFetching ? ' is-refreshing' : ''}`}>{companies.data.content.map((company) => <CompanyCard company={company} key={company.id} />)}</div>}
        {companies.data && <CompanyPagination page={companies.data.number} totalPages={companies.data.totalPages} first={companies.data.first} last={companies.data.last} onPageChange={(page) => updateParams({ page }, false)} />}
      </div>
    </section>
  </>
}
