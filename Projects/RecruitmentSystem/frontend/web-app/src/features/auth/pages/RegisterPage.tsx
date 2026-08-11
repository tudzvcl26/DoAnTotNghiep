import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowRight, LockKeyhole, Mail, Phone, UserRound } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import { getErrorMessage } from '../../../lib/api/error-adapter'
import { getRoleHome } from '../../../lib/auth/role-routing'
import { registerSchema, type RegisterFormValues } from '../../../lib/validation/auth.schemas'
import { useAuth } from '../auth-context'
import { AuthShell } from './AuthShell'

export function RegisterPage() {
  const [submitError, setSubmitError] = useState('')
  const { register: createAccount } = useAuth()
  const navigate = useNavigate()
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<RegisterFormValues>({ resolver: zodResolver(registerSchema) })

  const onSubmit = handleSubmit(async ({ confirmPassword: _, ...values }) => {
    setSubmitError('')
    try {
      const user = await createAccount(values)
      navigate(getRoleHome(user.roles), { replace: true })
    } catch (error) {
      setSubmitError(getErrorMessage(error))
    }
  })

  return (
    <AuthShell title="Bắt đầu sự nghiệp mới" description="Tạo tài khoản ứng viên để xây dựng hồ sơ của bạn." footer={<>Đã có tài khoản? <Link to="/login">Đăng nhập</Link></>}>
      <form className="auth-form auth-form--register" onSubmit={onSubmit} noValidate>
        {submitError && <div className="alert-error" role="alert">{submitError}</div>}
        <div className="form-field auth-form__wide"><label htmlFor="fullName">Họ và tên</label><div className="auth-input"><UserRound size={18} /><input id="fullName" className="form-input" autoComplete="name" placeholder="Nguyễn Minh Anh" {...register('fullName')} /></div>{errors.fullName && <p className="form-error">{errors.fullName.message}</p>}</div>
        <div className="form-field"><label htmlFor="registerEmail">Email</label><div className="auth-input"><Mail size={18} /><input id="registerEmail" className="form-input" type="email" autoComplete="email" placeholder="ban@example.com" {...register('email')} /></div>{errors.email && <p className="form-error">{errors.email.message}</p>}</div>
        <div className="form-field"><label htmlFor="phone">Số điện thoại</label><div className="auth-input"><Phone size={18} /><input id="phone" className="form-input" autoComplete="tel" placeholder="09xx xxx xxx" {...register('phone')} /></div>{errors.phone && <p className="form-error">{errors.phone.message}</p>}</div>
        <div className="form-field"><label htmlFor="registerPassword">Mật khẩu</label><div className="auth-input"><LockKeyhole size={18} /><input id="registerPassword" className="form-input" type="password" autoComplete="new-password" placeholder="Tối thiểu 8 ký tự" {...register('password')} /></div>{errors.password && <p className="form-error">{errors.password.message}</p>}</div>
        <div className="form-field"><label htmlFor="confirmPassword">Xác nhận mật khẩu</label><div className="auth-input"><LockKeyhole size={18} /><input id="confirmPassword" className="form-input" type="password" autoComplete="new-password" placeholder="Nhập lại mật khẩu" {...register('confirmPassword')} /></div>{errors.confirmPassword && <p className="form-error">{errors.confirmPassword.message}</p>}</div>
        <div className="auth-form__wide"><Button type="submit" size="lg" fullWidth disabled={isSubmitting}>{isSubmitting ? 'Đang tạo tài khoản...' : <>Tạo tài khoản <ArrowRight size={18} /></>}</Button><p className="auth-form__terms">Bằng việc đăng ký, bạn đồng ý với điều khoản và chính sách bảo mật của nền tảng.</p></div>
      </form>
    </AuthShell>
  )
}
