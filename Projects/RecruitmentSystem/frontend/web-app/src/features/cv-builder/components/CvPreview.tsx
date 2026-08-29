import { Mail, MapPin, Phone, ExternalLink } from 'lucide-react'
import type { ReactNode } from 'react'
import type { CvContent, CvTemplateId } from '../cv.types'

const hasText = (value?: string) => Boolean(value?.trim())

export function CvPreview({ content, templateId, compact = false }: { content: CvContent; templateId: CvTemplateId; compact?: boolean }) {
  const personal = content.personalInfo
  return (
    <article className={`cv-paper cv-paper--${templateId}${compact ? ' cv-paper--compact' : ''}`} aria-label="Bản xem trước CV">
      <header className="cv-paper__header">
        <h1>{personal.fullName || 'Họ và tên'}</h1>
        <p>{personal.headline || 'Vị trí chuyên môn'}</p>
        <div className="cv-paper__contact">
          {hasText(personal.email) && <span><Mail />{personal.email}</span>}
          {hasText(personal.phone) && <span><Phone />{personal.phone}</span>}
          {hasText(personal.location) && <span><MapPin />{personal.location}</span>}
          {hasText(personal.website) && <span><ExternalLink />{personal.website}</span>}
        </div>
      </header>
      <CvSection title="Giới thiệu" visible={hasText(content.summary)}><p>{content.summary}</p></CvSection>
      <CvSection title="Kinh nghiệm" visible={content.experiences.length > 0}>
        {content.experiences.map((item, index) => <CvItem key={`${item.company}-${index}`} title={item.position} meta={[item.company, item.startDate && item.endDate ? `${item.startDate} – ${item.endDate}` : item.startDate || item.endDate].filter(Boolean).join(' · ')} description={item.description} />)}
      </CvSection>
      <CvSection title="Học vấn" visible={content.education.length > 0}>
        {content.education.map((item, index) => <CvItem key={`${item.school}-${index}`} title={item.degree} meta={[item.school, item.startDate && item.endDate ? `${item.startDate} – ${item.endDate}` : item.startDate || item.endDate].filter(Boolean).join(' · ')} description={item.description} />)}
      </CvSection>
      <CvSection title="Kỹ năng" visible={content.skills.some(hasText)}><div className="cv-paper__skills">{content.skills.filter(hasText).map((skill, index) => <span key={`${skill}-${index}`}>{skill}</span>)}</div></CvSection>
      <CvSection title="Dự án" visible={content.projects.length > 0}>{content.projects.map((item, index) => <CvItem key={`${item.name}-${index}`} title={item.name} meta={item.url} description={item.description} />)}</CvSection>
      <CvSection title="Chứng chỉ" visible={content.certifications.length > 0}>{content.certifications.map((item, index) => <CvItem key={`${item.name}-${index}`} title={item.name} meta={[item.issuer, item.date].filter(Boolean).join(' · ')} />)}</CvSection>
      <CvSection title="Giải thưởng" visible={content.awards.length > 0}>{content.awards.map((item, index) => <CvItem key={`${item.name}-${index}`} title={item.name} meta={item.date} description={item.description} />)}</CvSection>
      <CvSection title="Hoạt động" visible={content.activities.length > 0}>{content.activities.map((item, index) => <CvItem key={`${item.name}-${index}`} title={item.name} meta={item.date} description={item.description} />)}</CvSection>
    </article>
  )
}

function CvSection({ title, visible, children }: { title: string; visible: boolean; children: ReactNode }) {
  if (!visible) return null
  return <section className="cv-paper__section"><h2>{title}</h2>{children}</section>
}

function CvItem({ title, meta, description }: { title?: string; meta?: string; description?: string }) {
  return <div className="cv-paper__item"><h3>{title || 'Nội dung'}</h3>{hasText(meta) && <strong>{meta}</strong>}{hasText(description) && <p>{description}</p>}</div>
}
