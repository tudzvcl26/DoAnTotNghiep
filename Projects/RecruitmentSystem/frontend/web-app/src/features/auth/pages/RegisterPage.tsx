import { zodResolver } from '@hookform/resolvers/zod'
import { AlertCircle, ArrowLeft, ArrowRight, LoaderCircle, LockKeyhole, Mail, Phone, Sparkles, UserRound } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import { getErrorMessage } from '../../../lib/api/error-adapter'
import { getRoleHome } from '../../../lib/auth/role-routing'
import { registerSchema, type RegisterFormValues } from '../../../lib/validation/auth.schemas'
import { useAuth } from '../auth-context'
import './auth-pages.css'

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
    <section className="register-page">
      <div className="register-page__grain" aria-hidden="true" />

      <header className="register-page__topbar">
        <Link className="login-brand" to="/" aria-label="RecruitmentSystem - Trang chủ">
          <span className="login-brand__mark"><Sparkles size={16} aria-hidden="true" /></span>
          <span>Recruitment<span>System</span></span>
        </Link>
        <Link className="login-page__back" to="/"><ArrowLeft size={16} aria-hidden="true" /> Back to home</Link>
      </header>

      <main className="register-page__main">
        <article className="register-card">
          <div className="register-card__heading">
            <span className="register-card__kicker">Candidate account</span>
            <h1>Create your account</h1>
            <p>Start your career journey with RecruitmentSystem.</p>
          </div>

          <form className="register-form" onSubmit={onSubmit} noValidate>
            {submitError && (
              <div className="login-alert register-form__wide" role="alert" aria-live="polite">
                <AlertCircle size={17} aria-hidden="true" /><span>{submitError}</span>
              </div>
            )}

            <div className="register-field register-form__wide">
              <label htmlFor="fullName">Full name</label>
              <div className="register-input"><UserRound size={18} aria-hidden="true" /><input id="fullName" autoComplete="name" placeholder="Nguyễn Minh Anh" aria-invalid={Boolean(errors.fullName)} aria-describedby={errors.fullName ? 'fullName-error' : undefined} {...register('fullName')} /></div>
              {errors.fullName && <p id="fullName-error" className="login-form__error">{errors.fullName.message}</p>}
            </div>

            <div className="register-field">
              <label htmlFor="registerEmail">Email</label>
              <div className="register-input"><Mail size={18} aria-hidden="true" /><input id="registerEmail" type="email" autoComplete="email" placeholder="ban@example.com" aria-invalid={Boolean(errors.email)} aria-describedby={errors.email ? 'registerEmail-error' : undefined} {...register('email')} /></div>
              {errors.email && <p id="registerEmail-error" className="login-form__error">{errors.email.message}</p>}
            </div>

            <div className="register-field">
              <label htmlFor="phone">Phone number</label>
              <div className="register-input"><Phone size={18} aria-hidden="true" /><input id="phone" autoComplete="tel" placeholder="09xx xxx xxx" aria-invalid={Boolean(errors.phone)} aria-describedby={errors.phone ? 'phone-error' : undefined} {...register('phone')} /></div>
              {errors.phone && <p id="phone-error" className="login-form__error">{errors.phone.message}</p>}
            </div>

            <div className="register-field">
              <label htmlFor="registerPassword">Password</label>
              <div className="register-input"><LockKeyhole size={18} aria-hidden="true" /><input id="registerPassword" type="password" autoComplete="new-password" placeholder="At least 8 characters" aria-invalid={Boolean(errors.password)} aria-describedby={errors.password ? 'registerPassword-error' : undefined} {...register('password')} /></div>
              {errors.password && <p id="registerPassword-error" className="login-form__error">{errors.password.message}</p>}
            </div>

            <div className="register-field">
              <label htmlFor="confirmPassword">Confirm password</label>
              <div className="register-input"><LockKeyhole size={18} aria-hidden="true" /><input id="confirmPassword" type="password" autoComplete="new-password" placeholder="Enter your password again" aria-invalid={Boolean(errors.confirmPassword)} aria-describedby={errors.confirmPassword ? 'confirmPassword-error' : undefined} {...register('confirmPassword')} /></div>
              {errors.confirmPassword && <p id="confirmPassword-error" className="login-form__error">{errors.confirmPassword.message}</p>}
            </div>

            <div className="register-form__wide register-form__actions">
              <Button className="register-submit" type="submit" size="lg" fullWidth disabled={isSubmitting}>
                {isSubmitting ? <><LoaderCircle className="login-submit__spinner" size={18} aria-hidden="true" /> Creating account...</> : <>Create account <ArrowRight size={18} aria-hidden="true" /></>}
              </Button>
              <p className="register-form__terms">By signing up, you agree to our terms and privacy policy.</p>
            </div>
          </form>

          <div className="register-card__footer">Already have an account? <Link to="/login">Sign in</Link></div>
        </article>
      </main>
    </section>
  )
}
