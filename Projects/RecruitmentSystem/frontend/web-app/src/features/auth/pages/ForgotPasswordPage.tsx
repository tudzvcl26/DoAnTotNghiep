import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import { getErrorMessage } from '../../../lib/api/error-adapter'
import { authApi } from '../auth.api'
import { AuthShell } from './AuthShell'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault(); setError(''); setMessage(''); setSubmitting(true)
    try {
      await authApi.forgotPassword(email)
      setMessage('Nếu tài khoản tồn tại, hướng dẫn đặt lại mật khẩu đã được chuẩn bị.')
    } catch (cause) { setError(getErrorMessage(cause)) } finally { setSubmitting(false) }
  }

  return <AuthShell title="Quên mật khẩu" description="Yêu cầu một liên kết đặt lại mật khẩu dùng một lần." footer={<Link to="/login">Quay lại đăng nhập</Link>}>
    <form className="login-form" onSubmit={submit}>
      {error && <div className="login-alert" role="alert">{error}</div>}
      {message && <div className="login-alert" role="status">{message}</div>}
      <div className="login-form__field"><label htmlFor="forgotEmail">Email</label><div className="login-input"><input id="forgotEmail" type="email" autoComplete="email" required value={email} onChange={(event) => setEmail(event.target.value)} /></div></div>
      <Button type="submit" fullWidth disabled={submitting}>{submitting ? 'Đang gửi...' : 'Gửi yêu cầu'}</Button>
    </form>
  </AuthShell>
}
