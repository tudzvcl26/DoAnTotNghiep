import { ChevronDown, ChevronUp, Copy, ExternalLink, Mail, MapPin, Phone, Plus, Trash2 } from 'lucide-react'
import type { ReactNode } from 'react'
import type { CvCertification, CvContent, CvEducation, CvExperience, CvNamedItem, CvProject, CvTemplateId } from '../cv.types'
import { EditableText } from './EditableText'

const hasText = (value?: string) => Boolean(value?.trim())
const replaceAt = <T,>(items: T[], index: number, item: T) => items.map((current, currentIndex) => currentIndex === index ? item : current)
const moveAt = <T,>(items: T[], index: number, direction: -1 | 1) => {
  const target = index + direction
  if (target < 0 || target >= items.length) return items
  const next = [...items]
  ;[next[index], next[target]] = [next[target], next[index]]
  return next
}

export type CvPreviewEditor = { onChange: (content: CvContent) => void; onCheckpoint: () => void }
type CvPreviewProps = { content: CvContent; templateId: CvTemplateId; compact?: boolean; editor?: CvPreviewEditor }

export function CvPreview({ content, templateId, compact = false, editor }: CvPreviewProps) {
  const editing = Boolean(editor) && !compact
  const personal = content.personalInfo
  const textProps = (label: string) => ({ label, onEditStart: editor?.onCheckpoint })
  const setPersonal = (field: keyof CvContent['personalInfo'], value: string) => editor?.onChange({ ...content, personalInfo: { ...personal, [field]: value } })
  const structural = (next: CvContent) => { editor?.onCheckpoint(); editor?.onChange(next) }

  return <article className={`cv-paper cv-paper--${templateId}${compact ? ' cv-paper--compact' : ''}${editing ? ' cv-paper--editable' : ''}`} aria-label={editing ? 'CV đang chỉnh sửa trực tiếp' : 'Bản xem trước CV'}>
    <header className="cv-paper__header" id="cv-section-personal">
      <h1>{editing ? <EditableText {...textProps('Họ và tên')} value={personal.fullName} placeholder="Họ và tên" onChange={(value) => setPersonal('fullName', value)} /> : personal.fullName || 'Họ và tên'}</h1>
      <p>{editing ? <EditableText {...textProps('Chức danh')} value={personal.headline} placeholder="Vị trí chuyên môn" onChange={(value) => setPersonal('headline', value)} /> : personal.headline || 'Vị trí chuyên môn'}</p>
      <div className="cv-paper__contact">
        <ContactField icon={<Mail />} visible={editing || hasText(personal.email)}>{editing ? <EditableText {...textProps('Email')} value={personal.email} placeholder="email@example.com" onChange={(value) => setPersonal('email', value)} /> : personal.email}</ContactField>
        <ContactField icon={<Phone />} visible={editing || hasText(personal.phone)}>{editing ? <EditableText {...textProps('Số điện thoại')} value={personal.phone} placeholder="Số điện thoại" onChange={(value) => setPersonal('phone', value)} /> : personal.phone}</ContactField>
        <ContactField icon={<MapPin />} visible={editing || hasText(personal.location)}>{editing ? <EditableText {...textProps('Địa điểm')} value={personal.location} placeholder="Địa điểm" onChange={(value) => setPersonal('location', value)} /> : personal.location}</ContactField>
        <ContactField icon={<ExternalLink />} visible={editing || hasText(personal.website)}>{editing ? <EditableText {...textProps('Website')} value={personal.website} placeholder="Website" onChange={(value) => setPersonal('website', value)} /> : personal.website}</ContactField>
      </div>
    </header>

    <CvSection id="summary" title="Giới thiệu" editable={editing} visible={editing || hasText(content.summary)}>
      {editing ? <EditableText {...textProps('Mục tiêu nghề nghiệp')} multiline value={content.summary} placeholder="Nhấp để viết mục tiêu nghề nghiệp…" onChange={(summary) => editor?.onChange({ ...content, summary })} /> : <p>{content.summary}</p>}
    </CvSection>

    <CvSection id="experience" title="Kinh nghiệm" editable={editing} visible={editing || content.experiences.length > 0} onAdd={() => structural({ ...content, experiences: [...content.experiences, { position: '', company: '', startDate: '', endDate: '', description: '' }] })}>
      {content.experiences.map((item, index) => <EditableExperience key={index} item={item} index={index} count={content.experiences.length} editing={editing} checkpoint={editor?.onCheckpoint} onChange={(next) => editor?.onChange({ ...content, experiences: replaceAt(content.experiences, index, next) })} onDuplicate={() => structural({ ...content, experiences: [...content.experiences.slice(0, index + 1), { ...item }, ...content.experiences.slice(index + 1)] })} onRemove={() => structural({ ...content, experiences: content.experiences.filter((_, itemIndex) => itemIndex !== index) })} onMove={(direction) => structural({ ...content, experiences: moveAt(content.experiences, index, direction) })} />)}
    </CvSection>

    <CvSection id="education" title="Học vấn" editable={editing} visible={editing || content.education.length > 0} onAdd={() => structural({ ...content, education: [...content.education, { school: '', degree: '', startDate: '', endDate: '', description: '' }] })}>
      {content.education.map((item, index) => <EditableEducation key={index} item={item} index={index} count={content.education.length} editing={editing} checkpoint={editor?.onCheckpoint} onChange={(next) => editor?.onChange({ ...content, education: replaceAt(content.education, index, next) })} onDuplicate={() => structural({ ...content, education: [...content.education.slice(0, index + 1), { ...item }, ...content.education.slice(index + 1)] })} onRemove={() => structural({ ...content, education: content.education.filter((_, itemIndex) => itemIndex !== index) })} onMove={(direction) => structural({ ...content, education: moveAt(content.education, index, direction) })} />)}
    </CvSection>

    <CvSection id="skills" title="Kỹ năng" editable={editing} visible={editing || content.skills.some(hasText)} onAdd={() => structural({ ...content, skills: [...content.skills, ''] })}>
      <div className="cv-paper__skills">{content.skills.map((skill, index) => editing ? <div className="cv-inline-skill" key={index}><EditableText {...textProps(`Kỹ năng ${index + 1}`)} value={skill} placeholder="Kỹ năng mới" onChange={(value) => editor?.onChange({ ...content, skills: replaceAt(content.skills, index, value) })} /><button type="button" aria-label={`Xóa kỹ năng ${index + 1}`} onClick={() => structural({ ...content, skills: content.skills.filter((_, itemIndex) => itemIndex !== index) })}><Trash2 /></button></div> : hasText(skill) ? <span key={index}>{skill}</span> : null)}</div>
    </CvSection>

    <CvSection id="projects" title="Dự án" editable={editing} visible={editing || content.projects.length > 0} onAdd={() => structural({ ...content, projects: [...content.projects, { name: '', url: '', description: '' }] })}>
      {content.projects.map((item, index) => <EditableProject key={index} item={item} index={index} count={content.projects.length} editing={editing} checkpoint={editor?.onCheckpoint} onChange={(next) => editor?.onChange({ ...content, projects: replaceAt(content.projects, index, next) })} onDuplicate={() => structural({ ...content, projects: [...content.projects.slice(0, index + 1), { ...item }, ...content.projects.slice(index + 1)] })} onRemove={() => structural({ ...content, projects: content.projects.filter((_, itemIndex) => itemIndex !== index) })} onMove={(direction) => structural({ ...content, projects: moveAt(content.projects, index, direction) })} />)}
    </CvSection>

    <CvSection id="certifications" title="Chứng chỉ" editable={editing} visible={editing || content.certifications.length > 0} onAdd={() => structural({ ...content, certifications: [...content.certifications, { name: '', issuer: '', date: '' }] })}>
      {content.certifications.map((item, index) => <EditableCertification key={index} item={item} index={index} count={content.certifications.length} editing={editing} checkpoint={editor?.onCheckpoint} onChange={(next) => editor?.onChange({ ...content, certifications: replaceAt(content.certifications, index, next) })} onDuplicate={() => structural({ ...content, certifications: [...content.certifications.slice(0, index + 1), { ...item }, ...content.certifications.slice(index + 1)] })} onRemove={() => structural({ ...content, certifications: content.certifications.filter((_, itemIndex) => itemIndex !== index) })} onMove={(direction) => structural({ ...content, certifications: moveAt(content.certifications, index, direction) })} />)}
    </CvSection>

    <NamedSection id="awards" title="Giải thưởng" items={content.awards} editing={editing} checkpoint={editor?.onCheckpoint} onChange={(awards) => editor?.onChange({ ...content, awards })} onStructural={(awards) => structural({ ...content, awards })} />
    <NamedSection id="activities" title="Hoạt động" items={content.activities} editing={editing} checkpoint={editor?.onCheckpoint} onChange={(activities) => editor?.onChange({ ...content, activities })} onStructural={(activities) => structural({ ...content, activities })} />
  </article>
}

