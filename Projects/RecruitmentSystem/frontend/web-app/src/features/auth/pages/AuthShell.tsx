import { ShieldCheck, Sparkles } from 'lucide-react'
import type { ReactNode } from 'react'
import { BrandLogo } from '../../../components/ui/BrandLogo'
import './auth-pages.css'

export function AuthShell({ title, description, children, footer }: { title: string; description: string; children: ReactNode; footer: ReactNode }) {
  return (
    <section className="auth-page">
      <div className="auth-page__aside">
        <BrandLogo />
        <div>
          <span className="auth-page__eyebrow"><Sparkles size={16} /> Career clarity</span>
          <h2>Chủ động cho hành trình nghề nghiệp của bạn.</h2>
          <p>Một hồ sơ tốt bắt đầu từ nền tảng an toàn, rõ ràng và dễ sử dụng.</p>
        </div>
        <span className="auth-page__trust"><ShieldCheck size={18} /> Phiên đăng nhập được bảo vệ qua API Gateway</span>
      </div>
      <div className="auth-page__main">
        <div className="auth-card">
          <span className="auth-card__mobile-logo"><BrandLogo /></span>
          <h1>{title}</h1>
          <p>{description}</p>
          {children}
          <div className="auth-card__footer">{footer}</div>
        </div>
      </div>
    </section>
  )
}
