import { ArrowRight, Sparkles } from 'lucide-react'
import { Link } from 'react-router-dom'
import type { MegaMenuDefinition } from './menu.config'

type MegaMenuProps = {
  menu: MegaMenuDefinition
  onNavigate: () => void
}

export function MegaMenu({ menu, onNavigate }: MegaMenuProps) {
  return (
    <div className="mega-menu" role="region" aria-label={`Menu ${menu.label}`}>
      <div className="container mega-menu__inner">
        <div className="mega-menu__intro">
          <span className="mega-menu__eyebrow"><Sparkles size={15} /> Khám phá</span>
          <h2>{menu.label}</h2>
          <p>Những lối tắt hữu ích giúp hành trình nghề nghiệp của bạn thuận lợi hơn.</p>
        </div>
        <div className="mega-menu__sections">
          {menu.sections.map((section) => (
            <div className="mega-menu__section" key={section.title}>
              <h3>{section.title}</h3>
              {section.links.map((link) => (
                <Link key={`${section.title}-${link.label}`} to={link.to} onClick={onNavigate}>
                  {link.label}<ArrowRight size={14} aria-hidden="true" />
                </Link>
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
