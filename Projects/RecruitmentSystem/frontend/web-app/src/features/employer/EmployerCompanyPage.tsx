import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertCircle, Building2, CalendarDays, CheckCircle2, Edit3, ExternalLink, Globe2, Mail, Phone, Plus, RefreshCw, ShieldCheck } from 'lucide-react'
import { useState } from 'react'
import { Button } from '../../components/ui/Button'
import { getErrorMessage } from '../../lib/api/error-adapter'
import type { Company, CreateCompanyRequest, UpdateCompanyRequest } from '../../types/models/company'
import { useAuth } from '../auth/auth-context'
import { EmployerCompanyForm } from './components/EmployerCompanyForm'
import { createEmployerCompany, employerCompanyKey, getEmployerCompanies, updateEmployerCompany } from './employer.api'

const statusLabels: Record<string, string> = { PENDING: 'Đang chờ xác minh', VERIFIED: 'Đã xác minh', REJECTED: 'Bị từ chối', ACTIVE: 'Đang hoạt động', INACTIVE: 'Ngừng hoạt động', SUSPENDED: 'Tạm khóa' }
const typeLabels: Record<string, string> = { PRIVATE: 'Tư nhân', PUBLIC: 'Đại chúng', STARTUP: 'Khởi nghiệp', NON_PROFIT: 'Phi lợi nhuận', GOVERNMENT: 'Nhà nước', OTHER: 'Khác' }
const sizeLabels: Record<string, string> = { MICRO: 'Siêu nhỏ', SMALL: 'Nhỏ', MEDIUM: 'Vừa', LARGE: 'Lớn', ENTERPRISE: 'Tập đoàn' }

