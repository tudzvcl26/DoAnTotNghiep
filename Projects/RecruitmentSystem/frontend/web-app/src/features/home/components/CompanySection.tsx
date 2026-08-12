import { useQuery } from '@tanstack/react-query'
import { Building2, CheckCircle2, RotateCw, UsersRound } from 'lucide-react'
import { Link } from 'react-router-dom'
import { getFeaturedCompanies } from '../../companies/companies.api'
import { SectionHeading } from './SectionHeading'

const label = (value: string | null) => value ? value.replaceAll('_', ' ').toLowerCase().replace(/^./, (char) => char.toUpperCase()) : 'Đang cập nhật'

export function CompanySection() {
  const companies = useQuery({ queryKey: ['home', 'featured-companies'], queryFn: getFeaturedCompanies })
  return (
    <section className="home-section company-section">
      <div className="container">
        <SectionHeading eyebrow="Nơi làm việc đáng tin cậy" title="Khám phá doanh nghiệp" description="Thông tin doanh nghiệp công khai được đồng bộ trực tiếp từ hệ thống." to="/companies" />
        {companies.isPending && <div className="company-grid">{Array.from({ length: 4 }, (_, index) => <div className="home-skeleton home-skeleton--company" key={index} />)}</div>}
        {companies.isError && <div className="home-state"><Building2 /><h3>Chưa thể tải doanh nghiệp</h3><p>Danh sách doanh nghiệp hiện chưa phản hồi.</p><button type="button" onClick={() => companies.refetch()}><RotateCw size={16} /> Thử lại</button></div>}
        {companies.data?.content.length === 0 && <div className="home-state"><Building2 /><h3>Chưa có doanh nghiệp công khai</h3><p>Thông tin sẽ xuất hiện khi hồ sơ doanh nghiệp được kích hoạt.</p></div>}
        {companies.data && companies.data.content.length > 0 && <div className="company-grid">{companies.data.content.slice(0, 4).map((company) => <Link className="company-card" to={`/companies/${company.id}`} key={company.id}><div className="company-card__brand">{company.logoUrl ? <img src={company.logoUrl} alt={`Logo ${company.name}`} /> : <span>{company.name.slice(0, 2).toUpperCase()}</span>}</div><h3>{company.name}</h3><p>{label(company.companyType)}</p><div><span><UsersRound size={15} /> {label(company.companySize)}</span>{company.verificationStatus === 'VERIFIED' && <span className="company-card__verified"><CheckCircle2 size={15} /> Đã xác minh</span>}</div></Link>)}</div>}
      </div>
    </section>
  )
}
