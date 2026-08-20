import { MapPin, Search, Sparkles } from 'lucide-react'
import { type FormEvent, useEffect, useState } from 'react'
import { Button } from '../../../components/ui/Button'

const popular = ['Java Developer', 'Frontend Developer', 'Backend Developer', 'Data Analyst', 'Marketing', 'Remote']

export function JobSearchHeader({ keyword, location, onSearch }: { keyword: string; location: string; onSearch: (keyword: string, location: string) => void }) {
  const [value, setValue] = useState(keyword)
  const [locationValue, setLocationValue] = useState(location)
  useEffect(() => setValue(keyword), [keyword])
  useEffect(() => setLocationValue(location), [location])

  const submit = (event: FormEvent) => {
    event.preventDefault()
    onSearch(value.trim(), locationValue.trim())
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
          <span className="jobs-search-form__divider" aria-hidden="true" />
          <MapPin size={21} aria-hidden="true" />
          <label className="sr-only" htmlFor="jobs-hero-location">Tỉnh, quận hoặc địa chỉ</label>
          <input id="jobs-hero-location" maxLength={100} value={locationValue} onChange={(event) => setLocationValue(event.target.value)} placeholder="Địa điểm" />
          <Button type="submit" size="lg">Tìm việc</Button>
        </form>
        <div className="jobs-quick-search"><span>Tìm kiếm phổ biến:</span><div>{popular.map((term) => <button type="button" key={term} onClick={() => onSearch(term, location)}>{term}</button>)}</div></div>
      </div>
    </section>
  )
}
