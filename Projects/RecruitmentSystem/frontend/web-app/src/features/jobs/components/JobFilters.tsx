import { Filter, MapPin, RotateCcw, SearchCheck, SlidersHorizontal } from 'lucide-react'
import { useEffect, useState } from 'react'
import {
  EMPLOYMENT_TYPES, EXPERIENCE_LEVELS, type EmploymentType, type ExperienceLevel, type JobCategory,
} from '../../../types/models/job'

export type JobFilterValues = {
  location: string
  categoryId: string
  employmentType: '' | EmploymentType
  experienceLevel: '' | ExperienceLevel
  remoteAllowed: '' | 'true' | 'false'
  minSalary: string
  maxSalary: string
}

const employmentLabels: Record<EmploymentType, string> = {
  FULL_TIME: 'Toàn thời gian', PART_TIME: 'Bán thời gian', INTERNSHIP: 'Thực tập',
  FREELANCE: 'Freelance', CONTRACT: 'Hợp đồng', TEMPORARY: 'Thời vụ',
}
const experienceLabels: Record<ExperienceLevel, string> = {
  NO_EXPERIENCE: 'Không yêu cầu kinh nghiệm', FRESHER: 'Fresher', JUNIOR: 'Junior',
  MIDDLE: 'Middle', SENIOR: 'Senior', LEADER: 'Trưởng nhóm', MANAGER: 'Quản lý',
}

export function JobFilters({
  keyword, filters, categories, categoriesLoading, size, onApply, onClear, onSizeChange,
}: {
  keyword: string
  filters: JobFilterValues
  categories: JobCategory[]
  categoriesLoading: boolean
  size: number
  onApply: (filters: JobFilterValues) => void
  onClear: () => void
  onSizeChange: (size: number) => void
}) {
  const [draft, setDraft] = useState(filters)
  useEffect(() => setDraft(filters), [filters])

  const set = <K extends keyof JobFilterValues>(key: K, value: JobFilterValues[K]) => setDraft((current) => ({ ...current, [key]: value }))
  const min = draft.minSalary === '' ? null : Number(draft.minSalary)
  const max = draft.maxSalary === '' ? null : Number(draft.maxSalary)
  const salaryInvalid = min != null && max != null && min > max
  const hasFilters = Boolean(keyword || Object.values(filters).some(Boolean))

  return (
    <aside className="jobs-filter" aria-label="Bộ lọc việc làm">
      <div className="jobs-filter__heading"><span><Filter size={18} /> Bộ lọc</span>{hasFilters && <button type="button" onClick={onClear}><RotateCcw size={14} /> Xóa tất cả</button>}</div>
      {keyword && <div className="jobs-filter__group"><h2>Từ khóa hiện tại</h2><div className="jobs-filter__keyword"><SearchCheck size={16} /><span>{keyword}</span></div></div>}
      <div className="jobs-filter__group jobs-filter__fields">
        <label htmlFor="jobs-location"><span><MapPin size={15} /> Địa điểm</span><input id="jobs-location" maxLength={100} value={draft.location} onChange={(event) => set('location', event.target.value)} placeholder="Tỉnh, quận hoặc địa chỉ" /></label>
        <label htmlFor="jobs-category"><span>Ngành nghề</span><select id="jobs-category" value={draft.categoryId} disabled={categoriesLoading} onChange={(event) => set('categoryId', event.target.value)}><option value="">Tất cả ngành nghề</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label>
        <label htmlFor="jobs-employment"><span>Hình thức làm việc</span><select id="jobs-employment" value={draft.employmentType} onChange={(event) => set('employmentType', event.target.value as JobFilterValues['employmentType'])}><option value="">Tất cả hình thức</option>{EMPLOYMENT_TYPES.map((type) => <option key={type} value={type}>{employmentLabels[type]}</option>)}</select></label>
        <label htmlFor="jobs-experience"><span>Kinh nghiệm</span><select id="jobs-experience" value={draft.experienceLevel} onChange={(event) => set('experienceLevel', event.target.value as JobFilterValues['experienceLevel'])}><option value="">Tất cả cấp độ</option>{EXPERIENCE_LEVELS.map((level) => <option key={level} value={level}>{experienceLabels[level]}</option>)}</select></label>
        <label htmlFor="jobs-remote"><span>Hình thức địa điểm</span><select id="jobs-remote" value={draft.remoteAllowed} onChange={(event) => set('remoteAllowed', event.target.value as JobFilterValues['remoteAllowed'])}><option value="">Tất cả</option><option value="true">Có hỗ trợ remote</option><option value="false">Làm việc tại văn phòng</option></select></label>
        <fieldset><legend>Khoảng lương (VND)</legend><div className="jobs-filter__salary"><label htmlFor="jobs-min-salary"><span>Từ</span><input id="jobs-min-salary" type="number" min="0" step="1000000" value={draft.minSalary} onChange={(event) => set('minSalary', event.target.value)} placeholder="10.000.000" /></label><label htmlFor="jobs-max-salary"><span>Đến</span><input id="jobs-max-salary" type="number" min="0" step="1000000" value={draft.maxSalary} onChange={(event) => set('maxSalary', event.target.value)} placeholder="40.000.000" /></label></div>{salaryInvalid && <small role="alert">Mức lương từ không thể lớn hơn mức lương đến.</small>}</fieldset>
        <button className="jobs-filter__apply" type="button" disabled={salaryInvalid} onClick={() => onApply(draft)}><SlidersHorizontal size={16} /> Áp dụng bộ lọc</button>
      </div>
      <div className="jobs-filter__group"><label htmlFor="jobs-page-size"><span>Số kết quả mỗi trang</span><select id="jobs-page-size" value={size} onChange={(event) => onSizeChange(Number(event.target.value))}><option value="6">6 việc làm</option><option value="12">12 việc làm</option><option value="24">24 việc làm</option></select></label></div>
      <div className="jobs-filter__notice"><strong>Dữ liệu từ backend</strong><p>Kết quả được lọc trực tiếp qua Recruitment Service; không dùng dữ liệu mẫu hoặc bộ lọc phía trình duyệt.</p></div>
    </aside>
  )
}
