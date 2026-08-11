import { ArrowUpRight, Mail, MapPin, Phone } from 'lucide-react'
import { Link } from 'react-router-dom'
import { BrandLogo } from '../ui/BrandLogo'
import './footer.css'

const footerGroups = [
  { title: 'Việc làm', links: ['Tìm việc', 'Việc mới', 'Việc lương cao', 'Remote'] },
  { title: 'CV & Sự nghiệp', links: ['Tạo CV', 'Mẫu CV', 'AI Resume', 'Cover Letter'] },
  { title: 'Nhà tuyển dụng', links: ['Đăng tuyển', 'Tìm ứng viên', 'Quản lý tin'] },
  { title: 'Công cụ', links: ['AI Matching', 'Công cụ nghề nghiệp'] },
  { title: 'Hỗ trợ', links: ['FAQ', 'Liên hệ', 'Điều khoản', 'Chính sách'] },
]

export function Footer() {
  return (
    <footer className="footer">
      <div className="container footer__top">
        <div className="footer__brand">
          <BrandLogo />
          <p>Kiến tạo kết nối nghề nghiệp minh bạch, thông minh và bền vững cho người Việt.</p>
          <div className="footer__contact">
            <span><Phone size={16} /> 028 7300 8080</span>
            <span><Mail size={16} /> hello@recruitment.local</span>
            <span><MapPin size={16} /> Việt Nam</span>
          </div>
        </div>
        <div className="footer__groups">
          {footerGroups.map((group) => (
            <div key={group.title}>
              <h2>{group.title}</h2>
              {group.links.map((label) => <Link key={label} to={group.title === 'Việc làm' ? '/jobs' : '/'}>{label}<ArrowUpRight size={13} /></Link>)}
            </div>
          ))}
        </div>
      </div>
      <div className="container footer__bottom">
        <span>© {new Date().getFullYear()} RecruitmentSystem</span>
        <span>Made for better careers.</span>
      </div>
    </footer>
  )
}
