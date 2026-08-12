import { BriefcaseBusiness, RotateCw, SearchX } from 'lucide-react'

export function JobResultsSkeleton() {
  return <div className="jobs-results-list" aria-label="Đang tải danh sách việc làm">{Array.from({ length: 6 }, (_, index) => <div className="jobs-card-skeleton" key={index}><span /><strong /><i /><i /><footer /></div>)}</div>
}

export function JobErrorState({ onRetry }: { onRetry: () => void }) {
  return <div className="jobs-state"><BriefcaseBusiness /><h2>Không thể tải danh sách việc làm</h2><p>Hệ thống dữ liệu có thể đang tạm dừng. Vui lòng thử lại sau ít phút.</p><button type="button" onClick={onRetry}><RotateCw size={16} /> Thử lại</button></div>
}

export function JobEmptyState({ keyword, onClear }: { keyword: string; onClear: () => void }) {
  return <div className="jobs-state"><SearchX /><h2>Không tìm thấy việc làm phù hợp</h2><p>{keyword ? <>Không có kết quả cho “<strong>{keyword}</strong>”. Hãy thử một từ khóa khác.</> : 'Chưa có việc làm công khai trên hệ thống.'}</p>{keyword && <button type="button" onClick={onClear}>Xóa từ khóa</button>}</div>
}