function label(value: string | null, labels: Record<string, string>) {
  return value ? labels[value] ?? value : 'Chưa cập nhật'
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

function CompanyCard({ company, userId, onEdit }: { company: Company; userId: string; onEdit: () => void }) {
  return <article className="employer-company-profile">
    {company.bannerUrl && <div className="employer-company-profile__banner"><img src={company.bannerUrl} alt={`Ảnh bìa ${company.name}`} /></div>}
    <div className="employer-company-profile__hero">
      <div className="employer-company-profile__logo">{company.logoUrl ? <img src={company.logoUrl} alt={`Logo ${company.name}`} /> : company.name.slice(0, 2).toUpperCase()}</div>
      <div><span className={`employer-chip employer-chip--${company.verificationStatus?.toLowerCase()}`}>{label(company.verificationStatus, statusLabels)}</span><h2>{company.name}</h2><p>{company.description || 'Doanh nghiệp chưa cập nhật phần giới thiệu.'}</p></div>
      <Button type="button" variant="secondary" size="sm" onClick={onEdit}><Edit3 /> Chỉnh sửa</Button>
    </div>
    <dl className="employer-company-profile__details">
      <div><dt>Loại hình</dt><dd>{label(company.companyType, typeLabels)}</dd></div><div><dt>Quy mô</dt><dd>{label(company.companySize, sizeLabels)}</dd></div>
      <div><dt>Trạng thái</dt><dd>{label(company.status, statusLabels)}</dd></div><div><dt>Mã số thuế</dt><dd>{company.taxCode || 'Chưa cập nhật'}</dd></div>
      <div><dt>Slug công khai</dt><dd>{company.slug}</dd></div><div><dt>Ngày tạo</dt><dd>{formatDate(company.createdAt)}</dd></div>
      <div><dt>Cập nhật gần nhất</dt><dd>{formatDate(company.updatedAt)}</dd></div><div><dt>Quyền sở hữu</dt><dd>{company.ownerId === userId ? 'Đã xác minh' : 'Không khớp'}</dd></div>
    </dl>
    <div className="employer-company-profile__contact">{company.website && <a href={company.website} target="_blank" rel="noreferrer"><Globe2 /> Website <ExternalLink /></a>}{company.email && <a href={`mailto:${company.email}`}><Mail /> {company.email}</a>}{company.phone && <a href={`tel:${company.phone}`}><Phone /> {company.phone}</a>}</div>
    <div className="employer-company-profile__meta"><p><ShieldCheck /> Trạng thái xác minh do backend quản lý; Employer không thể tự thay đổi.</p><p><CalendarDays /> Company ID: {company.id}</p></div>
  </article>
}

export function EmployerCompanyPage() {
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const queryClient = useQueryClient()
  const [editingId, setEditingId] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)
  const [success, setSuccess] = useState('')
  const companies = useQuery({ queryKey: employerCompanyKey(userId), queryFn: () => getEmployerCompanies(userId), enabled: Boolean(userId) })

  const finish = async (message: string) => {
    setSuccess(message); setEditingId(null); setCreating(false)
    await queryClient.invalidateQueries({ queryKey: employerCompanyKey(userId) })
    window.setTimeout(() => setSuccess(''), 3000)
  }
  const createMutation = useMutation({ mutationFn: createEmployerCompany, onSuccess: () => finish('Đã tạo hồ sơ công ty.') })
  const updateMutation = useMutation({ mutationFn: ({ id, request }: { id: string; request: UpdateCompanyRequest }) => updateEmployerCompany(id, request), onSuccess: () => finish('Đã cập nhật hồ sơ công ty.') })
  const editingCompany = companies.data?.find((company) => company.id === editingId)

  return <main className="employer-company-page">
    <header><div><span>Employer Portal</span><h1>Quản lý công ty</h1><p>Xem và cập nhật hồ sơ doanh nghiệp thuộc tài khoản Employer hiện tại.</p></div><ShieldCheck /></header>
    {success && <div className="employer-company-success" role="status"><CheckCircle2 /> {success}</div>}
    {companies.isPending && <div className="employer-company-page__skeleton" aria-label="Đang tải hồ sơ công ty"><span /><span /><span /></div>}
    {companies.isError && <div className="employer-error" role="alert"><AlertCircle /><div><strong>Chưa thể tải hồ sơ công ty</strong><p>{getErrorMessage(companies.error)}</p></div><button type="button" onClick={() => void companies.refetch()}><RefreshCw /> Thử lại</button></div>}
    {companies.isSuccess && companies.data.length === 0 && !creating && <section className="employer-company-empty"><span><Building2 /></span><h2>Bạn chưa có công ty</h2><p>Company Service hỗ trợ Employer tạo hồ sơ doanh nghiệp. Owner ID sẽ được backend lấy từ phiên đăng nhập, không nhận từ biểu mẫu.</p><Button type="button" onClick={() => setCreating(true)}><Plus /> Tạo công ty</Button></section>}
    {creating && <EmployerCompanyForm pending={createMutation.isPending} error={createMutation.error} onCancel={() => { createMutation.reset(); setCreating(false) }} onSubmit={(request) => createMutation.mutate(request as CreateCompanyRequest)} />}
    {editingCompany && <EmployerCompanyForm company={editingCompany} pending={updateMutation.isPending} error={updateMutation.error} onCancel={() => { updateMutation.reset(); setEditingId(null) }} onSubmit={(request) => updateMutation.mutate({ id: editingCompany.id, request: request as UpdateCompanyRequest })} />}
    {!creating && !editingCompany && companies.data?.map((company) => <CompanyCard key={company.id} company={company} userId={userId} onEdit={() => { updateMutation.reset(); setEditingId(company.id) }} />)}
    {companies.isSuccess && <aside className="employer-company-lifecycle"><AlertCircle /><div><strong>Không hiển thị thao tác xóa</strong><p>Backend có soft-delete, nhưng chưa mô tả ảnh hưởng tới Jobs và Applications. Phase 5B không expose thao tác có thể làm gián đoạn dữ liệu tuyển dụng.</p></div></aside>}
  </main>
}
