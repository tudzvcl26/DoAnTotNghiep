import { AlertCircle, CheckCircle2, FileText, LoaderCircle, LogIn, Send } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import { normalizeRole } from '../../../types/enums/auth'
import type { CurrentUser } from '../../../types/models/auth'

export function JobApplyCard({ jobId, currentUser, isAuthenticated, isPending, success, error, onApply }: { jobId: string; currentUser: CurrentUser | null; isAuthenticated: boolean; isPending: boolean; success: boolean; error: string; onApply: (coverLetter?: string) => void }) {
  const [open, setOpen] = useState(false)
  const [coverLetter, setCoverLetter] = useState('')
  const isCandidate = currentUser?.roles.some((role) => ['CANDIDATE', 'ADMIN'].includes(normalizeRole(role))) ?? false

  return <aside className="job-apply-card">
    <span className="job-apply-card__icon"><Send /></span>
    <h2>Sẵn sàng ứng tuyển?</h2>
    <p>Hệ thống sẽ sử dụng hồ sơ ứng viên và CV hiện tại của bạn để tạo đơn ứng tuyển.</p>
    {!isAuthenticated && <Link className="job-apply-login" to={`/login?returnTo=${encodeURIComponent(`/jobs/${jobId}`)}`}><LogIn /> Đăng nhập để ứng tuyển</Link>}
    {isAuthenticated && !isCandidate && <div className="job-apply-message"><AlertCircle /> Chỉ tài khoản ứng viên mới có thể ứng tuyển.</div>}
    {isAuthenticated && isCandidate && !open && !success && <Button type="button" size="lg" fullWidth onClick={() => setOpen(true)}>Ứng tuyển ngay <Send size={17} /></Button>}
    {open && !success && <form className="job-apply-form" onSubmit={(event) => { event.preventDefault(); onApply(coverLetter.trim() || undefined) }}><label htmlFor="coverLetter"><FileText /> Thư ứng tuyển <span>(không bắt buộc)</span></label><textarea id="coverLetter" value={coverLetter} onChange={(event) => setCoverLetter(event.target.value)} maxLength={5000} rows={5} placeholder="Giới thiệu ngắn về sự phù hợp của bạn..." /><small>{coverLetter.length}/5000</small>{error && <div className="job-apply-message job-apply-message--error" role="alert"><AlertCircle /> {error}</div>}<div><button type="button" onClick={() => setOpen(false)} disabled={isPending}>Hủy</button><Button type="submit" disabled={isPending}>{isPending ? <><LoaderCircle className="job-apply-spinner" /> Đang gửi...</> : <>Gửi đơn <Send /></>}</Button></div></form>}
    {success && <div className="job-apply-success" role="status"><CheckCircle2 /><div><strong>Đã gửi đơn ứng tuyển</strong><span>Bạn có thể theo dõi trạng thái trong Candidate Dashboard.</span></div></div>}
    <small className="job-apply-card__note">Yêu cầu hồ sơ ứng viên và CV hiện tại hợp lệ.</small>
  </aside>
}
