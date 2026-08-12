import { ArrowUpRight, BadgeCheck, Building2, Users } from 'lucide-react'
import { Link } from 'react-router-dom'
import type { Company } from '../../../types/models/company'

function displayEnum(value: string) {
  return value.toLowerCase().replaceAll('_', ' ')
}

export function CompanyCard({ company }: { company: Company }) {
  const initial = company.name.trim().charAt(0).toUpperCase() || 'C'
  return <article className="company-card">
    <div className="company-card__brand">{company.logoUrl ? <img src={company.logoUrl} alt={`Logo ${company.name}`} loading="lazy" /> : <span aria-hidden="true">{initial}</span>}</div>
    <div className="company-card__body">
      <div className="company-card__title"><h3>{company.name}</h3>{company.verificationStatus === 'VERIFIED' && <span title="Doanh nghiệp đã xác minh"><BadgeCheck size={17} /> Đã xác minh</span>}</div>
      {(company.companyType || company.companySize) && <ul className="company-card__meta">
        {company.companyType && <li><Building2 /> {displayEnum(company.companyType)}</li>}
        {company.companySize && <li><Users /> {displayEnum(company.companySize)}</li>}
      </ul>}
      {company.description && <p>{company.description}</p>}
    </div>
    <Link to={`/companies/${company.id}`} aria-label={`Xem công ty ${company.name}`}>Xem công ty <ArrowUpRight size={16} /></Link>
  </article>
}
