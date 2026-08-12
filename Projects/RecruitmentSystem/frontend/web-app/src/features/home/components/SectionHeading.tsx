import { ArrowRight } from 'lucide-react'
import { Link } from 'react-router-dom'

export function SectionHeading({ eyebrow, title, description, to, linkLabel }: { eyebrow: string; title: string; description: string; to?: string; linkLabel?: string }) {
  return (
    <div className="home-section-heading">
      <div><span className="page-eyebrow">{eyebrow}</span><h2>{title}</h2><p>{description}</p></div>
      {to && <Link className="home-section-heading__link" to={to}>{linkLabel ?? 'Xem tất cả'} <ArrowRight size={17} /></Link>}
    </div>
  )
}
