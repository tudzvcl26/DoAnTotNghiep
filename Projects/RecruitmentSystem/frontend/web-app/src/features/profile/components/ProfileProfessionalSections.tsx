import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { BriefcaseBusiness, CircleAlert, Edit3, RefreshCw, Target } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Button } from '../../../components/ui/Button'
import { AppError, getErrorMessage } from '../../../lib/api/error-adapter'
import { profileApi } from '../profile.api'
import { objectiveSchema, preferenceSchema, type ObjectiveForm, type PreferenceForm } from '../profile.schemas'

const availabilityOptions = [
  ['ACTIVELY_LOOKING', 'Đang tích cực tìm việc'], ['OPEN_TO_OFFERS', 'Sẵn sàng nhận đề nghị'],
  ['NOT_LOOKING', 'Chưa có nhu cầu'], ['UNAVAILABLE', 'Chưa sẵn sàng'],
] as const

const isNotFound = (error: unknown) => error instanceof AppError && error.status === 404
const clean = <T extends Record<string, unknown>>(values: T) => Object.fromEntries(Object.entries(values).filter(([, value]) => value !== '' && value !== undefined)) as T

function SectionFailure({ error, retry }: { error: unknown; retry: () => void }) {
  return <div className="profile-section-failure" role="alert"><CircleAlert /><div><strong>Không thể tải phần này</strong><p>{getErrorMessage(error)}</p></div><button type="button" onClick={retry}><RefreshCw size={15} /> Thử lại</button></div>
}