function ContactField({ icon, visible, children }: { icon: ReactNode; visible: boolean; children: ReactNode }) { return visible ? <span>{icon}{children}</span> : null }

function CvSection({ id, title, visible, editable, onAdd, children }: { id: string; title: string; visible: boolean; editable?: boolean; onAdd?: () => void; children: ReactNode }) {
  if (!visible) return null
  return <section className={`cv-paper__section${editable ? ' is-editable' : ''}`} id={`cv-section-${id}`}><div className="cv-paper__section-heading"><h2>{title}</h2>{editable && onAdd && <button type="button" aria-label={`Thêm ${title}`} onClick={onAdd}><Plus /> Thêm</button>}</div>{children}</section>
}

type ItemControlsProps = { index: number; count: number; onDuplicate: () => void; onRemove: () => void; onMove: (direction: -1 | 1) => void }
function ItemControls({ index, count, onDuplicate, onRemove, onMove }: ItemControlsProps) {
  return <div className="cv-inline-controls"><button type="button" disabled={index === 0} aria-label="Di chuyển lên" onClick={() => onMove(-1)}><ChevronUp /></button><button type="button" disabled={index === count - 1} aria-label="Di chuyển xuống" onClick={() => onMove(1)}><ChevronDown /></button><button type="button" aria-label="Nhân bản mục" onClick={onDuplicate}><Copy /></button><button type="button" aria-label="Xóa mục" onClick={onRemove}><Trash2 /></button></div>
}

