import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, CheckCircle2, CircleAlert, Edit3, LoaderCircle, Plus, Sparkles, UserRound } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { Button } from '../../components/ui/Button'
import { useAuth } from '../auth/auth-context'
import { AppError, getErrorMessage } from '../../lib/api/error-adapter'
import type { CandidateProfile } from '../../types/models/profile'
import { profileApi } from './profile.api'
import { initializeProfileSchema, profileSchema, type InitializeProfileForm, type ProfileForm } from './profile.schemas'
import { ProfileCollectionSections } from './components/ProfileCollectionSections'
import { ProfileProfessionalSections } from './components/ProfileProfessionalSections'
import './profile-page.css'

const profileKey = ['candidate-profile'] as const

function isNotFound(error: unknown) {
  return error instanceof AppError && error.status === 404
}

function ProfileSkeleton() {
  return <div className="profile-skeleton" aria-label="Đang tải hồ sơ"><span /><span /><span /><span /></div>
}

function ProfileError({ error, onRetry }: { error: unknown; onRetry: () => void }) {
  return <div className="profile-error" role="alert"><CircleAlert /><div><strong>Chưa thể tải hồ sơ</strong><p>{getErrorMessage(error)}</p></div><button type="button" onClick={onRetry}>Thử lại</button></div>
}

function InitializeProfile({ defaultName }: { defaultName: string }) {
  const queryClient = useQueryClient()
  const { register, handleSubmit, formState: { errors } } = useForm<InitializeProfileForm>({ resolver: zodResolver(initializeProfileSchema), defaultValues: { displayName: defaultName } })
  const mutation = useMutation({
    mutationFn: profileApi.initialize,
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: profileKey }) },
  })

  return (
    <section className="profile-empty-state">
      <span><UserRound size={32} /></span>
      <div><p className="profile-eyebrow">Bắt đầu hồ sơ nghề nghiệp</p><h1>Bạn chưa tạo hồ sơ ứng viên.</h1><p>Khởi tạo hồ sơ bằng tên hiển thị của bạn. Bạn có thể bổ sung thông tin nghề nghiệp ngay sau đó.</p></div>
      <form onSubmit={handleSubmit((values) => mutation.mutate(values))} noValidate>
        <label htmlFor="initialize-display-name">Tên hiển thị</label>
        <input id="initialize-display-name" {...register('displayName')} aria-invalid={Boolean(errors.displayName)} />
        {errors.displayName && <small role="alert">{errors.displayName.message}</small>}
        {mutation.isError && <p className="profile-form-error" role="alert">{getErrorMessage(mutation.error)}</p>}
        <Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? <><LoaderCircle className="spin" size={17} /> Đang tạo...</> : <><Plus size={17} /> Tạo hồ sơ</>}</Button>
      </form>
    </section>
  )
}

function valueOrEmpty(value: string | null | undefined) {
  return value || 'Chưa cập nhật'
}

