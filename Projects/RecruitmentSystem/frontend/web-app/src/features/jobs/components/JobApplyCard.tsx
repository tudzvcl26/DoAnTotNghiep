import { zodResolver } from '@hookform/resolvers/zod'
import { AlertCircle, CheckCircle2, FileText, LoaderCircle, LogIn, Send, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import { applyJobSchema, type ApplyJobFormValues } from '../../applications/applications.schemas'
import { normalizeRole } from '../../../types/enums/auth'
import type { CurrentUser } from '../../../types/models/auth'
import type { ResumeAsset } from '../../../types/models/resume'

type JobApplyCardProps = {
  jobId: string
  jobTitle: string
  currentUser: CurrentUser | null
  currentResume?: ResumeAsset
  resumePending: boolean
  resumeError: string
  isAuthenticated: boolean
  isPending: boolean
  applied: boolean
  error: string
  onApply: (coverLetter?: string) => void
}

export function JobApplyCard({ jobId, jobTitle, currentUser, currentResume, resumePending, resumeError, isAuthenticated, isPending, applied, error, onApply }: JobApplyCardProps) {
  const [open, setOpen] = useState(false)
  const isCandidate = currentUser?.roles.some((role) => ['CANDIDATE', 'ADMIN'].includes(normalizeRole(role))) ?? false
  const { register, handleSubmit, watch, reset, formState: { errors } } = useForm<ApplyJobFormValues>({
    resolver: zodResolver(applyJobSchema),
    defaultValues: { coverLetter: '' },
  })
  const coverLetter = watch('coverLetter')

  useEffect(() => {
    if (!open) reset()
  }, [open, reset])

  useEffect(() => {
    if (applied) setOpen(false)
  }, [applied])

  const submit = ({ coverLetter: value }: ApplyJobFormValues) => onApply(value.trim() || undefined)

  return <aside className="job-apply-card">
    <span className="job-apply-card__icon"><Send /></span>
    <h2>Sẵn sàng ứng tuyển?</h2>
    <p>Hệ thống sử dụng hồ sơ ứng viên và CV hiện tại của bạn để tạo đơn ứng tuyển.</p>
    {!isAuthenticated && <Link className="job-apply-login" to={`/login?returnTo=${encodeURIComponent(`/jobs/${jobId}`)}`}><LogIn /> Đăng nhập để ứng tuyển</Link>}
    {isAuthenticated && !isCandidate && <div className="job-apply-message"><AlertCircle /> Chỉ tài khoản ứng viên mới có thể ứng tuyển.</div>}
    {isAuthenticated && isCandidate && !applied && <Button type="button" size="lg" fullWidth onClick={() => setOpen(true)} disabled={resumePending || Boolean(resumeError)}>{resumePending ? 'Đang kiểm tra CV...' : 'Ứng tuyển ngay'} <Send size={17} /></Button>}
    {resumeError && isCandidate && <div className="job-apply-message job-apply-message--error" role="alert"><AlertCircle /> {resumeError}</div>}
    {applied && <div className="job-apply-success" role="status"><CheckCircle2 /><div><strong>Đã ứng tuyển</strong><span>Theo dõi trạng thái trong mục Đơn ứng tuyển.</span></div></div>}
    <small className="job-apply-card__note">Yêu cầu hồ sơ ứng viên và CV hiện tại hợp lệ.</small>

    {open && <div className="job-apply-modal" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget && !isPending) setOpen(false) }}>
      <section role="dialog" aria-modal="true" aria-labelledby="apply-dialog-title">
        <div className="job-apply-modal__heading">
          <div><span>Đơn ứng tuyển</span><h2 id="apply-dialog-title">{jobTitle}</h2></div>
          <button type="button" onClick={() => setOpen(false)} disabled={isPending} aria-label="Đóng"><X /></button>
        </div>
        <div className="job-apply-resume"><FileText /><div><small>CV hiện tại</small><strong>{currentResume?.originalFilename}</strong><span>Phiên bản {currentResume?.assetVersion}</span></div></div>
        <form className="job-apply-form" onSubmit={handleSubmit(submit)} noValidate>
          <label htmlFor="coverLetter"><FileText /> Thư ứng tuyển <span>(không bắt buộc)</span></label>
          <textarea id="coverLetter" {...register('coverLetter')} maxLength={5000} rows={7} placeholder="Giới thiệu ngắn về sự phù hợp của bạn..." aria-invalid={Boolean(errors.coverLetter)} />
          <small>{coverLetter.length}/5000</small>
          {errors.coverLetter && <div className="job-apply-message job-apply-message--error" role="alert"><AlertCircle /> {errors.coverLetter.message}</div>}
          {error && <div className="job-apply-message job-apply-message--error" role="alert"><AlertCircle /> {error}</div>}
          <div><button type="button" onClick={() => setOpen(false)} disabled={isPending}>Hủy</button><Button type="submit" disabled={isPending}>{isPending ? <><LoaderCircle className="job-apply-spinner" /> Đang gửi...</> : <>Gửi đơn <Send /></>}</Button></div>
        </form>
      </section>
    </div>}
  </aside>
}
