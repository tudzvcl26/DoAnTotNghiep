import { zodResolver } from '@hookform/resolvers/zod'
import { LoaderCircle, Save } from 'lucide-react'
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Button, ButtonLink } from '../../../components/ui/Button'
import { AppError, getErrorMessage } from '../../../lib/api/error-adapter'
import {
  EMPLOYMENT_TYPES, EXPERIENCE_LEVELS, type JobCategory, type JobDetail, type JobMutationRequest,
} from '../../../types/models/job'
import { employerJobSchema, type EmployerJobFormValues } from '../employer-job.schemas'

const employmentLabels = { FULL_TIME: 'Toàn thời gian', PART_TIME: 'Bán thời gian', INTERNSHIP: 'Thực tập', FREELANCE: 'Freelance', CONTRACT: 'Hợp đồng', TEMPORARY: 'Tạm thời' }
const experienceLabels = { NO_EXPERIENCE: 'Không yêu cầu kinh nghiệm', FRESHER: 'Fresher', JUNIOR: 'Junior', MIDDLE: 'Middle', SENIOR: 'Senior', LEADER: 'Leader', MANAGER: 'Manager' }

function initialValues(job?: JobDetail): EmployerJobFormValues {
  return {
    title: job?.title ?? '', jobCode: job?.jobCode ?? '', description: job?.description ?? '',
    requirements: job?.requirements ?? '', responsibilities: job?.responsibilities ?? '',
    salaryMin: job?.salaryMin == null ? '' : String(job.salaryMin), salaryMax: job?.salaryMax == null ? '' : String(job.salaryMax),
    currency: job?.currency ?? 'VND', employmentType: job?.employmentType ?? 'FULL_TIME',
    experienceLevel: job?.experienceLevel ?? 'NO_EXPERIENCE', quantity: job?.quantity ?? 1,
    applicationDeadline: job?.applicationDeadline ?? '', remoteAllowed: job?.remoteAllowed ?? false,
    categoryId: job?.categoryId ?? '',
  }
}

function toRequest(values: EmployerJobFormValues, companyId: string): JobMutationRequest {
  return {
    title: values.title.trim(), jobCode: values.jobCode.trim(),
    ...(values.description ? { description: values.description } : {}),
    ...(values.requirements ? { requirements: values.requirements } : {}),
    ...(values.responsibilities ? { responsibilities: values.responsibilities } : {}),
    ...(values.salaryMin ? { salaryMin: Number(values.salaryMin) } : {}),
    ...(values.salaryMax ? { salaryMax: Number(values.salaryMax) } : {}),
    ...(values.currency ? { currency: values.currency } : {}),
    employmentType: values.employmentType, experienceLevel: values.experienceLevel, quantity: values.quantity,
    ...(values.applicationDeadline ? { applicationDeadline: values.applicationDeadline } : {}),
    remoteAllowed: values.remoteAllowed, active: true, companyId, categoryId: values.categoryId,
  }
}

export function EmployerJobForm({ job, companyId, companyName, categories, pending, error, onSubmit }: {
  job?: JobDetail
  companyId: string
  companyName: string
  categories: JobCategory[]
  pending: boolean
  error: unknown
  onSubmit: (request: JobMutationRequest) => void
}) {
  const { register, handleSubmit, reset, setError, formState: { errors } } = useForm<EmployerJobFormValues>({
    resolver: zodResolver(employerJobSchema), defaultValues: initialValues(job),
  })

  useEffect(() => reset(initialValues(job)), [job, reset])
  useEffect(() => {
    if (!(error instanceof AppError) || !error.fieldErrors) return
    for (const [field, message] of Object.entries(error.fieldErrors)) {
      if (field in initialValues(job)) setError(field as keyof EmployerJobFormValues, { message })
    }
  }, [error, job, setError])

  return <form className="employer-job-form" onSubmit={handleSubmit((values) => onSubmit(toRequest(values, companyId)))} noValidate>
    <div className="employer-job-form__intro"><div><span>{job ? 'Cập nhật bản nháp tuyển dụng' : 'Tạo cơ hội mới'}</span><h1>{job ? 'Chỉnh sửa việc làm' : 'Tạo việc làm'}</h1><p>Company được xác định từ tài khoản Employer. Trạng thái mới mặc định là DRAFT.</p></div><div><small>Doanh nghiệp sở hữu</small><strong>{companyName}</strong><code>{companyId}</code></div></div>
    <div className="employer-job-form__grid">
      <label>Tiêu đề <em>*</em><input {...register('title')} aria-invalid={Boolean(errors.title)} />{errors.title && <small role="alert">{errors.title.message}</small>}</label>
      <label>Mã công việc <em>*</em><input {...register('jobCode')} aria-invalid={Boolean(errors.jobCode)} />{errors.jobCode && <small role="alert">{errors.jobCode.message}</small>}</label>
      <label>Danh mục <em>*</em><select {...register('categoryId')} aria-invalid={Boolean(errors.categoryId)}><option value="">Chọn danh mục</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select>{errors.categoryId && <small role="alert">{errors.categoryId.message}</small>}</label>
      <label>Hình thức <em>*</em><select {...register('employmentType')}>{EMPLOYMENT_TYPES.map((value) => <option key={value} value={value}>{employmentLabels[value]}</option>)}</select></label>
      <label>Cấp độ kinh nghiệm <em>*</em><select {...register('experienceLevel')}>{EXPERIENCE_LEVELS.map((value) => <option key={value} value={value}>{experienceLabels[value]}</option>)}</select></label>
      <label>Số lượng <em>*</em><input type="number" min="1" step="1" {...register('quantity', { valueAsNumber: true })} aria-invalid={Boolean(errors.quantity)} />{errors.quantity && <small role="alert">{errors.quantity.message}</small>}</label>
      <label>Lương tối thiểu<input inputMode="decimal" {...register('salaryMin')} aria-invalid={Boolean(errors.salaryMin)} />{errors.salaryMin && <small role="alert">{errors.salaryMin.message}</small>}</label>
      <label>Lương tối đa<input inputMode="decimal" {...register('salaryMax')} aria-invalid={Boolean(errors.salaryMax)} />{errors.salaryMax && <small role="alert">{errors.salaryMax.message}</small>}</label>
      <label>Đơn vị tiền tệ<input {...register('currency')} maxLength={10} aria-invalid={Boolean(errors.currency)} />{errors.currency && <small role="alert">{errors.currency.message}</small>}</label>
      <label>Hạn ứng tuyển<input type="date" {...register('applicationDeadline')} /></label>
      <label className="employer-job-form__check"><input type="checkbox" {...register('remoteAllowed')} /><span>Cho phép làm việc từ xa</span></label>
      <label className="employer-job-form__wide">Mô tả công việc<textarea rows={7} {...register('description')} /></label>
      <label className="employer-job-form__wide">Trách nhiệm<textarea rows={6} {...register('responsibilities')} /></label>
      <label className="employer-job-form__wide">Yêu cầu ứng viên<textarea rows={6} {...register('requirements')} /></label>
    </div>
    {Boolean(error) && <p className="employer-job-form__error" role="alert">{getErrorMessage(error)}</p>}
    <div className="employer-job-form__actions"><ButtonLink to={job ? `/employer/jobs/${job.id}` : '/employer/jobs'} variant="ghost">Hủy</ButtonLink><Button type="submit" disabled={pending}>{pending ? <><LoaderCircle className="spin" /> Đang lưu...</> : <><Save /> {job ? 'Lưu thay đổi' : 'Tạo bản nháp'}</>}</Button></div>
  </form>
}