type EditableItemProps<T> = ItemControlsProps & { item: T; editing: boolean; checkpoint?: () => void; onChange: (item: T) => void }
const fieldProps = (checkpoint: (() => void) | undefined, label: string) => ({ label, onEditStart: checkpoint })

function EditableExperience({ item, editing, checkpoint, onChange, ...controls }: EditableItemProps<CvExperience>) {
  if (!editing) return <CvItem title={item.position} meta={[item.company, formatDates(item.startDate, item.endDate)].filter(Boolean).join(' · ')} description={item.description} />
  return <div className="cv-paper__item is-editable"><ItemControls {...controls} /><h3><EditableText {...fieldProps(checkpoint, 'Vị trí công việc')} value={item.position} placeholder="Vị trí công việc" onChange={(position) => onChange({ ...item, position })} /></h3><div className="cv-inline-meta"><EditableText {...fieldProps(checkpoint, 'Tên công ty')} value={item.company} placeholder="Tên công ty" onChange={(company) => onChange({ ...item, company })} /><span>·</span><EditableText {...fieldProps(checkpoint, 'Thời gian bắt đầu')} value={item.startDate} placeholder="Bắt đầu" onChange={(startDate) => onChange({ ...item, startDate })} /><span>–</span><EditableText {...fieldProps(checkpoint, 'Thời gian kết thúc')} value={item.endDate} placeholder="Hiện tại" onChange={(endDate) => onChange({ ...item, endDate })} /></div><EditableText {...fieldProps(checkpoint, 'Mô tả kinh nghiệm')} multiline value={item.description} placeholder="Nhấp để mô tả công việc và thành tựu…" onChange={(description) => onChange({ ...item, description })} /></div>
}

function EditableEducation({ item, editing, checkpoint, onChange, ...controls }: EditableItemProps<CvEducation>) {
  if (!editing) return <CvItem title={item.degree} meta={[item.school, formatDates(item.startDate, item.endDate)].filter(Boolean).join(' · ')} description={item.description} />
  return <div className="cv-paper__item is-editable"><ItemControls {...controls} /><h3><EditableText {...fieldProps(checkpoint, 'Bằng cấp hoặc chuyên ngành')} value={item.degree} placeholder="Bằng cấp / Chuyên ngành" onChange={(degree) => onChange({ ...item, degree })} /></h3><div className="cv-inline-meta"><EditableText {...fieldProps(checkpoint, 'Tên trường')} value={item.school} placeholder="Tên trường" onChange={(school) => onChange({ ...item, school })} /><span>·</span><EditableText {...fieldProps(checkpoint, 'Thời gian bắt đầu học')} value={item.startDate} placeholder="Bắt đầu" onChange={(startDate) => onChange({ ...item, startDate })} /><span>–</span><EditableText {...fieldProps(checkpoint, 'Thời gian kết thúc học')} value={item.endDate} placeholder="Kết thúc" onChange={(endDate) => onChange({ ...item, endDate })} /></div><EditableText {...fieldProps(checkpoint, 'Mô tả học vấn')} multiline value={item.description} placeholder="Nhấp để thêm GPA, thành tích hoặc môn học nổi bật…" onChange={(description) => onChange({ ...item, description })} /></div>
}

