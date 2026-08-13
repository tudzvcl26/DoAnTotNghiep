import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, BriefcaseBusiness, CircleAlert, FileText, RefreshCw, RotateCcw } from 'lucide-react'
import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getErrorMessage, normalizeApiError } from '../../lib/api/error-adapter'
import { useAuth } from '../auth/auth-context'
import { getCompanyById } from '../companies/companies.api'
import { getJobById } from '../jobs/jobs.api'
import { applicationStatusLabels, canWithdrawApplication, formatApplicationDate } from './application-presenter'
import { getApplicationById, withdrawApplication } from './applications.api'
import './applications-page.css'

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

export function ApplicationDetailPage() {
  const { applicationId = '' } = useParams()
  const { currentUser } = useAuth()
  const queryClient = useQueryClient()
  const [confirmWithdraw, setConfirmWithdraw] = useState(false)
  const validId = UUID_PATTERN.test(applicationId)
  const application = useQuery({ queryKey: ['candidate-application', currentUser?.id, applicationId], queryFn: () => getApplicationById(applicationId), enabled: validId, retry: false })
  const job = useQuery({ queryKey: ['job', application.data?.jobId], queryFn: () => getJobById(application.data!.jobId), enabled: Boolean(application.data?.jobId), retry: false })
  const company = useQuery({ queryKey: ['company', job.data?.companyId], queryFn: () => getCompanyById(job.data!.companyId), enabled: Boolean(job.data?.companyId), retry: false })
  const withdraw = useMutation({
    mutationFn: () => withdrawApplication(applicationId),
    onSuccess: async (updated) => {
      queryClient.setQueryData(['candidate-application', currentUser?.id, applicationId], updated)
      setConfirmWithdraw(false)
      await queryClient.invalidateQueries({ queryKey: ['candidate-applications', currentUser?.id] })
    },
  })

  if (!validId) return <main className="applications-page"><div className="applications-state applications-state--error"><CircleAlert /><h1>Không tìm thấy đơn ứng tuyển</h1><Link to="/candidate/applications">Quay lại danh sách</Link></div></main>
  if (application.isPending) return <main className="applications-page"><div className="applications-state"><span className="applications-loading" /><p>Đang tải chi tiết đơn...</p></div></main>
  if (application.isError) {
    const status = normalizeApiError(application.error).status
    return <main className="applications-page"><div className="applications-state applications-state--error" role="alert"><CircleAlert /><h1>{status === 403 ? 'Bạn không có quyền xem đơn này' : status === 404 ? 'Không tìm thấy đơn ứng tuyển' : 'Chưa thể tải chi tiết đơn'}</h1><p>{getErrorMessage(application.error)}</p><button type="button" onClick={() => void application.refetch()}><RefreshCw /> Thử lại</button><Link to="/candidate/applications">Quay lại danh sách</Link></div></main>
  }

  const data = application.data
  return <main className="applications-page application-detail">
    <Link className="application-detail__back" to="/candidate/applications"><ArrowLeft /> Đơn ứng tuyển của tôi</Link>
    <header className="applications-page__header"><span>Chi tiết đơn ứng tuyển</span><h1>{job.data?.title ?? `Đơn ${data.id.slice(0, 8)}`}</h1>{company.data && <p>{company.data.name}</p>}</header>
    <section className="application-detail__grid">
      <article className="application-detail__card">
        <div className="application-detail__title"><BriefcaseBusiness /><h2>Thông tin đơn</h2></div>
        <dl><div><dt>Mã đơn</dt><dd>{data.id}</dd></div><div><dt>Mã công việc</dt><dd>{data.jobId}</dd></div><div><dt>Trạng thái</dt><dd><span className={`candidate-status-chip candidate-status-chip--${data.status.toLowerCase()}`}>{applicationStatusLabels[data.status]}</span></dd></div><div><dt>Ngày ứng tuyển</dt><dd>{formatApplicationDate(data.appliedAt)}</dd></div><div><dt>Cập nhật gần nhất</dt><dd>{formatApplicationDate(data.updatedAt)}</dd></div></dl>
      </article>
      <article className="application-detail__card">
        <div className="application-detail__title"><FileText /><h2>Hồ sơ đã gửi</h2></div>
        <div className="application-detail__cover"><h3>Thư ứng tuyển</h3><p>{data.coverLetter || 'Không có thư ứng tuyển.'}</p></div>
        {data.resumeSnapshot && <p className="application-detail__snapshot"><strong>CV snapshot:</strong> {data.resumeSnapshot.resumeVersion} · {formatApplicationDate(data.resumeSnapshot.createdAt)}</p>}
      </article>
    </section>
    {data.statusHistory?.length > 0 && <section className="application-detail__card"><div className="application-detail__title"><RefreshCw /><h2>Lịch sử trạng thái</h2></div><ol className="application-history">{data.statusHistory.map((item) => <li key={item.id}><span /><div><strong>{applicationStatusLabels[item.toStatus]}</strong><small>{formatApplicationDate(item.changedAt)}</small>{item.reasonDetail && <p>{item.reasonDetail}</p>}</div></li>)}</ol></section>}
    {canWithdrawApplication(data.status) && <section className="application-withdraw"><div><h2>Rút đơn ứng tuyển</h2><p>Sau khi rút, đơn không thể tiếp tục trong quy trình tuyển dụng.</p></div>{!confirmWithdraw ? <button type="button" onClick={() => setConfirmWithdraw(true)}><RotateCcw /> Rút đơn</button> : <div className="application-withdraw__confirm"><span>Bạn chắc chắn muốn rút đơn?</span><button type="button" onClick={() => setConfirmWithdraw(false)} disabled={withdraw.isPending}>Không</button><button type="button" onClick={() => withdraw.mutate()} disabled={withdraw.isPending}>{withdraw.isPending ? 'Đang xử lý...' : 'Xác nhận rút'}</button></div>}{withdraw.isError && <p className="application-withdraw__error" role="alert">{getErrorMessage(withdraw.error)}</p>}</section>}
  </main>
}
