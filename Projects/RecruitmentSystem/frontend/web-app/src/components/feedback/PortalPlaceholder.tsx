import type { LucideIcon } from 'lucide-react'
import { ArrowRight } from 'lucide-react'
import { ButtonLink } from '../ui/Button'
import './portal-placeholder.css'

type PortalPlaceholderProps = {
  eyebrow: string
  title: string
  description: string
  icon: LucideIcon
}

export function PortalPlaceholder({ eyebrow, title, description, icon: Icon }: PortalPlaceholderProps) {
  return (
    <section className="portal-placeholder page-section">
      <div className="container">
        <div className="portal-placeholder__card">
          <div className="portal-placeholder__icon"><Icon size={30} aria-hidden="true" /></div>
          <div>
            <span className="page-eyebrow">{eyebrow}</span>
            <h1>{title}</h1>
            <p>{description}</p>
          </div>
          <ButtonLink to="/" variant="secondary">
            Về trang chủ <ArrowRight size={17} aria-hidden="true" />
          </ButtonLink>
        </div>
      </div>
    </section>
  )
}
