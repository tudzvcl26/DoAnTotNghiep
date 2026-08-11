import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowRight, LockKeyhole, Mail } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import { useAuth } from '../auth-context'
import { getErrorMessage } from '../../../lib/api/error-adapter'
import { getRoleHome } from '../../../lib/auth/role-routing'
import { loginSchema, type LoginFormValues } from '../../../lib/validation/auth.schemas'
import { AuthShell } from './AuthShell'

export function LoginPage() {
  const [submitError, setSubmitError] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) })

  const onSubmit = handleSubmit(async (values) => {
    setSubmitError('')
    try {
      const user = await login(values)
      const returnTo = new URLSearchParams(location.search).get('returnTo')
      navigate(returnTo?.startsWith('/') ? returnTo : getRoleHome(user.roles), { replace: true })
    } catch (error) {
      setSubmitError(getErrorMessage(error))
    }
  })

  return (
    <AuthShell title="Chào mừng trở lại" description="Đăng nhập để tiếp tục hành trình nghề nghiệp." footer={<>Chưa có tài khoản? <Link to="/register">Đăng ký miễn phí</Link></>}>
      <form className="auth-form" onSubmit={onSubmit} noValidate>
        {submitError && <div className="alert-error" role="alert">{submitError}</div>}
        <div className="form-field"><label htmlFor="email">Email</label><div className="auth-input"><Mail size={18} /><input id="email" className="form-input" type="email" autoComplete="email" placeholder="ban@example.com" {...register('email')} /></div>{errors.email && <p className="form-error">{errors.email.message}</p>}</div>
        <div className="form-field"><label htmlFor="password">Mật khẩu</label><div className="auth-input"><LockKeyhole size={18} /><input id="password" className="form-input" type="password" autoComplete="current-password" placeholder="Nhập mật khẩu" {...register('password')} /></div>{errors.password && <p className="form-error">{errors.password.message}</p>}</div>
        <Button type="submit" size="lg" fullWidth disabled={isSubmitting}>{isSubmitting ? 'Đang đăng nhập...' : <>Đăng nhập <ArrowRight size={18} /></>}</Button>
      </form>
    </AuthShell>
  )
}