function EditableProject({ item, editing, checkpoint, onChange, ...controls }: EditableItemProps<CvProject>) {
  if (!editing) return <CvItem title={item.name} meta={item.url} description={item.description} />
  return <div className="cv-paper__item is-editable"><ItemControls {...controls} /><h3><EditableText {...fieldProps(checkpoint, 'Tên dự án')} value={item.name} placeholder="Tên dự án" onChange={(name) => onChange({ ...item, name })} /></h3><div className="cv-inline-meta"><EditableText {...fieldProps(checkpoint, 'Liên kết dự án')} value={item.url} placeholder="GitHub / Demo link" onChange={(url) => onChange({ ...item, url })} /></div><EditableText {...fieldProps(checkpoint, 'Mô tả dự án')} multiline value={item.description} placeholder="Nhấp để mô tả dự án, vai trò và công nghệ…" onChange={(description) => onChange({ ...item, description })} /></div>
}

function EditableCertification({ item, editing, checkpoint, onChange, ...controls }: EditableItemProps<CvCertification>) {
  if (!editing) return <CvItem title={item.name} meta={[item.issuer, item.date].filter(Boolean).join(' · ')} />
  return <div className="cv-paper__item is-editable"><ItemControls {...controls} /><h3><EditableText {...fieldProps(checkpoint, 'Tên chứng chỉ')} value={item.name} placeholder="Tên chứng chỉ" onChange={(name) => onChange({ ...item, name })} /></h3><div className="cv-inline-meta"><EditableText {...fieldProps(checkpoint, 'Đơn vị cấp chứng chỉ')} value={item.issuer} placeholder="Đơn vị cấp" onChange={(issuer) => onChange({ ...item, issuer })} /><span>·</span><EditableText {...fieldProps(checkpoint, 'Ngày cấp chứng chỉ')} value={item.date} placeholder="Thời gian" onChange={(date) => onChange({ ...item, date })} /></div></div>
}

function NamedSection({ id, title, items, editing, checkpoint, onChange, onStructural }: { id: string; title: string; items: CvNamedItem[]; editing: boolean; checkpoint?: () => void; onChange: (items: CvNamedItem[]) => void; onStructural: (items: CvNamedItem[]) => void }) {
  return <CvSection id={id} title={title} editable={editing} visible={editing || items.length > 0} onAdd={() => onStructural([...items, { name: '', date: '', description: '' }])}>{items.map((item, index) => editing ? <div className="cv-paper__item is-editable" key={index}><ItemControls index={index} count={items.length} onDuplicate={() => onStructural([...items.slice(0, index + 1), { ...item }, ...items.slice(index + 1)])} onRemove={() => onStructural(items.filter((_, itemIndex) => itemIndex !== index))} onMove={(direction) => onStructural(moveAt(items, index, direction))} /><h3><EditableText {...fieldProps(checkpoint, `Tên ${title.toLowerCase()}`)} value={item.name} placeholder={`Tên ${title.toLowerCase()}`} onChange={(name) => onChange(replaceAt(items, index, { ...item, name }))} /></h3><div className="cv-inline-meta"><EditableText {...fieldProps(checkpoint, `Thời gian ${title.toLowerCase()}`)} value={item.date} placeholder="Thời gian" onChange={(date) => onChange(replaceAt(items, index, { ...item, date }))} /></div><EditableText {...fieldProps(checkpoint, `Mô tả ${title.toLowerCase()}`)} multiline value={item.description} placeholder="Nhấp để thêm mô tả…" onChange={(description) => onChange(replaceAt(items, index, { ...item, description }))} /></div> : <CvItem key={index} title={item.name} meta={item.date} description={item.description} />)}</CvSection>
}

function formatDates(start?: string, end?: string) { return start && end ? `${start} – ${end}` : start || end || '' }
function CvItem({ title, meta, description }: { title?: string; meta?: string; description?: string }) { return <div className="cv-paper__item"><h3>{title || 'Nội dung'}</h3>{hasText(meta) && <strong>{meta}</strong>}{hasText(description) && <p>{description}</p>}</div> }
