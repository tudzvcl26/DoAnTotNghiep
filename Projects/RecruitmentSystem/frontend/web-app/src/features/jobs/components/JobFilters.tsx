import { Filter, RotateCcw, SearchCheck } from 'lucide-react'

export function JobFilters({ keyword, size, onClear, onSizeChange }: { keyword: string; size: number; onClear: () => void; onSizeChange: (size: number) => void }) {
  return (
    <aside className="jobs-filter" aria-label="Bộ lọc việc làm">
      <div className="jobs-filter__heading"><span><Filter size={18} /> Bộ lọc</span>{keyword && <button type="button" onClick={onClear}><RotateCcw size={14} /> Xóa</button>}</div>
      <div className="jobs-filter__group"><h2>Từ khóa hiện tại</h2>{keyword ? <div className="jobs-filter__keyword"><SearchCheck size={16} /><span>{keyword}</span></div> : <p>Đang hiển thị tất cả việc làm công khai.</p>}</div>
      <div className="jobs-filter__group"><label htmlFor="jobs-page-size">Số kết quả mỗi trang</label><select id="jobs-page-size" value={size} onChange={(event) => onSizeChange(Number(event.target.value))}><option value="6">6 việc làm</option><option value="12">12 việc làm</option><option value="24">24 việc làm</option></select></div>
      <div className="jobs-filter__notice"><strong>Filter theo dữ liệu thật</strong><p>API hiện hỗ trợ từ khóa, phân trang và sắp xếp. Các bộ lọc địa điểm, lương và ngành nghề sẽ được mở khi backend hỗ trợ.</p></div>
    </aside>
  )
}
