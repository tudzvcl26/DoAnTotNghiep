import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertCircle, ArrowLeft, BriefcaseBusiness, Download, FileClock, FileText, FileUser, RefreshCw, ShieldAlert } from 'lucide-react'
import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Button } from '../../components/ui/Button'
import { getErrorMessage, normalizeApiError } from '../../lib/api/error-adapter'
import type { Application, ApplicationStatus } from '../../types/models/application'
import { useAuth } from '../auth/auth-context'
import {
  employerApplicationStatusLabels, employerTransitions, formatEmployerApplicationDate, parseSnapshot, snapshotNumber, snapshotText,
} from './employer-application.presenter'
import {
  downloadEmployerApplicationResume, employerApplicationKey, employerApplicationsKey,
  employerApplicationStatisticsKey, employerApplicationSummaryKey, employerCompanyKey,
  getEmployerApplication, getEmployerCompanies, updateEmployerApplicationStatus,
} from './employer.api'

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

export function EmployerApplicationDetailPage() {
  const { applicationId = '' } = useParams()
  const validId = UUID.test(applicationId)
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const queryClient = useQueryClient()
  const [nextStatus, setNextStatus] = useState<ApplicationStatus | ''>('')
  const [reasonDetail, setReasonDetail] = useState('')
  const application = useQuery({ queryKey: employerApplicationKey(applicationId), queryFn: () => getEmployerApplication(applicationId), enabled: validId, retry: (count, error) => ![403, 404].includes(normalizeApiError(error).status ?? 0) && count < 1 })
  const companies = useQuery({ queryKey: employerCompanyKey(userId), queryFn: () => getEmployerCompanies(userId), enabled: Boolean(userId) })
  const owned = companies.data?.some((company) => company.id === application.data?.companyId)
  const transition = useMutation({
    mutationFn: () => updateEmployerApplicationStatus(applicationId, { status: nextStatus as ApplicationStatus, ...(reasonDetail.trim() ? { reasonDetail: reasonDetail.trim() } : {}) }),
    onSuccess: async (updated: Application) => {
      queryClient.setQueryData(employerApplicationKey(applicationId), updated)
      setNextStatus('')
      setReasonDetail('')
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: employerApplicationsKey }),
        queryClient.invalidateQueries({ queryKey: employerApplicationStatisticsKey }),
        queryClient.invalidateQueries({ queryKey: employerApplicationSummaryKey }),
      ])
    },
  })
  const resumeDownload = useMutation({
    mutationFn: () => downloadEmployerApplicationResume(applicationId),
    onSuccess: (blob) => {
      const snapshot = parseSnapshot(application.data?.resumeSnapshot?.snapshotData)
      const filename = snapshotText(snapshot, 'originalFilename') ?? `resume-${applicationId}.bin`
      const objectUrl = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = objectUrl
      anchor.download = filename
      document.body.appendChild(anchor)
      anchor.click()
      anchor.remove()
      window.setTimeout(() => URL.revokeObjectURL(objectUrl), 0)
    },
  })

  if (!validId) return <main className="employer-job-state"><AlertCircle /><h1>ID Application không hợp lệ</h1><Link to="/employer/applications">Quay lại danh sách</Link></main>
  if (application.isPending || companies.isPending) return <main className="employer-applications-page"><div className="employer-job-skeleton" aria-label="Đang tải hồ sơ ứng viên"><span /><span /><span /></div></main>
  if (application.isError) { const status = normalizeApiError(application.error).status; return <main className="employer-job-state"><AlertCircle /><h1>{status === 403 ? 'Không có quyền xem Application' : status === 404 ? 'Không tìm thấy Application' : 'Không thể tải Application'}</h1><p>{getErrorMessage(application.error)}</p><div><Link to="/employer/applications">Danh sách ứng viên</Link><button type="button" onClick={() => void application.refetch()}><RefreshCw /> Thử lại</button></div></main> }
  if (!owned) return <main className="employer-job-state"><ShieldAlert /><h1>Ownership không khớp</h1><p>Employer Portal không hiển thị hồ sơ khi Company của Application không thuộc tài khoản.</p><Link to="/employer/applications">Quay lại danh sách</Link></main>

  const data = application.data
  const jobSnapshot = parseSnapshot(data.jobSnapshot?.snapshotData)
  const resumeSnapshot = parseSnapshot(data.resumeSnapshot?.snapshotData)
  const jobTitle = snapshotText(jobSnapshot, 'title') ?? `Job ${data.jobId.slice(0, 8)}`
  const resumeName = snapshotText(resumeSnapshot, 'originalFilename')
  const resumeType = snapshotText(resumeSnapshot, 'contentType')
  const resumeSize = snapshotNumber(resumeSnapshot, 'sizeBytes')
  const transitions = employerTransitions[data.status]
  const candidate = data.candidateProfileSnapshot

  return <main className="employer-application-detail">
    <Link className="employer-application-detail__back" to="/employer/applications"><ArrowLeft /> Quản lý ứng viên</Link>
    <header><div><span className={`employer-application-status employer-application-status--${data.status.toLowerCase()}`}>{employerApplicationStatusLabels[data.status]}</span><h1>{jobTitle}</h1><p>{candidate?.displayName || `Ứng viên ${data.candidateId}`} · Nộp {formatEmployerApplicationDate(data.appliedAtInstant ?? data.appliedAt)}</p></div><FileUser /></header>
    <section className="employer-application-detail__grid">
      <article><div className="employer-application-detail__heading"><BriefcaseBusiness /><h2>Thông tin ứng viên</h2></div><dl><div><dt>Họ tên lúc ứng tuyển</dt><dd>{candidate?.displayName || 'Snapshot chưa có'}</dd></div><div><dt>Tiêu đề</dt><dd>{candidate?.headline || 'Chưa cập nhật'}</dd></div><div><dt>Email liên hệ</dt><dd>{candidate?.contactEmail || 'Chưa cung cấp'}</dd></div><div><dt>Điện thoại</dt><dd>{candidate?.contactPhone || 'Chưa cung cấp'}</dd></div><div><dt>Snapshot lúc</dt><dd>{candidate ? formatEmployerApplicationDate(candidate.capturedAt) : 'Dữ liệu cũ'}</dd></div><div><dt>Mã đơn</dt><dd>{data.id}</dd></div>{data.matchingScore != null && <div><dt>Matching score</dt><dd>{data.matchingScore}</dd></div>}</dl></article>
      <article><div className="employer-application-detail__heading"><FileText /><h2>Hồ sơ đã nộp</h2></div><section><h3>Thư xin việc</h3><p>{data.coverLetter || 'Không có thư xin việc'}</p></section><section><h3>CV snapshot</h3>{data.resumeSnapshot ? <><dl><div><dt>Tên tệp</dt><dd>{resumeName ?? 'Không có metadata tên tệp'}</dd></div><div><dt>Định dạng</dt><dd>{resumeType ?? 'Không xác định'}</dd></div><div><dt>Kích thước</dt><dd>{resumeSize == null ? 'Không xác định' : `${Math.ceil(resumeSize / 1024)} KB`}</dd></div><div><dt>Phiên bản</dt><dd>{data.resumeSnapshot.resumeVersion}</dd></div><div><dt>Snapshot lúc</dt><dd>{formatEmployerApplicationDate(data.resumeSnapshot.createdAt)}</dd></div></dl><Button type="button" onClick={() => resumeDownload.mutate()} disabled={resumeDownload.isPending}><Download /> {resumeDownload.isPending ? 'Đang tải CV...' : 'Tải CV snapshot'}</Button>{resumeDownload.isError && <p role="alert">{getErrorMessage(resumeDownload.error)}</p>}</> : <p>Không có CV snapshot.</p>}<small>File được tải qua API authenticated từ đúng storage key đã chụp khi ứng tuyển; không mở public storage URL.</small></section></article>
    </section>
    <section className="employer-application-history"><div className="employer-application-detail__heading"><FileClock /><h2>Lịch sử trạng thái</h2></div>{data.statusHistory.length ? <ol>{data.statusHistory.map((item) => <li key={item.id}><span /><div><strong>{employerApplicationStatusLabels[item.toStatus]}</strong><time dateTime={item.changedAtInstant ?? item.changedAt}>{formatEmployerApplicationDate(item.changedAtInstant ?? item.changedAt)}</time>{item.reasonDetail && <p>{item.reasonDetail}</p>}</div></li>)}</ol> : <p>Chưa có lịch sử trạng thái.</p>}</section>
    <section className="employer-application-transition"><div><h2>Cập nhật trạng thái</h2><p>Chỉ các transition hợp lệ từ trạng thái hiện tại được backend cho phép.</p></div>{transitions.length ? <form onSubmit={(event) => { event.preventDefault(); if (nextStatus && window.confirm(`Chuyển trạng thái sang ${employerApplicationStatusLabels[nextStatus]}?`)) transition.mutate() }}><label>Trạng thái tiếp theo<select value={nextStatus} onChange={(event) => setNextStatus(event.target.value as ApplicationStatus)} required><option value="">Chọn trạng thái</option>{transitions.map((status) => <option value={status} key={status}>{employerApplicationStatusLabels[status]}</option>)}</select></label><label>Ghi chú<textarea rows={3} maxLength={1000} value={reasonDetail} onChange={(event) => setReasonDetail(event.target.value)} /></label>{transition.isError && <p role="alert">{getErrorMessage(transition.error)}</p>}<Button type="submit" disabled={!nextStatus || transition.isPending}>{transition.isPending ? 'Đang cập nhật...' : 'Xác nhận chuyển trạng thái'}</Button></form> : <p className="employer-application-transition__terminal">Đây là trạng thái kết thúc; không có transition tiếp theo.</p>}</section>
  </main>
}
