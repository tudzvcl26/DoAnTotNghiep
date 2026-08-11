import type { LucideIcon } from 'lucide-react'
import './public-page-intro.css'

type PublicPageIntroProps = {
  eyebrow: string
  title: string
  description: string
  icon: LucideIcon
}

export function PublicPageIntro({ eyebrow, title, description, icon: Icon }: PublicPageIntroProps) {
  return (
    <section className="public-page-intro">
      <div className="container public-page-intro__inner">
        <div className="public-page-intro__icon"><Icon size={27} /></div>
        <span className="page-eyebrow">{eyebrow}</span>
        <h1 className="page-title">{title}</h1>
        <p className="page-description">{description}</p>
      </div>
    </section>
  )
}
