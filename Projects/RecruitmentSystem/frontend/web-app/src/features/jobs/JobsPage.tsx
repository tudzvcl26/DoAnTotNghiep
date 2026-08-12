import { useQuery } from '@tanstack/react-query'
import { Filter, SlidersHorizontal, X } from 'lucide-react'
import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { getJobs, type JobsQueryParams } from './jobs.api'
import { JobCard } from './components/JobCard'
import { JobFilters } from './components/JobFilters'
import { JobPagination } from './components/JobPagination'
import { JobSearchHeader } from './components/JobSearchHeader'
import { JobEmptyState, JobErrorState, JobResultsSkeleton } from './components/JobStates'
import './jobs-page.css'

const allowedSizes = new Set([6, 12, 24])
const allowedSorts = new Set<JobsQueryParams['sort']>(['publishedAt,desc', 'publishedAt,asc'])

export function JobsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [filterOpen, setFilterOpen] = useState(false)
  const pageValue = Number(searchParams.get('page') ?? 0)
  const sizeValue = Number(searchParams.get('size') ?? 12)
  const sortValue = searchParams.get('sort') as JobsQueryParams['sort'] | null
  const params: JobsQueryParams = {
    keyword: searchParams.get('keyword')?.trim() ?? '',
    page: Number.isInteger(pageValue) && pageValue >= 0 ? pageValue : 0,
    size: allowedSizes.has(sizeValue) ? sizeValue : 12,
    sort: sortValue && allowedSorts.has(sortValue) ? sortValue : 'publishedAt,desc',
  }

  const jobs = useQuery({ queryKey: ['jobs', params], queryFn: () => getJobs(params), placeholderData: (previous) => previous })

  const updateParams = (updates: Partial<Record<keyof JobsQueryParams, string | number>>, resetPage = true) => {
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
    <JobSearchHeader keyword={params.keyword} onSearch={(keyword) => updateParams({ keyword, page: 0 }, false)} />
    <section className="jobs-page-section">
      <div className="container">
        <div className="jobs-mobile-tools"><button type="button" onClick={() => setFilterOpen(true)} aria-expanded={filterOpen}><Filter size={17} /> Bộ lọc</button><label><SlidersHorizontal size={17} /><span className="sr-only">Sắp xếp</span><select value={params.sort} onChange={(event) => updateParams({ sort: event.target.value, page: 0 }, false)}><option value="publishedAt,desc">Mới nhất</option><option value="publishedAt,asc">Cũ nhất</option></select></label></div>
        <div className="jobs-layout">
          <button className={`jobs-filter-overlay${filterOpen ? ' is-open' : ''}`} type="button" aria-label="Đóng bộ lọc" onClick={() => setFilterOpen(false)} />
          <div className={`jobs-filter-wrap${filterOpen ? ' is-open' : ''}`}><div className="jobs-filter-mobile-head"><strong>Bộ lọc việc làm</strong><button type="button" onClick={() => setFilterOpen(false)} aria-label="Đóng bộ lọc"><X /></button></div><JobFilters keyword={params.keyword} size={params.size} onClear={clearKeyword} onSizeChange={(size) => updateParams({ size, page: 0 }, false)} /></div>
          <main className="jobs-results">
            <div className="jobs-results__header"><div><span>Kết quả tìm kiếm</span><h2>{jobs.data ? `${jobs.data.totalElements} việc làm phù hợp` : 'Việc làm dành cho bạn'}</h2></div><label>Sắp xếp<select value={params.sort} onChange={(event) => updateParams({ sort: event.target.value, page: 0 }, false)}><option value="publishedAt,desc">Mới nhất</option><option value="publishedAt,asc">Cũ nhất</option></select></label></div>
            {jobs.isPending && <JobResultsSkeleton />}
            {jobs.isError && <JobErrorState onRetry={() => jobs.refetch()} />}
            {jobs.data && jobs.data.content.length === 0 && <JobEmptyState keyword={params.keyword} onClear={clearKeyword} />}
            {jobs.data && jobs.data.content.length > 0 && <div className={`jobs-results-list${jobs.isFetching ? ' is-refreshing' : ''}`}>{jobs.data.content.map((job) => <JobCard job={job} key={job.id} />)}</div>}
            {jobs.data && <JobPagination page={jobs.data.page} totalPages={jobs.data.totalPages} hasPrevious={jobs.data.hasPrevious} hasNext={jobs.data.hasNext} onPageChange={(page) => updateParams({ page }, false)} />}
          </main>
        </div>
      </div>
    </section>
  </>
}