function ObjectiveSection({ userId }: { userId: string }) {
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  const query = useQuery({ queryKey: ['candidate-career-objective', userId], queryFn: () => profileApi.getObjective(userId), retry: (count, error) => !isNotFound(error) && count < 1 })
  const { register, handleSubmit, reset, formState: { errors } } = useForm<ObjectiveForm>({ resolver: zodResolver(objectiveSchema) })

  useEffect(() => {
    if (query.data) reset({ objectiveText: query.data.objectiveText ?? '', targetSeniority: query.data.targetSeniority ?? '', availabilityStatus: query.data.availabilityStatus ?? undefined })
  }, [query.data, reset])

  const mutation = useMutation({
    mutationFn: (values: ObjectiveForm) => profileApi.updateObjective(userId, { ...clean(values), version: query.data?.version }),
    onSuccess: async () => { setEditing(false); await queryClient.invalidateQueries({ queryKey: ['candidate-career-objective', userId] }); await queryClient.invalidateQueries({ queryKey: ['candidate-profile'] }) },
  })

  const empty = query.isError && isNotFound(query.error)
  const showForm = editing || empty
  return (
    <section className="profile-section">
      <div className="profile-section__heading"><div><span>Mục tiêu nghề nghiệp</span><h2>Định hướng tiếp theo</h2></div>{query.data && !editing && <button type="button" onClick={() => setEditing(true)}><Edit3 size={16} /> Chỉnh sửa</button>}</div>
      {query.isLoading && <div className="profile-mini-skeleton" />}
      {query.isError && !empty && <SectionFailure error={query.error} retry={() => void query.refetch()} />}
      {query.data && !showForm && <div className="profile-objective-view"><span><Target size={23} /></span><div><p>{query.data.objectiveText || 'Chưa cập nhật mục tiêu nghề nghiệp.'}</p><dl><div><dt>Cấp bậc mục tiêu</dt><dd>{query.data.targetSeniority || 'Chưa cập nhật'}</dd></div><div><dt>Mức độ sẵn sàng</dt><dd>{query.data.availabilityStatus || 'Chưa cập nhật'}</dd></div></dl></div></div>}
      {showForm && (
        <form className="profile-form" onSubmit={handleSubmit((values) => mutation.mutate(values))}>
          {empty && <p className="profile-empty-copy">Chưa có mục tiêu nghề nghiệp. Thông tin chỉ được tạo khi bạn bấm lưu.</p>}
          <div className="profile-form-grid">
            <label>Cấp bậc mục tiêu<input {...register('targetSeniority')} placeholder="Ví dụ: Mid-level" />{errors.targetSeniority && <small>{errors.targetSeniority.message}</small>}</label>
            <label>Mức độ sẵn sàng<select {...register('availabilityStatus')}><option value="">Chưa chọn</option>{availabilityOptions.map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></label>
            <label className="profile-form-wide">Mục tiêu nghề nghiệp<textarea rows={5} {...register('objectiveText')} />{errors.objectiveText && <small>{errors.objectiveText.message}</small>}</label>
          </div>
          {mutation.isError && <p className="profile-form-error">{getErrorMessage(mutation.error)}</p>}
          <div className="profile-form-actions">{query.data && <Button type="button" variant="ghost" onClick={() => { reset(); setEditing(false) }}>Hủy</Button>}<Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Đang lưu...' : 'Lưu mục tiêu'}</Button></div>
        </form>
      )}
    </section>
  )
}

function PreferenceSection({ userId }: { userId: string }) {
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  const query = useQuery({ queryKey: ['candidate-preference', userId], queryFn: () => profileApi.getPreference(userId), retry: (count, error) => !isNotFound(error) && count < 1 })
  const { register, handleSubmit, reset, formState: { errors } } = useForm<PreferenceForm>({ resolver: zodResolver(preferenceSchema), defaultValues: { recommendationConsent: false } })

  useEffect(() => {
    if (query.data) reset({ salaryMinimum: query.data.salaryMinimum ?? undefined, salaryMaximum: query.data.salaryMaximum ?? undefined, salaryCurrency: query.data.salaryCurrency ?? '', salaryPeriod: query.data.salaryPeriod ?? undefined, availabilityStatus: query.data.availabilityStatus ?? undefined, workArrangement: query.data.workArrangement ?? undefined, recommendationConsent: query.data.recommendationConsent ?? false })
  }, [query.data, reset])

  const mutation = useMutation({
    mutationFn: (values: PreferenceForm) => {
      const payload = { ...clean(values), salaryCurrency: values.salaryCurrency?.toUpperCase() || undefined, version: query.data?.version }
      return query.data ? profileApi.updatePreference(userId, payload) : profileApi.createPreference(userId, payload)
    },
    onSuccess: async () => { setEditing(false); await queryClient.invalidateQueries({ queryKey: ['candidate-preference', userId] }); await queryClient.invalidateQueries({ queryKey: ['candidate-profile'] }) },
  })

  const empty = query.isError && isNotFound(query.error)
  const showForm = editing || empty
  return (
    <section className="profile-section">
      <div className="profile-section__heading"><div><span>Ưu tiên nghề nghiệp</span><h2>Điều kiện công việc mong muốn</h2></div>{query.data && !editing && <button type="button" onClick={() => setEditing(true)}><Edit3 size={16} /> Chỉnh sửa</button>}</div>
      {query.isLoading && <div className="profile-mini-skeleton" />}
      {query.isError && !empty && <SectionFailure error={query.error} retry={() => void query.refetch()} />}
      {query.data && !showForm && <div className="profile-preference-view"><span><BriefcaseBusiness size={23} /></span><dl><div><dt>Mức lương</dt><dd>{query.data.salaryMinimum != null || query.data.salaryMaximum != null ? `${query.data.salaryMinimum ?? '—'} – ${query.data.salaryMaximum ?? '—'} ${query.data.salaryCurrency ?? ''} / ${query.data.salaryPeriod ?? ''}` : 'Chưa cập nhật'}</dd></div><div><dt>Hình thức làm việc</dt><dd>{query.data.workArrangement || 'Chưa cập nhật'}</dd></div><div><dt>Sẵn sàng</dt><dd>{query.data.availabilityStatus || 'Chưa cập nhật'}</dd></div><div><dt>Cho phép gợi ý</dt><dd>{query.data.recommendationConsent ? 'Có' : 'Không'}</dd></div></dl></div>}
      {showForm && (
        <form className="profile-form" onSubmit={handleSubmit((values) => mutation.mutate(values))}>
          {empty && <p className="profile-empty-copy">Chưa có ưu tiên nghề nghiệp. Thông tin chỉ được tạo khi bạn bấm lưu.</p>}
          <div className="profile-form-grid">
            <label>Lương tối thiểu<input type="number" min="0" {...register('salaryMinimum', { setValueAs: (value) => value === '' ? undefined : Number(value) })} />{errors.salaryMinimum && <small>{errors.salaryMinimum.message}</small>}</label>
            <label>Lương tối đa<input type="number" min="0" {...register('salaryMaximum', { setValueAs: (value) => value === '' ? undefined : Number(value) })} />{errors.salaryMaximum && <small>{errors.salaryMaximum.message}</small>}</label>
            <label>Đơn vị tiền tệ<input {...register('salaryCurrency')} maxLength={3} placeholder="VND" /></label>
            <label>Chu kỳ lương<select {...register('salaryPeriod')}><option value="">Chưa chọn</option><option value="HOURLY">Theo giờ</option><option value="MONTHLY">Theo tháng</option><option value="YEARLY">Theo năm</option></select></label>
            <label>Hình thức làm việc<select {...register('workArrangement')}><option value="">Chưa chọn</option><option value="ONSITE">Tại văn phòng</option><option value="HYBRID">Linh hoạt</option><option value="REMOTE">Từ xa</option><option value="FLEXIBLE">Tùy chọn</option></select></label>
            <label>Mức độ sẵn sàng<select {...register('availabilityStatus')}><option value="">Chưa chọn</option>{availabilityOptions.map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></label>
            <label className="profile-checkbox"><input type="checkbox" {...register('recommendationConsent')} /> Cho phép hệ thống gợi ý cơ hội phù hợp</label>
          </div>
          {mutation.isError && <p className="profile-form-error">{getErrorMessage(mutation.error)}</p>}
          <div className="profile-form-actions">{query.data && <Button type="button" variant="ghost" onClick={() => { reset(); setEditing(false) }}>Hủy</Button>}<Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Đang lưu...' : 'Lưu ưu tiên'}</Button></div>
        </form>
      )}
    </section>
  )
}

export function ProfileProfessionalSections({ userId }: { userId: string }) {
  return <><ObjectiveSection userId={userId} /><PreferenceSection userId={userId} /></>
}
