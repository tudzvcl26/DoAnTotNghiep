import { ArrowLeft, Building2, FileQuestion, RotateCw, SearchX } from 'lucide-react'
import { Link } from 'react-router-dom'

export function CompanyGridSkeleton() {
  return <div className="companies-grid" aria-label="Đang tải danh sách doanh nghiệp">{Array.from({ length: 6 }, (_, index) => <div className="company-card-skeleton" key={index}><span /><strong /><i /><i /><footer /></div>)}</div>
}

export function CompanyErrorState({ onRetry }: { onRetry: () => void }) {
  return <div className="companies-state"><Building2 /><h2>Không thể tải danh sách doanh nghiệp.</h2><p>Hệ thống dữ liệu có thể đang tạm dừng. Vui lòng thử lại sau ít phút.</p><button type="button" onClick={onRetry}><RotateCw size={16} /> Thử lại</button></div>
}

export function CompanyEmptyState({ keyword, onClear }: { keyword: string; onClear: () => void }) {
  return <div className="companies-state"><SearchX /><h2>Chưa có doanh nghiệp phù hợp.</h2><p>{keyword ? <>Không có kết quả cho “<strong>{keyword}</strong>”. Hãy thử một từ khóa khác.</> : 'Chưa có doanh nghiệp công khai trên hệ thống.'}</p>{keyword && <button type="button" onClick={onClear}>Xóa từ khóa</button>}</div>
}

export function CompanyDetailSkeleton() {
  return <div className="container company-detail-skeleton" aria-label="Đang tải thông tin doanh nghiệp"><div className="company-detail-skeleton__hero"><span /><div><strong /><i /><i /></div></div><div className="company-detail-skeleton__grid"><section /><aside /></div></div>
}

export function CompanyDetailError({ notFound, onRetry }: { notFound: boolean; onRetry: () => void }) {
  return <section className="company-detail-state"><div>{notFound ? <FileQuestion /> : <Building2 />}<span>{notFound ? '404' : 'Kết nối dữ liệu'}</span><h1>{notFound ? 'Không tìm thấy doanh nghiệp' : 'Không thể tải thông tin doanh nghiệp'}</h1><p>{notFound ? 'Doanh nghiệp này không tồn tại hoặc không còn được công khai.' : 'Dịch vụ dữ liệu có thể đang tạm dừng. Vui lòng thử lại sau.'}</p><div><Link to="/companies"><ArrowLeft /> Quay lại danh sách doanh nghiệp</Link>{!notFound && <button type="button" onClick={onRetry}><RotateCw /> Thử lại</button>}</div></div></section>
}
