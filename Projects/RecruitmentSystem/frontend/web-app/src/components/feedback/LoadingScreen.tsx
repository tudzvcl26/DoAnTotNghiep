import { LoaderCircle } from 'lucide-react'
import './loading-screen.css'

export function LoadingScreen() {
  return (
    <div className="loading-screen" role="status" aria-live="polite">
      <LoaderCircle size={28} aria-hidden="true" />
      <span>Đang chuẩn bị không gian của bạn...</span>
    </div>
  )
}
