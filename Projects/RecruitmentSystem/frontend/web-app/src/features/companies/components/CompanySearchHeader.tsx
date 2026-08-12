import { Building2, Search, Sparkles } from 'lucide-react'
import { useEffect, useState, type FormEvent } from 'react'

export function CompanySearchHeader({ keyword, onSearch }: { keyword: string; onSearch: (keyword: string) => void }) {
  const [value, setValue] = useState(keyword)
  useEffect(() => setValue(keyword), [keyword])

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    onSearch(value.trim())
  }

  return <section className="companies-search-hero">
    <div className="container companies-search-hero__inner">
      <div className="companies-search-hero__copy"><span><Sparkles size={15} /> Không gian nghề nghiệp</span><h1>Khám phá doanh nghiệp</h1><p>Tìm hiểu về những nơi bạn có thể phát triển sự nghiệp.</p></div>
      <form className="companies-search-form" role="search" onSubmit={submit}><Search aria-hidden="true" /><label className="sr-only" htmlFor="company-keyword">Tìm kiếm công ty</label><input id="company-keyword" value={value} onChange={(event) => setValue(event.target.value)} placeholder="Tìm kiếm công ty..." autoComplete="off" /><button className="button button--primary" type="submit">Tìm kiếm</button></form>
      <div className="companies-search-hero__note"><Building2 size={16} /> Khám phá hồ sơ doanh nghiệp được công khai trên RecruitmentSystem</div>
    </div>
  </section>
}
