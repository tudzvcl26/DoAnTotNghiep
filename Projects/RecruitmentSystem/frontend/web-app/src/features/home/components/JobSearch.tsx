import { MapPin, Search } from 'lucide-react'
import { type FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'

const popularSearches = ['Java Developer', 'Frontend Developer', 'Backend Developer', 'Data Analyst', 'Marketing', 'Remote']

export function JobSearch() {
  const [keyword, setKeyword] = useState('')
  const navigate = useNavigate()

  const submit = (event: FormEvent) => {
    event.preventDefault()
    const value = keyword.trim()
    navigate(value ? `/jobs?keyword=${encodeURIComponent(value)}` : '/jobs')
  }

  return (
    <div className="hero-search-wrap">
      <form className="hero-search" onSubmit={submit} role="search">
        <div className="hero-search__field">
          <Search size={20} aria-hidden="true" />
          <label className="sr-only" htmlFor="home-keyword">Vị trí, kỹ năng hoặc tên công việc</label>
          <input id="home-keyword" value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="Vị trí, kỹ năng hoặc tên công việc" />
        </div>
        <span className="hero-search__scope"><MapPin size={18} aria-hidden="true" /> Toàn quốc</span>
        <Button type="submit" size="lg">Tìm việc</Button>
      </form>
      <div className="quick-search" aria-label="Tìm kiếm phổ biến">
        <span>Tìm kiếm phổ biến:</span>
        <div>{popularSearches.map((term) => <Link key={term} to={`/jobs?keyword=${encodeURIComponent(term)}`}>{term}</Link>)}</div>
      </div>
    </div>
  )
}
