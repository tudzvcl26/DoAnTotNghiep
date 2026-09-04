import { zodResolver } from '@hookform/resolvers/zod'
import { LoaderCircle, Save, X } from 'lucide-react'
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Button } from '../../../components/ui/Button'
import { AppError, getErrorMessage } from '../../../lib/api/error-adapter'
import { COMPANY_SIZES, COMPANY_TYPES, type Company, type CreateCompanyRequest, type UpdateCompanyRequest } from '../../../types/models/company'
import { employerCompanySchema, type EmployerCompanyForm } from '../employer-company.schemas'

const typeLabels = { PRIVATE: 'Tư nhân', PUBLIC: 'Đại chúng', STARTUP: 'Khởi nghiệp', NON_PROFIT: 'Phi lợi nhuận', GOVERNMENT: 'Nhà nước', OTHER: 'Khác' }
const sizeLabels = { MICRO: 'Siêu nhỏ', SMALL: 'Nhỏ', MEDIUM: 'Vừa', LARGE: 'Lớn', ENTERPRISE: 'Tập đoàn' }

function valuesFrom(company?: Company): EmployerCompanyForm {
  return {
    name: company?.name ?? '', description: company?.description ?? '', website: company?.website ?? '',
    email: company?.email ?? '', phone: company?.phone ?? '', taxCode: company?.taxCode ?? '',
    companyType: company?.companyType ?? '', companySize: company?.companySize ?? '',
    logoUrl: company?.logoUrl ?? '', bannerUrl: company?.bannerUrl ?? '',
  }
}

function withoutEmptyValues(values: EmployerCompanyForm): CreateCompanyRequest {
  return Object.fromEntries(Object.entries(values).filter(([, value]) => value !== '')) as CreateCompanyRequest
}

function updateValues(values: EmployerCompanyForm): UpdateCompanyRequest {
  const { taxCode: _taxCode, companyType, companySize, ...editable } = values
  return {
    ...editable,
    ...(companyType ? { companyType } : {}),
    ...(companySize ? { companySize } : {}),
  }
}

export function EmployerCompanyForm({ company, pending, error, onCancel, onSubmit }: {
  company?: Company
  pending: boolean
  error: unknown
  onCancel: () => void
  onSubmit: (request: CreateCompanyRequest | UpdateCompanyRequest) => void
}) {
  const { register, handleSubmit, reset, setError, formState: { errors, isDirty } } = useForm<EmployerCompanyForm>({
    resolver: zodResolver(employerCompanySchema), defaultValues: valuesFrom(company),
  })

  useEffect(() => reset(valuesFrom(company)), [company, reset])
  useEffect(() => {
    if (!(error instanceof AppError) || !error.fieldErrors) return
    for (const [field, message] of Object.entries(error.fieldErrors)) {
      if (field in valuesFrom(company)) setError(field as keyof EmployerCompanyForm, { message })
    }
  }, [company, error, setError])

  const cancel = () => {
    if (!isDirty || window.confirm('Bỏ các thay đổi chưa lưu?')) onCancel()
  }

  return <form className="employer-company-form" onSubmit={handleSubmit((values) => onSubmit(company ? updateValues(values) : withoutEmptyValues(values)))} noValidate>
    <div className="employer-company-form__heading"><div><span>{company ? 'Cập nhật thông tin' : 'Khởi tạo doanh nghiệp'}</span><h2>{company ? 'Chỉnh sửa hồ sơ công ty' : 'Tạo công ty'}</h2></div><button type="button" onClick={cancel} aria-label="Đóng biểu mẫu"><X /></button></div>
    <div className="employer-company-form__grid">
      <label>Tên công ty <em>*</em><input {...register('name')} aria-invalid={Boolean(errors.name)} />{errors.name && <small role="alert">{errors.name.message}</small>}</label>
      <label>Email công ty<input type="email" {...register('email')} aria-invalid={Boolean(errors.email)} />{errors.email && <small role="alert">{errors.email.message}</small>}</label>
      <label>Loại hình <em>*</em><select {...register('companyType')} aria-invalid={Boolean(errors.companyType)}><option value="">Chọn loại hình</option>{COMPANY_TYPES.map((value) => <option key={value} value={value}>{typeLabels[value]}</option>)}</select>{errors.companyType && <small role="alert">{errors.companyType.message}</small>}</label>
      <label>Quy mô <em>*</em><select {...register('companySize')} aria-invalid={Boolean(errors.companySize)}><option value="">Chọn quy mô</option>{COMPANY_SIZES.map((value) => <option key={value} value={value}>{sizeLabels[value]}</option>)}</select>{errors.companySize && <small role="alert">{errors.companySize.message}</small>}</label>
      <label>Website<input {...register('website')} placeholder="https://example.com" aria-invalid={Boolean(errors.website)} />{errors.website && <small role="alert">{errors.website.message}</small>}</label>
      <label>Số điện thoại<input {...register('phone')} aria-invalid={Boolean(errors.phone)} />{errors.phone && <small role="alert">{errors.phone.message}</small>}</label>
      <label>Mã số thuế<input {...register('taxCode')} readOnly={Boolean(company)} aria-readonly={Boolean(company)} />{company && <small className="employer-company-form__hint">Backend không hỗ trợ thay đổi mã số thuế sau khi tạo.</small>}{errors.taxCode && <small role="alert">{errors.taxCode.message}</small>}</label>
      <label>Logo URL<input {...register('logoUrl')} aria-invalid={Boolean(errors.logoUrl)} />{errors.logoUrl && <small role="alert">{errors.logoUrl.message}</small>}</label>
      <label className="employer-company-form__wide">Banner URL<input {...register('bannerUrl')} aria-invalid={Boolean(errors.bannerUrl)} />{errors.bannerUrl && <small role="alert">{errors.bannerUrl.message}</small>}</label>
      <label className="employer-company-form__wide">Giới thiệu<textarea rows={7} {...register('description')} aria-invalid={Boolean(errors.description)} />{errors.description && <small role="alert">{errors.description.message}</small>}</label>
    </div>
    {Boolean(error) && <p className="employer-company-form__error" role="alert">{getErrorMessage(error)}</p>}
    <div className="employer-company-form__actions"><Button type="button" variant="ghost" onClick={cancel} disabled={pending}>Hủy</Button><Button type="submit" disabled={pending}>{pending ? <><LoaderCircle className="spin" /> Đang lưu...</> : <><Save /> {company ? 'Lưu thay đổi' : 'Tạo công ty'}</>}</Button></div>
  </form>
}
