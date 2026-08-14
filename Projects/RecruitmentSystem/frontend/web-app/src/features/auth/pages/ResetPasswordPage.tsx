import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import { getErrorMessage } from '../../../lib/api/error-adapter'
import { authApi } from '../auth.api'
import { AuthShell } from './AuthShell'

export function ResetPasswordPage() {
  const [params, setParams] = useSearchParams()
  const [token, setToken] = useState(params.get('token') ?? '')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault(); setError(''); setMessage('')
    if (password !== confirm) { setError('Mật khẩu xác nhận không khớp.'); return }
    setSubmitting(true)
    try {
      await authApi.resetPassword(token, password)
      setToken(''); setPassword(''); setConfirm(''); setParams({}, { replace: true })
      setMessage('Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.')
    }
    catch (cause) { setError(getErrorMessage(cause)) } finally { setSubmitting(false) }
  }

  return <AuthShell title="Đặt lại mật khẩu" description="Token chỉ dùng được một lần và sẽ hết hạn tự động." footer={<Link to="/login">Đăng nhập</Link>}>
    <form className="login-form" onSubmit={submit}>
      {error && <div className="login-alert" role="alert">{error}</div>}
      {message && <div className="login-alert" role="status">{message}</div>}
      <div className="login-form__field"><label htmlFor="resetToken">Reset token</label><div className="login-input"><input id="resetToken" required value={token} onChange={(event) => setToken(event.target.value)} /></div></div>
      <div className="login-form__field"><label htmlFor="newPassword">Mật khẩu mới</label><div className="login-input"><input id="newPassword" type="password" minLength={8} maxLength={100} required value={password} onChange={(event) => setPassword(event.target.value)} /></div></div>
      <div className="login-form__field"><label htmlFor="confirmNewPassword">Xác nhận mật khẩu</label><div className="login-input"><input id="confirmNewPassword" type="password" required value={confirm} onChange={(event) => setConfirm(event.target.value)} /></div></div>
      <Button type="submit" fullWidth disabled={submitting}>{submitting ? 'Đang cập nhật...' : 'Đặt lại mật khẩu'}</Button>
    </form>
  </AuthShell>
}
