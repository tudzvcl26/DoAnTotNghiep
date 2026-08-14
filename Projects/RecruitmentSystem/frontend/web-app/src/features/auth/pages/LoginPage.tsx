import { zodResolver } from '@hookform/resolvers/zod'
import { AlertCircle, ArrowLeft, LoaderCircle, LockKeyhole, Sparkles, UserRound } from 'lucide-react'
import { useEffect, useRef, useState, type AnimationEvent as ReactAnimationEvent, type CSSProperties, type PointerEvent as ReactPointerEvent } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { Button } from '../../../components/ui/Button'
import { useAuth } from '../auth-context'
import { getErrorMessage } from '../../../lib/api/error-adapter'
import { getRoleHome } from '../../../lib/auth/role-routing'
import { loginSchema, type LoginFormValues } from '../../../lib/validation/auth.schemas'
import './auth-pages.css'

const MAX_PULL_DISTANCE = 58
const PULL_TOGGLE_THRESHOLD = 34

export function LoginPage() {
  const [submitError, setSubmitError] = useState('')
  const [isLampOn, setIsLampOn] = useState(false)
  const [pullDistance, setPullDistance] = useState(0)
  const [isAutoPulling, setIsAutoPulling] = useState(false)
  const hasAutoPulledRef = useRef(false)
  const pullStartY = useRef<number | null>(null)
  const pullDistanceRef = useRef(0)
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) })

  useEffect(() => {
    if (hasAutoPulledRef.current) return
    hasAutoPulledRef.current = true
    let autoPullStarted = false

    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      setIsLampOn(true)
      return
    }

    const autoPullTimer = window.setTimeout(() => {
      autoPullStarted = true
      setIsAutoPulling(true)
    }, 620)
    return () => {
      window.clearTimeout(autoPullTimer)
      if (!autoPullStarted) hasAutoPulledRef.current = false
    }
  }, [])

  const finishAutoPull = (event: ReactAnimationEvent<HTMLButtonElement>) => {
    if (!isAutoPulling || event.animationName !== 'login-cord-auto-pull-knob') return
    setIsAutoPulling(false)
    setIsLampOn(true)
  }

  const handlePullStart = (event: ReactPointerEvent<HTMLButtonElement>) => {
    if (event.button !== 0 || isAutoPulling) return
    pullStartY.current = event.clientY
    pullDistanceRef.current = 0
    setPullDistance(0)
    event.currentTarget.setPointerCapture(event.pointerId)
  }

  const handlePullMove = (event: ReactPointerEvent<HTMLButtonElement>) => {
    if (pullStartY.current === null) return
    const distance = Math.min(MAX_PULL_DISTANCE, Math.max(0, event.clientY - pullStartY.current))
    pullDistanceRef.current = distance
    setPullDistance(distance)
  }

  const handlePullEnd = (event: ReactPointerEvent<HTMLButtonElement>, cancelled = false) => {
    if (pullStartY.current === null) return
    if (!cancelled && pullDistanceRef.current >= PULL_TOGGLE_THRESHOLD) {
      setIsLampOn((current) => !current)
    }
    pullStartY.current = null
    pullDistanceRef.current = 0
    setPullDistance(0)
    if (event.currentTarget.hasPointerCapture(event.pointerId)) {
      event.currentTarget.releasePointerCapture(event.pointerId)
    }
  }

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
    <section className={`login-page${isLampOn ? ' login-page--lit' : ''}`}>
      <div className="login-page__grain" aria-hidden="true" />

      <header className="login-page__topbar">
        <Link className="login-brand" to="/" aria-label="RecruitmentSystem - Trang chủ">
          <span className="login-brand__mark"><Sparkles size={16} aria-hidden="true" /></span>
          <span>Recruitment<span>System</span></span>
        </Link>
        <Link className="login-page__back" to="/"><ArrowLeft size={16} aria-hidden="true" /> Back to home</Link>
      </header>

      <div className="login-page__layout">
        <div className="login-visual">
          <div className="login-visual__halo" />
          <div className="desk-lamp">
            <div className="desk-lamp__light" aria-hidden="true" />
            <div className="desk-lamp__shade" aria-hidden="true"><span /></div>
            <div className="desk-lamp__stem" aria-hidden="true" />
            <button
              className={`desk-lamp__switch${pullDistance > 0 ? ' desk-lamp__switch--pulling' : ''}${isAutoPulling ? ' desk-lamp__switch--auto' : ''}`}
              type="button"
              aria-label={isLampOn ? 'Kéo dây xuống để tắt đèn' : 'Kéo dây xuống để bật đèn'}
              aria-pressed={isLampOn}
              style={{ '--pull-distance': `${pullDistance}px` } as CSSProperties}
              onPointerDown={handlePullStart}
              onPointerMove={handlePullMove}
              onPointerUp={(event) => handlePullEnd(event)}
              onPointerCancel={(event) => handlePullEnd(event, true)}
              onAnimationEnd={finishAutoPull}
              onKeyDown={(event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                  event.preventDefault()
                  setIsLampOn((current) => !current)
                }
              }}
            >
              <span />
            </button>
            <div className="desk-lamp__base" aria-hidden="true"><span /></div>
          </div>
        </div>

        <div className="login-card-wrap">
          <article className="login-card">
            <div className="login-card__heading">
              <span className="login-card__kicker">Member access</span>
              <h1>Welcome</h1>
              <p>Sign in to continue your career journey.</p>
            </div>

            <form className="login-form" onSubmit={onSubmit} noValidate>
              {submitError && (
                <div className="login-alert" role="alert" aria-live="polite">
                  <AlertCircle size={17} aria-hidden="true" /><span>{submitError}</span>
                </div>
              )}

              <div className="login-form__field login-form__field--username">
                <label htmlFor="email">Username</label>
                <div className="login-input">
                  <UserRound size={18} aria-hidden="true" />
                  <input
                    id="email"
                    type="email"
                    autoComplete="email"
                    placeholder="Enter name"
                    aria-invalid={Boolean(errors.email)}
                    aria-describedby={errors.email ? 'email-error' : undefined}
                    {...register('email')}
                  />
                </div>
                {errors.email && <p id="email-error" className="login-form__error">{errors.email.message}</p>}
              </div>

              <div className="login-form__field login-form__field--password">
                <label htmlFor="password">Password</label>
                <div className="login-input">
                  <LockKeyhole size={18} aria-hidden="true" />
                  <input
                    id="password"
                    type="password"
                    autoComplete="current-password"
                    placeholder="Enter Password"
                    aria-invalid={Boolean(errors.password)}
                    aria-describedby={errors.password ? 'password-error' : undefined}
                    {...register('password')}
                  />
                </div>
                {errors.password && <p id="password-error" className="login-form__error">{errors.password.message}</p>}
                <Link to="/forgot-password">Forgot password?</Link>
              </div>

              <Button className="login-submit" type="submit" size="lg" fullWidth disabled={isSubmitting}>
                {isSubmitting ? <><LoaderCircle className="login-submit__spinner" size={18} aria-hidden="true" /> Signing in...</> : 'Sign In'}
              </Button>
            </form>

            <div className="login-card__footer">New here? <Link to="/register">Create an account</Link> · <Link to="/verify-email">Verify email</Link></div>
          </article>
          <span className="login-card-wrap__note">Secure gateway · Private session</span>
        </div>
      </div>
    </section>
  )
}
