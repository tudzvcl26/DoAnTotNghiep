import { ArrowLeft, BriefcaseBusiness, FileQuestion, RotateCw } from 'lucide-react'
import { Link } from 'react-router-dom'

export function JobDetailSkeleton() {
  return <div className="container job-detail-skeleton" aria-label="Đang tải thông tin việc làm"><div className="job-detail-skeleton__hero"><span /><div><strong /><i /><i /></div></div><div className="job-detail-skeleton__grid"><main><section /><section /><section /></main><aside><section /><section /></aside></div></div>
}

export function JobDetailError({ notFound, onRetry }: { notFound: boolean; onRetry: () => void }) {
  return <section className="job-detail-state"><div>{notFound ? <FileQuestion /> : <BriefcaseBusiness />}<span>{notFound ? '404' : 'Kết nối dữ liệu'}</span><h1>{notFound ? 'Không tìm thấy việc làm' : 'Không thể tải thông tin việc làm'}</h1><p>{notFound ? 'Vị trí này không tồn tại hoặc đã ngừng công khai.' : 'Dịch vụ dữ liệu có thể đang tạm dừng. Vui lòng thử lại sau.'}</p><div><Link to="/jobs"><ArrowLeft /> Quay lại danh sách việc làm</Link>{!notFound && <button type="button" onClick={onRetry}><RotateCw /> Thử lại</button>}</div></div></section>
}
