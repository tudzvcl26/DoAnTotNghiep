import { useQuery } from '@tanstack/react-query'
import { AlertCircle, Building2, ExternalLink, Globe2, Mail, Phone, RefreshCw, ShieldCheck } from 'lucide-react'
import { getErrorMessage } from '../../lib/api/error-adapter'
import { useAuth } from '../auth/auth-context'
import { getEmployerCompanies } from './employer.api'

function humanize(value: string | null) {
  return value ? value.replaceAll('_', ' ').toLocaleLowerCase('vi-VN') : 'Chưa cập nhật'
}

export function EmployerCompanyPage() {
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const companies = useQuery({ queryKey: ['employer-companies', userId], queryFn: () => getEmployerCompanies(userId), enabled: Boolean(userId) })

  return <main className="employer-company-page">
    <header><div><span>Employer Portal</span><h1>Hồ sơ công ty</h1><p>Thông tin doanh nghiệp active được Company Service xác nhận thuộc tài khoản hiện tại.</p></div><ShieldCheck /></header>
    {companies.isPending && <div className="employer-company-page__skeleton"><span /><span /><span /></div>}
    {companies.isError && <div className="employer-error" role="alert"><AlertCircle /><div><strong>Chưa thể tải hồ sơ công ty</strong><p>{getErrorMessage(companies.error)}</p></div><button type="button" onClick={() => void companies.refetch()}><RefreshCw /> Thử lại</button></div>}
    {companies.isSuccess && companies.data.length === 0 && <section className="employer-company-empty"><span><Building2 /></span><h2>Bạn chưa tạo công ty</h2><p>Backend có Company CRUD nhưng Phase 5A chỉ triển khai overview. Việc tạo và chỉnh sửa công ty sẽ thuộc Company Management phase sau.</p></section>}
    {companies.data?.map((company) => <article className="employer-company-profile" key={company.id}>
      <div className="employer-company-profile__hero"><div className="employer-company-profile__logo">{company.logoUrl ? <img src={company.logoUrl} alt="" /> : company.name.slice(0, 2).toUpperCase()}</div><div><span className={`employer-chip employer-chip--${company.verificationStatus?.toLowerCase()}`}>{humanize(company.verificationStatus)}</span><h2>{company.name}</h2><p>{company.description || 'Doanh nghiệp chưa cập nhật phần giới thiệu.'}</p></div></div>
      <dl className="employer-company-profile__details"><div><dt>Loại hình</dt><dd>{humanize(company.companyType)}</dd></div><div><dt>Quy mô</dt><dd>{humanize(company.companySize)}</dd></div><div><dt>Trạng thái</dt><dd>{humanize(company.status)}</dd></div><div><dt>Mã số thuế</dt><dd>{company.taxCode ?? 'Chưa cập nhật'}</dd></div></dl>
      <div className="employer-company-profile__contact">{company.website && <a href={company.website} target="_blank" rel="noreferrer"><Globe2 /> Website <ExternalLink /></a>}{company.email && <a href={`mailto:${company.email}`}><Mail /> {company.email}</a>}{company.phone && <a href={`tel:${company.phone}`}><Phone /> {company.phone}</a>}</div>
      <p className="employer-contract-note"><ShieldCheck /> Owner ID khớp tài khoản đăng nhập: {company.ownerId === userId ? 'Đã xác minh' : 'Không khớp'}</p>
    </article>)}
  </main>
}
