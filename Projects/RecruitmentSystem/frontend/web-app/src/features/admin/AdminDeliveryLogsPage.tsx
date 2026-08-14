import { useQuery } from '@tanstack/react-query'
import { AlertCircle, ChevronLeft, ChevronRight, FileClock, Inbox, RefreshCw } from 'lucide-react'
import { useState } from 'react'
import { getErrorMessage } from '../../lib/api/error-adapter'
import { adminDeliveryLogsKey, getNotificationDeliveryLogs } from './admin.api'
import type { NotificationDeliveryStatus } from './admin.types'

const statuses: NotificationDeliveryStatus[] = ['PENDING','SENT','FAILED','SKIPPED']
export function AdminDeliveryLogsPage() {
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState<NotificationDeliveryStatus | ''>('')
  const logs = useQuery({ queryKey: [...adminDeliveryLogsKey, { page, status }], queryFn: () => getNotificationDeliveryLogs({ page, size: 20, status: status || undefined }), placeholderData: (old) => old })
  return <main className="admin-page"><header className="admin-page__hero"><div><span>Delivery audit</span><h1>Notification delivery logs</h1><p>Theo dõi trạng thái delivery read-only từ Notification Service.</p></div><FileClock /></header><section className="admin-toolbar"><label>Trạng thái<select value={status} onChange={(e) => { setStatus(e.target.value as NotificationDeliveryStatus | ''); setPage(0) }}><option value="">Tất cả</option>{statuses.map((value) => <option key={value}>{value}</option>)}</select></label></section>
    {logs.isPending && <div className="admin-skeleton"><span /><span /></div>}{logs.isError && <section className="admin-state"><AlertCircle /><h2>Không thể tải delivery logs</h2><p>{getErrorMessage(logs.error)}</p><button className="admin-button admin-button--secondary" onClick={() => void logs.refetch()}><RefreshCw />Thử lại</button></section>}{logs.data?.content.length === 0 && <section className="admin-state"><Inbox /><h2>Chưa có delivery log</h2><p>Không có bản ghi phù hợp với trạng thái đã chọn.</p></section>}
    {logs.data && logs.data.content.length > 0 && <><div className="admin-table-wrap"><table className="admin-table"><thead><tr><th>Status</th><th>Channel</th><th>User</th><th>Notification</th><th>Attempt</th><th>Attempted</th><th>Delivered</th><th>Error</th></tr></thead><tbody>{logs.data.content.map((item) => <tr key={item.id}><td><span className={`admin-badge admin-badge--${item.status.toLowerCase()}`}>{item.status}</span></td><td>{item.channel}</td><td>{item.userId}</td><td>{item.notificationId}</td><td>{item.attemptNumber}</td><td>{item.attemptedAt ? new Date(item.attemptedAt).toLocaleString('vi-VN') : '—'}</td><td>{item.deliveredAt ? new Date(item.deliveredAt).toLocaleString('vi-VN') : '—'}</td><td title={item.errorMessage ?? ''}>{item.errorMessage ?? '—'}</td></tr>)}</tbody></table></div><nav className="admin-pagination"><button className="admin-button admin-button--secondary" disabled={!logs.data.hasPrevious} onClick={() => setPage(page - 1)}><ChevronLeft />Trước</button><span>Trang {logs.data.page + 1}/{Math.max(1, logs.data.totalPages)} · {logs.data.totalElements} logs</span><button className="admin-button admin-button--secondary" disabled={!logs.data.hasNext} onClick={() => setPage(page + 1)}>Sau<ChevronRight /></button></nav></>}
  </main>
}
