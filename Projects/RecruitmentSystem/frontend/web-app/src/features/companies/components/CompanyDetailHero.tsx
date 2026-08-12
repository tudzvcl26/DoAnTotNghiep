import { BadgeCheck, Building2, ExternalLink, Users } from 'lucide-react'
import { Link } from 'react-router-dom'
import type { Company } from '../../../types/models/company'

function displayEnum(value: string) { return value.toLowerCase().replaceAll('_', ' ') }
function websiteHref(website: string) { return /^https?:\/\//i.test(website) ? website : `https://${website}` }

export function CompanyDetailHero({ company }: { company: Company }) {
  const initial = company.name.trim().charAt(0).toUpperCase() || 'C'
  return <section className="company-detail-hero">
    {company.bannerUrl && <img className="company-detail-hero__banner" src={company.bannerUrl} alt="" aria-hidden="true" />}
    <div className="container company-detail-hero__inner">
      <nav className="company-breadcrumb" aria-label="Breadcrumb"><Link to="/companies">Doanh nghiệp</Link><span>/</span><span>{company.name}</span></nav>
      <div className="company-hero-card">
        <div className="company-hero-card__brand">{company.logoUrl ? <img src={company.logoUrl} alt={`Logo ${company.name}`} /> : <span aria-hidden="true">{initial}</span>}</div>
        <div className="company-hero-card__content"><div className="company-hero-card__label"><Building2 size={16} /> Hồ sơ doanh nghiệp</div><h1>{company.name}</h1>{company.verificationStatus === 'VERIFIED' && <span className="company-verified"><BadgeCheck size={17} /> Doanh nghiệp đã xác minh</span>}
          <div className="company-hero-meta">{company.companyType && <span><Building2 /><small>Loại hình</small><strong>{displayEnum(company.companyType)}</strong></span>}{company.companySize && <span><Users /><small>Quy mô</small><strong>{displayEnum(company.companySize)}</strong></span>}{company.website && <a href={websiteHref(company.website)} target="_blank" rel="noreferrer"><ExternalLink /><small>Website</small><strong>{company.website}</strong></a>}</div>
        </div>
      </div>
    </div>
  </section>
}
