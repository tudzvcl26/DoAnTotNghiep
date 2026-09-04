import { Component, type ReactNode } from 'react'

type Props = { children: ReactNode; onReload?: () => void }

export class AppErrorBoundary extends Component<Props, { failed: boolean }> {
  state = { failed: false }
  static getDerivedStateFromError() { return { failed: true } }

  render() {
    if (!this.state.failed) return this.props.children
    return <main className="app-recovery" role="alert">
      <h1>Không thể mở giao diện lúc này</h1>
      <p>Ứng dụng có thể vừa được cập nhật hoặc kết nối bị gián đoạn. Hãy tải lại trang để tiếp tục.</p>
      <p>Dữ liệu đã lưu trên máy chủ không bị thay đổi. Bản nháp CV trong tab này được giữ nếu trình duyệt hỗ trợ.</p>
      <button type="button" onClick={() => this.props.onReload ? this.props.onReload() : window.location.reload()}>Tải lại ứng dụng</button>
    </main>
  }
}
