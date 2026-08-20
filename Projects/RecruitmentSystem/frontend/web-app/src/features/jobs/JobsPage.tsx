import { useQueries, useQuery } from '@tanstack/react-query'
import { Filter, SlidersHorizontal, X } from 'lucide-react'
import { useEffect, useMemo, useRef, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { getCompanyById } from '../companies/companies.api'
import { EMPLOYMENT_TYPES, EXPERIENCE_LEVELS, type EmploymentType, type ExperienceLevel } from '../../types/models/job'
import { getJobs, getPublicJobCategories, type JobsQueryParams } from './jobs.api'
import { JobCard } from './components/JobCard'
import { JobFilters, type JobFilterValues } from './components/JobFilters'
import { JobPagination } from './components/JobPagination'
import { JobSearchHeader } from './components/JobSearchHeader'
import { JobEmptyState, JobErrorState, JobResultsSkeleton } from './components/JobStates'
import './jobs-page.css'

const allowedSizes = new Set([6, 12, 24])
const allowedSorts = new Set<JobsQueryParams['sort']>(['publishedAt,desc', 'publishedAt,asc'])
const filterKeys = ['keyword', 'location', 'categoryId', 'companyId', 'skillId', 'employmentType', 'experienceLevel', 'remoteAllowed', 'minSalary', 'maxSalary']

function enumValue<T extends string>(value: string | null, allowed: readonly T[]): T | undefined {
  return value && allowed.includes(value as T) ? value as T : undefined
}

function numberValue(value: string): number | undefined {
  if (value === '') return undefined
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : undefined
}

export function JobsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [filterOpen, setFilterOpen] = useState(false)
  const filterTriggerRef = useRef<HTMLButtonElement>(null)
  const filterCloseRef = useRef<HTMLButtonElement>(null)
  const pageValue = Number(searchParams.get('page') ?? 0)
  const sizeValue = Number(searchParams.get('size') ?? 12)
  const sortValue = searchParams.get('sort') as JobsQueryParams['sort'] | null
  const employmentType = enumValue<EmploymentType>(searchParams.get('employmentType'), EMPLOYMENT_TYPES)
  const experienceLevel = enumValue<ExperienceLevel>(searchParams.get('experienceLevel'), EXPERIENCE_LEVELS)
  const remoteValue = searchParams.get('remoteAllowed')
  const minSalary = searchParams.get('minSalary') ?? ''
  const maxSalary = searchParams.get('maxSalary') ?? ''

  const filters = useMemo<JobFilterValues>(() => ({
    location: searchParams.get('location')?.trim() ?? '',
    categoryId: searchParams.get('categoryId') ?? '',
    employmentType: employmentType ?? '',
    experienceLevel: experienceLevel ?? '',
    remoteAllowed: remoteValue === 'true' || remoteValue === 'false' ? remoteValue : '',
    minSalary: numberValue(minSalary) == null ? '' : minSalary,
    maxSalary: numberValue(maxSalary) == null ? '' : maxSalary,
  }), [searchParams, employmentType, experienceLevel, remoteValue, minSalary, maxSalary])

  const params: JobsQueryParams = {
    keyword: searchParams.get('keyword')?.trim() ?? '',
    location: filters.location || undefined,
    categoryId: filters.categoryId || undefined,
    companyId: searchParams.get('companyId') || undefined,
    skillId: searchParams.get('skillId') || undefined,
    employmentType: filters.employmentType || undefined,
    experienceLevel: filters.experienceLevel || undefined,
    remoteAllowed: filters.remoteAllowed === '' ? undefined : filters.remoteAllowed === 'true',
    minSalary: numberValue(filters.minSalary),
    maxSalary: numberValue(filters.maxSalary),
    page: Number.isInteger(pageValue) && pageValue >= 0 ? pageValue : 0,
    size: allowedSizes.has(sizeValue) ? sizeValue : 12,
    sort: sortValue && allowedSorts.has(sortValue) ? sortValue : 'publishedAt,desc',
  }

  const jobs = useQuery({ queryKey: ['jobs', params], queryFn: () => getJobs(params), placeholderData: (previous) => previous })
  const categories = useQuery({ queryKey: ['job-categories', 'public'], queryFn: getPublicJobCategories, staleTime: 5 * 60_000 })
  const companyIds = useMemo(() => [...new Set(jobs.data?.content.map((job) => job.companyId) ?? [])], [jobs.data?.content])
  const companyQueries = useQueries({ queries: companyIds.map((companyId) => ({ queryKey: ['company', companyId], queryFn: () => getCompanyById(companyId), staleTime: 5 * 60_000 })) })
  const companies = new Map(companyQueries.flatMap((query, index) => query.data ? [[companyIds[index], query.data] as const] : []))

  useEffect(() => {
    if (!filterOpen) return
    const trigger = filterTriggerRef.current
    filterCloseRef.current?.focus()
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setFilterOpen(false)
    }
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('keydown', closeOnEscape)
      trigger?.focus()
    }
  }, [filterOpen])

  const updateParams = (updates: Record<string, string | number | boolean | undefined>, resetPage = true) => {
    const next = new URLSearchParams(searchParams)
    Object.entries(updates).forEach(([key, value]) => {
      if (value === '' || value == null) next.delete(key)
      else next.set(key, String(value))
    })
    if (resetPage && !('page' in updates)) next.set('page', '0')
    setSearchParams(next)
  }

  const applyFilters = (nextFilters: JobFilterValues) => {
    updateParams(nextFilters)
    setFilterOpen(false)
  }
  const clearFilters = () => {
    const next = new URLSearchParams(searchParams)
    filterKeys.forEach((key) => next.delete(key))
    next.set('page', '0')
    setSearchParams(next)
  }

  return <>
    <JobSearchHeader keyword={params.keyword} location={filters.location} onSearch={(keyword, location) => updateParams({ keyword, location, page: 0 }, false)} />
    <section className="jobs-page-section">
      <div className="container">
        <div className="jobs-mobile-tools"><button ref={filterTriggerRef} type="button" onClick={() => setFilterOpen(true)} aria-expanded={filterOpen} aria-controls="jobs-filter-drawer"><Filter size={17} /> Bộ lọc</button><label><SlidersHorizontal size={17} /><span className="sr-only">Sắp xếp</span><select value={params.sort} onChange={(event) => updateParams({ sort: event.target.value, page: 0 }, false)}><option value="publishedAt,desc">Mới nhất</option><option value="publishedAt,asc">Cũ nhất</option></select></label></div>
        <div className="jobs-layout">
          <button className={`jobs-filter-overlay${filterOpen ? ' is-open' : ''}`} type="button" aria-label="Đóng bộ lọc" onClick={() => setFilterOpen(false)} />
          <div id="jobs-filter-drawer" className={`jobs-filter-wrap${filterOpen ? ' is-open' : ''}`} role={filterOpen ? 'dialog' : undefined} aria-modal={filterOpen || undefined} aria-label={filterOpen ? 'Bộ lọc việc làm' : undefined}><div className="jobs-filter-mobile-head"><strong>Bộ lọc việc làm</strong><button ref={filterCloseRef} type="button" onClick={() => setFilterOpen(false)} aria-label="Đóng bộ lọc"><X /></button></div><JobFilters keyword={params.keyword} filters={filters} categories={categories.data ?? []} categoriesLoading={categories.isPending} size={params.size} onApply={applyFilters} onClear={clearFilters} onSizeChange={(size) => updateParams({ size, page: 0 }, false)} /></div>
          <main className="jobs-results">
            <div className="jobs-results__header"><div><span>Kết quả tìm kiếm</span><h2>{jobs.data ? `${jobs.data.totalElements} việc làm phù hợp` : 'Việc làm dành cho bạn'}</h2></div><label>Sắp xếp<select value={params.sort} onChange={(event) => updateParams({ sort: event.target.value, page: 0 }, false)}><option value="publishedAt,desc">Mới nhất</option><option value="publishedAt,asc">Cũ nhất</option></select></label></div>
            {jobs.isPending && <JobResultsSkeleton />}
            {jobs.isError && <JobErrorState onRetry={() => jobs.refetch()} />}
            {jobs.data && jobs.data.content.length === 0 && <JobEmptyState keyword={params.keyword || filters.location} onClear={clearFilters} />}
            {jobs.data && jobs.data.content.length > 0 && <div className={`jobs-results-list${jobs.isFetching ? ' is-refreshing' : ''}`}>{jobs.data.content.map((job) => <JobCard job={job} company={companies.get(job.companyId)} key={job.id} />)}</div>}
            {jobs.data && <JobPagination page={jobs.data.page} totalPages={jobs.data.totalPages} hasPrevious={jobs.data.hasPrevious} hasNext={jobs.data.hasNext} onPageChange={(page) => updateParams({ page }, false)} />}
          </main>
        </div>
      </div>
    </section>
  </>
}
