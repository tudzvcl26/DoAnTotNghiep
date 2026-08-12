import { ChevronLeft, ChevronRight } from 'lucide-react'

function visiblePages(page: number, totalPages: number) {
  const start = Math.max(0, Math.min(page - 2, totalPages - 5))
  return Array.from({ length: Math.min(5, totalPages) }, (_, index) => start + index)
}

export function JobPagination({ page, totalPages, hasPrevious, hasNext, onPageChange }: { page: number; totalPages: number; hasPrevious: boolean; hasNext: boolean; onPageChange: (page: number) => void }) {
  if (totalPages <= 1) return null
  return <nav className="jobs-pagination" aria-label="Phân trang việc làm"><button type="button" disabled={!hasPrevious} onClick={() => onPageChange(page - 1)}><ChevronLeft size={17} /> Trước</button><div>{visiblePages(page, totalPages).map((value) => <button type="button" key={value} aria-current={value === page ? 'page' : undefined} onClick={() => onPageChange(value)}>{value + 1}</button>)}</div><button type="button" disabled={!hasNext} onClick={() => onPageChange(page + 1)}>Sau <ChevronRight size={17} /></button></nav>
}
