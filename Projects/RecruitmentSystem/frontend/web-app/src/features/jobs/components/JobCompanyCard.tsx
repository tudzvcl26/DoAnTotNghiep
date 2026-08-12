import { ArrowUpRight, Building2, CheckCircle2, Globe2, Mail, UsersRound } from 'lucide-react'
import { Link } from 'react-router-dom'
import type { Company } from '../../../types/models/company'

const clean = (value: string | null) => value ? value.replaceAll('_', ' ').toLowerCase().replace(/^./, (letter) => letter.toUpperCase()) : null

export function JobCompanyCard({ company, companyId, isLoading }: { company?: Company; companyId: string; isLoading: boolean }) {
  if (isLoading) return <div className="job-company-card job-company-card--loading" aria-label="Đang tải doanh nghiệp"><span /><strong /><i /><i /></div>
  if (!company) return <aside className="job-company-card"><span className="job-company-card__eyebrow">Doanh nghiệp</span><h2>Thông tin đang cập nhật</h2><p>Job API chỉ cung cấp mã doanh nghiệp.</p><Link to={`/companies/${companyId}`}>Xem trang doanh nghiệp <ArrowUpRight /></Link></aside>
  return <aside className="job-company-card"><span className="job-company-card__eyebrow">Về doanh nghiệp</span><div className="job-company-card__head"><div>{company.logoUrl ? <img src={company.logoUrl} alt={`Logo ${company.name}`} /> : company.name.slice(0, 2).toUpperCase()}</div><h2>{company.name}</h2>{company.verificationStatus === 'VERIFIED' && <CheckCircle2 aria-label="Đã xác minh" />}</div>{company.description && <p>{company.description}</p>}<ul>{company.companyType && <li><Building2 /> {clean(company.companyType)}</li>}{company.companySize && <li><UsersRound /> {clean(company.companySize)}</li>}{company.website && <li><Globe2 /> Có website doanh nghiệp</li>}{company.email && <li><Mail /> {company.email}</li>}</ul><Link to={`/companies/${company.id}`}>Xem trang doanh nghiệp <ArrowUpRight /></Link></aside>
}
