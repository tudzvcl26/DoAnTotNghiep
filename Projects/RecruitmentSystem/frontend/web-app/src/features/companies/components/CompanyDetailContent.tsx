import { Building2, CalendarDays, ExternalLink, FileText, Mail, Phone, Users } from 'lucide-react'
import type { Company } from '../../../types/models/company'

function displayEnum(value: string) { return value.toLowerCase().replaceAll('_', ' ') }
function websiteHref(website: string) { return /^https?:\/\//i.test(website) ? website : `https://${website}` }
function formatDate(value: string) { return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'long' }).format(new Date(value)) }

export function CompanyDetailContent({ company }: { company: Company }) {
  return <div className="company-detail-layout">
    <main className="company-about-card"><h2><span><Building2 /></span> Giới thiệu doanh nghiệp</h2>{company.description ? <div className="company-rich-text">{company.description}</div> : <p className="company-muted">Doanh nghiệp chưa cập nhật nội dung giới thiệu.</p>}</main>
    <aside className="company-info-card"><h2>Thông tin doanh nghiệp</h2><dl>
      {company.companyType && <div><dt><Building2 /> Loại hình</dt><dd>{displayEnum(company.companyType)}</dd></div>}
      {company.companySize && <div><dt><Users /> Quy mô</dt><dd>{displayEnum(company.companySize)}</dd></div>}
      {company.website && <div><dt><ExternalLink /> Website</dt><dd><a href={websiteHref(company.website)} target="_blank" rel="noreferrer">{company.website}</a></dd></div>}
      {company.email && <div><dt><Mail /> Email</dt><dd><a href={`mailto:${company.email}`}>{company.email}</a></dd></div>}
      {company.phone && <div><dt><Phone /> Điện thoại</dt><dd><a href={`tel:${company.phone}`}>{company.phone}</a></dd></div>}
      {company.taxCode && <div><dt><FileText /> Mã số thuế</dt><dd>{company.taxCode}</dd></div>}
      {company.createdAt && <div><dt><CalendarDays /> Tham gia</dt><dd>{formatDate(company.createdAt)}</dd></div>}
    </dl></aside>
  </div>
}