function PersonalSection({ profile }: { profile: CandidateProfile }) {
  const { currentUser } = useAuth()
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  const [success, setSuccess] = useState(false)
  const { register, handleSubmit, reset, formState: { errors, isDirty } } = useForm<ProfileForm>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      displayName: profile.displayName, headline: profile.headline ?? '', summary: profile.summary ?? '',
      countryCode: profile.countryCode ?? '', provinceCode: profile.provinceCode ?? '', cityName: profile.cityName ?? '',
      districtName: profile.districtName ?? '', contactEmail: profile.contactEmail ?? '', contactPhone: profile.contactPhone ?? '',
      profileVisibility: profile.profileVisibility as ProfileForm['profileVisibility'],
    },
  })

  useEffect(() => {
    reset({ displayName: profile.displayName, headline: profile.headline ?? '', summary: profile.summary ?? '', countryCode: profile.countryCode ?? '', provinceCode: profile.provinceCode ?? '', cityName: profile.cityName ?? '', districtName: profile.districtName ?? '', contactEmail: profile.contactEmail ?? '', contactPhone: profile.contactPhone ?? '', profileVisibility: profile.profileVisibility as ProfileForm['profileVisibility'] })
  }, [profile, reset])

  const mutation = useMutation({
    mutationFn: (values: ProfileForm) => profileApi.update({ ...values, countryCode: values.countryCode?.toUpperCase() || undefined, version: profile.version }),
    onSuccess: async () => { setSuccess(true); setEditing(false); await queryClient.invalidateQueries({ queryKey: profileKey }); window.setTimeout(() => setSuccess(false), 2500) },
  })

  const cancel = () => {
    if (!isDirty || window.confirm('Bỏ các thay đổi chưa lưu?')) { reset(); setEditing(false) }
  }

  return (
    <section className="profile-section">
      <div className="profile-section__heading"><div><span>Thông tin cá nhân</span><h2>Hồ sơ cơ bản</h2></div>{!editing && <button type="button" onClick={() => setEditing(true)}><Edit3 size={16} /> Chỉnh sửa</button>}</div>
      {success && <div className="profile-success" role="status"><CheckCircle2 size={17} /> Đã cập nhật hồ sơ.</div>}
      {!editing ? (
        <div className="profile-personal-view">
          <div className="profile-personal-identity">
            <span className="profile-avatar">{currentUser?.avatarUrl ? <img src={currentUser.avatarUrl} alt={`Ảnh đại diện ${profile.displayName}`} /> : profile.displayName.charAt(0).toUpperCase()}</span>
            <div><h3>{profile.displayName}</h3><p>{valueOrEmpty(profile.headline)}</p></div>
          </div>
          <dl>
            <div><dt>Email tài khoản</dt><dd>{currentUser?.email}</dd></div>
            <div><dt>Email liên hệ</dt><dd>{valueOrEmpty(profile.contactEmail)}</dd></div>
            <div><dt>Số điện thoại</dt><dd>{valueOrEmpty(profile.contactPhone)}</dd></div>
            <div><dt>Địa điểm</dt><dd>{[profile.districtName, profile.cityName, profile.provinceCode, profile.countryCode].filter(Boolean).join(', ') || 'Chưa cập nhật'}</dd></div>
            <div><dt>Hiển thị hồ sơ</dt><dd>{profile.profileVisibility}</dd></div>
            <div><dt>Trạng thái</dt><dd>{profile.profileStatus}</dd></div>
          </dl>
          {profile.summary && <div className="profile-summary"><strong>Giới thiệu</strong><p>{profile.summary}</p></div>}
        </div>
      ) : (
        <form className="profile-form" onSubmit={handleSubmit((values) => mutation.mutate(values))} noValidate>
          <div className="profile-form-grid">
            <label>Họ tên<span>*</span><input {...register('displayName')} aria-invalid={Boolean(errors.displayName)} />{errors.displayName && <small>{errors.displayName.message}</small>}</label>
            <label>Tiêu đề nghề nghiệp<input {...register('headline')} /></label>
            <label>Email tài khoản<input value={currentUser?.email ?? ''} readOnly aria-readonly="true" /></label>
            <label>Email liên hệ<input type="email" {...register('contactEmail')} />{errors.contactEmail && <small>{errors.contactEmail.message}</small>}</label>
            <label>Số điện thoại<input {...register('contactPhone')} /></label>
            <label>Mã quốc gia<input {...register('countryCode')} placeholder="VN" maxLength={2} />{errors.countryCode && <small>{errors.countryCode.message}</small>}</label>
            <label>Mã tỉnh/thành<input {...register('provinceCode')} /></label>
            <label>Thành phố<input {...register('cityName')} /></label>
            <label>Quận/Huyện<input {...register('districtName')} /></label>
            <label>Quyền riêng tư<select {...register('profileVisibility')}><option value="PUBLIC">Công khai</option><option value="RECRUITERS_ONLY">Chỉ nhà tuyển dụng</option><option value="PRIVATE">Riêng tư</option><option value="HIDDEN">Ẩn</option><option value="ANONYMOUS">Ẩn danh</option></select></label>
            <label className="profile-form-wide">Giới thiệu<textarea rows={5} {...register('summary')} />{errors.summary && <small>{errors.summary.message}</small>}</label>
          </div>
          {mutation.isError && <p className="profile-form-error" role="alert">{getErrorMessage(mutation.error)}</p>}
          <div className="profile-form-actions"><Button type="button" variant="ghost" onClick={cancel}>Hủy</Button><Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Đang lưu...' : 'Lưu thay đổi'}</Button></div>
        </form>
      )}
    </section>
  )
}

export function ProfilePage() {
  const { currentUser } = useAuth()
  const profileQuery = useQuery({ queryKey: profileKey, queryFn: profileApi.get, retry: (count, error) => !isNotFound(error) && count < 1 })

  return (
    <div className="profile-page">
      <header className="profile-page-header">
        <Link to="/candidate"><ArrowLeft size={17} /> Quay lại tổng quan</Link>
        <div><span className="profile-eyebrow"><Sparkles size={15} /> Career Profile</span><h1>Hồ sơ của tôi</h1><p>Hoàn thiện thông tin để tăng cơ hội tìm được công việc phù hợp.</p></div>
        {profileQuery.data && typeof profileQuery.data.completionScore === 'number' && <div className="profile-completion"><span>Mức độ hoàn thiện</span><strong>{profileQuery.data.completionScore}%</strong><progress max="100" value={profileQuery.data.completionScore} /></div>}
      </header>

      {profileQuery.isLoading && <ProfileSkeleton />}
      {profileQuery.isError && !isNotFound(profileQuery.error) && <ProfileError error={profileQuery.error} onRetry={() => void profileQuery.refetch()} />}
      {profileQuery.isError && isNotFound(profileQuery.error) && <InitializeProfile defaultName={currentUser?.fullName ?? ''} />}
      {profileQuery.data && currentUser && (
        <div className="profile-page-sections">
          <PersonalSection profile={profileQuery.data} />
          <ProfileProfessionalSections userId={currentUser.id} />
          <ProfileCollectionSections userId={currentUser.id} />
        </div>
      )}
    </div>
  )
}
