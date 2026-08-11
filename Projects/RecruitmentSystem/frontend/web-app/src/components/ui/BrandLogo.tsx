import { Link } from 'react-router-dom'
import './brand-logo.css'

export function BrandLogo() {
  return (
    <Link className="brand-logo" to="/" aria-label="RecruitmentSystem - Trang chủ">
      <span className="brand-logo__mark" aria-hidden="true">
        <span />
        <span />
      </span>
      <span className="brand-logo__wordmark">
        Recruit<span>ment</span>
      </span>
    </Link>
  )
}
