import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import { getErrorMessage } from '../../../lib/api/error-adapter'
import { authApi } from '../auth.api'
import { AuthShell } from './AuthShell'

export function VerifyEmailPage() {
  const [params, setParams] = useSearchParams()
  const [token, setToken] = useState(params.get('token') ?? '')
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function verify(event: FormEvent) {
    event.preventDefault(); setError(''); setMessage(''); setSubmitting(true)
    try { await authApi.verifyEmail(token); setToken(''); setParams({}, { replace: true }); setMessage('Email đã được xác minh thành công.') }
    catch (cause) { setError(getErrorMessage(cause)) } finally { setSubmitting(false) }
  }

  async function resend(event: FormEvent) {
    event.preventDefault(); setError(''); setMessage(''); setSubmitting(true)
    try { await authApi.resendVerification(email); setMessage('Nếu tài khoản đủ điều kiện, hướng dẫn xác minh đã được chuẩn bị.') }
    catch (cause) { setError(getErrorMessage(cause)) } finally { setSubmitting(false) }
  }

  return <AuthShell title="Xác minh email" description="Xác minh địa chỉ email hoặc yêu cầu lại token mới." footer={<Link to="/login">Đăng nhập</Link>}>
    {error && <div className="login-alert" role="alert">{error}</div>}
    {message && <div className="login-alert" role="status">{message}</div>}
    <form className="login-form" onSubmit={verify}>
      <div className="login-form__field"><label htmlFor="verificationToken">Verification token</label><div className="login-input"><input id="verificationToken" required value={token} onChange={(event) => setToken(event.target.value)} /></div></div>
      <Button type="submit" fullWidth disabled={submitting}>{submitting ? 'Đang xác minh...' : 'Xác minh email'}</Button>
    </form>
    <form className="login-form" onSubmit={resend}>
      <div className="login-form__field"><label htmlFor="resendEmail">Gửi lại cho email</label><div className="login-input"><input id="resendEmail" type="email" required value={email} onChange={(event) => setEmail(event.target.value)} /></div></div>
      <Button type="submit" variant="secondary" fullWidth disabled={submitting}>Gửi lại xác minh</Button>
    </form>
  </AuthShell>
}
