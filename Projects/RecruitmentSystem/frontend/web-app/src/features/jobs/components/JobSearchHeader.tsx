import { Search, Sparkles } from 'lucide-react'
import { type FormEvent, useEffect, useState } from 'react'
import { Button } from '../../../components/ui/Button'

const popular = ['Java Developer', 'Frontend Developer', 'Backend Developer', 'Data Analyst', 'Marketing', 'Remote']

export function JobSearchHeader({ keyword, onSearch }: { keyword: string; onSearch: (keyword: string) => void }) {
  const [value, setValue] = useState(keyword)
  useEffect(() => setValue(keyword), [keyword])

  const submit = (event: FormEvent) => {
    event.preventDefault()
    onSearch(value.trim())
  }

  return (
    <section className="jobs-search-hero">
      <div className="container jobs-search-hero__inner">
        <span className="jobs-search-hero__eyebrow"><Sparkles size={15} /> Khám phá cơ hội nghề nghiệp</span>
        <h1>Tìm cơ hội phù hợp với bạn</h1>
        <p>Tìm kiếm trong danh sách việc làm đang được công khai trên RecruitmentSystem.</p>
        <form className="jobs-search-form" onSubmit={submit} role="search">
          <Search size={21} aria-hidden="true" />
          <label className="sr-only" htmlFor="jobs-keyword">Vị trí, kỹ năng hoặc tên công việc</label>
          <input id="jobs-keyword" value={value} onChange={(event) => setValue(event.target.value)} placeholder="Vị trí, kỹ năng hoặc tên công việc" />
          <Button type="submit" size="lg">Tìm việc</Button>
        </form>
        <div className="jobs-quick-search"><span>Tìm kiếm phổ biến:</span><div>{popular.map((term) => <button type="button" key={term} onClick={() => onSearch(term)}>{term}</button>)}</div></div>
      </div>
    </section>
  )
}
