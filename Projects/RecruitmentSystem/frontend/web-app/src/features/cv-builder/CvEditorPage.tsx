import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowDown, ArrowLeft, ArrowUp, Check, ChevronRight, Download, Eye, LayoutTemplate, Minus, Plus, Save, Sparkles, Trash2 } from 'lucide-react'
import { type CSSProperties, type ReactNode, useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { cvApi, saveBlob } from './cv.api'
import { CvPreview } from './components/CvPreview'
import { cvTemplates } from './cv.templates'
import { emptyCvContent, type CvCertification, type CvContent, type CvEducation, type CvExperience, type CvNamedItem, type CvProject, type CvTemplateId, type SaveCvPayload } from './cv.types'
import './cv-builder.css'

const blankExperience = (): CvExperience => ({ position: '', company: '', startDate: '', endDate: '', description: '' })
const blankEducation = (): CvEducation => ({ school: '', degree: '', startDate: '', endDate: '', description: '' })
const blankProject = (): CvProject => ({ name: '', url: '', description: '' })
const blankCertification = (): CvCertification => ({ name: '', issuer: '', date: '' })
const blankNamedItem = (): CvNamedItem => ({ name: '', date: '', description: '' })
const sections = [
  ['personal', 'Thông tin', 'Tên và liên hệ'], ['summary', 'Giới thiệu', 'Mục tiêu nghề nghiệp'], ['experience', 'Kinh nghiệm', 'Vai trò và thành tựu'],
  ['education', 'Học vấn', 'Trường và bằng cấp'], ['skills', 'Kỹ năng', 'Năng lực nổi bật'], ['projects', 'Dự án', 'Sản phẩm đã thực hiện'],
  ['certifications', 'Chứng chỉ', 'Chứng nhận chuyên môn'], ['awards', 'Giải thưởng', 'Thành tích nổi bật'], ['activities', 'Hoạt động', 'Cộng đồng và ngoại khóa'],
] as const
type SectionId = (typeof sections)[number][0]

export function CvEditorPage() {
  const { id } = useParams()
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const initialTemplate = cvTemplates.some((item) => item.id === params.get('template')) ? params.get('template') as CvTemplateId : 'classic'
  const [title, setTitle] = useState('CV tiếng Việt')
  const [templateId, setTemplateId] = useState<CvTemplateId>(initialTemplate)
  const [content, setContent] = useState<CvContent>(emptyCvContent)
  const [mobileTab, setMobileTab] = useState<'content' | 'preview'>('content')
  const [activeSection, setActiveSection] = useState<SectionId>('personal')
  const [templatePickerOpen, setTemplatePickerOpen] = useState(false)
  const [zoom, setZoom] = useState(85)
  const [savedSignature, setSavedSignature] = useState('')
  const [loadedId, setLoadedId] = useState<string | null>(null)
  const profileImportStarted = useRef(false)
  const existing = useQuery({ queryKey: ['candidate-cv', id], queryFn: () => cvApi.get(id!), enabled: Boolean(id) })

  useEffect(() => {
    if (!existing.data || loadedId === existing.data.id) return
    setTitle(existing.data.title); setTemplateId(existing.data.templateId); setContent(existing.data.content); setSavedSignature(JSON.stringify({ title: existing.data.title, templateId: existing.data.templateId, language: 'vi', content: existing.data.content })); setLoadedId(existing.data.id)
  }, [existing.data, loadedId])

  const currentPayload: SaveCvPayload = { title: title.trim() || 'CV tiếng Việt', templateId, language: 'vi', content }
  const payload = () => currentPayload
  const signature = JSON.stringify(currentPayload)
  const save = useMutation({ mutationFn: () => id ? cvApi.update(id, payload()) : cvApi.create(payload()), onSuccess: async (saved) => { setSavedSignature(JSON.stringify({ title: saved.title, templateId: saved.templateId, language: saved.language, content: saved.content })); await queryClient.invalidateQueries({ queryKey: ['candidate-cvs'] }); await queryClient.invalidateQueries({ queryKey: ['candidate-cv', saved.id] }); if (!id) navigate(`/cv/${saved.id}/edit`, { replace: true }) } })
  const autofill = useMutation({ mutationFn: () => cvApi.createFromProfile(title.trim() || 'CV từ hồ sơ', templateId), onSuccess: async (saved) => { await queryClient.invalidateQueries({ queryKey: ['candidate-cvs'] }); navigate(`/cv/${saved.id}/edit`, { replace: true }) } })
  const download = useMutation({ mutationFn: async () => { const saved = id ? await cvApi.update(id, payload()) : await cvApi.create(payload()); return { saved, blob: await cvApi.download(saved.id) } }, onSuccess: ({ saved, blob }) => { saveBlob(blob, `${saved.title}.pdf`); if (!id) navigate(`/cv/${saved.id}/edit`, { replace: true }) } })

  useEffect(() => {
    if (id || params.get('source') !== 'profile' || profileImportStarted.current) return
    profileImportStarted.current = true
    autofill.mutate()
  }, [id, params, autofill])

  useEffect(() => {
    if (!templatePickerOpen) return
    const closeOnEscape = (event: KeyboardEvent) => { if (event.key === 'Escape') setTemplatePickerOpen(false) }
    window.addEventListener('keydown', closeOnEscape)
    return () => window.removeEventListener('keydown', closeOnEscape)
  }, [templatePickerOpen])

  const setPersonal = (field: keyof CvContent['personalInfo'], value: string) => setContent((current) => ({ ...current, personalInfo: { ...current.personalInfo, [field]: value } }))
  const updateList = <K extends 'experiences' | 'education' | 'projects' | 'certifications' | 'awards' | 'activities'>(key: K, index: number, value: CvContent[K][number]) => setContent((current) => ({ ...current, [key]: current[key].map((item, itemIndex) => itemIndex === index ? value : item) }))
  const removeList = (key: 'experiences' | 'education' | 'projects' | 'certifications' | 'awards' | 'activities', index: number) => setContent((current) => ({ ...current, [key]: current[key].filter((_, itemIndex) => itemIndex !== index) }))
  const moveList = (key: 'experiences' | 'education' | 'projects' | 'certifications' | 'awards' | 'activities', index: number, direction: -1 | 1) => setContent((current) => { const next = [...current[key]] as CvNamedItem[]; const target = index + direction; if (target < 0 || target >= next.length) return current; [next[index], next[target]] = [next[target], next[index]]; return { ...current, [key]: next } as CvContent })

  if (id && existing.isLoading) return <main className="cv-page"><div className="cv-state">Đang mở CV…</div></main>
  if (id && existing.isError) return <main className="cv-page"><div className="cv-state cv-state--error">Không tìm thấy CV hoặc bạn không có quyền truy cập.</div></main>

  return <main className="cv-editor">
    <header className="cv-editor__toolbar"><Link to="/cv"><ArrowLeft /> CV của tôi</Link><input aria-label="Tên CV" value={title} maxLength={150} onChange={(event) => setTitle(event.target.value)} /><button type="button" className="cv-editor__template-trigger" onClick={() => setTemplatePickerOpen(true)}><LayoutTemplate /> {cvTemplates.find((item) => item.id === templateId)?.name}</button><span className={`cv-save-state${save.isError ? ' is-error' : ''}`}>{save.isPending ? 'Đang lưu…' : save.isError ? 'Lưu thất bại' : !id && !savedSignature ? 'Chưa lưu' : signature === savedSignature ? <><Check /> Đã lưu</> : 'Có thay đổi chưa lưu'}</span><div className="cv-editor__toolbar-actions">{!id && params.get('source') !== 'profile' && <button type="button" onClick={() => autofill.mutate()} disabled={autofill.isPending}><Sparkles /> Điền từ hồ sơ</button>}<button type="button" onClick={() => setMobileTab('preview')}><Eye /> Xem trước</button><button type="button" onClick={() => download.mutate()} disabled={download.isPending}><Download /> PDF</button><button className="is-primary" type="button" onClick={() => save.mutate()} disabled={save.isPending}><Save /> {save.isPending ? 'Đang lưu…' : 'Lưu CV'}</button></div></header>
    {(save.isError || autofill.isError || download.isError) && <div className="cv-editor__error">Không thể hoàn tất thao tác. Vui lòng kiểm tra dữ liệu và thử lại.</div>}
    <div className="cv-editor__mobile-tabs"><button className={mobileTab === 'content' ? 'is-active' : ''} onClick={() => setMobileTab('content')} type="button">Nội dung</button><button className={mobileTab === 'preview' ? 'is-active' : ''} onClick={() => setMobileTab('preview')} type="button">Xem trước</button></div>
    <div className="cv-editor__workspace">
      <aside data-active-section={activeSection} className={`cv-editor__form${mobileTab === 'preview' ? ' is-mobile-hidden' : ''}`}>
        <nav className="cv-editor__section-nav" aria-label="Các phần CV">{sections.map(([section, label, hint]) => <button type="button" className={activeSection === section ? 'is-active' : ''} onClick={() => setActiveSection(section)} key={section}><span>{label}<small>{hint}</small></span><ChevronRight /></button>)}</nav>
        <EditorPanel id="cv-personal" title="Thông tin cá nhân"><div className="cv-form-grid"><Field label="Họ và tên" value={content.personalInfo.fullName} onChange={(value) => setPersonal('fullName', value)} /><Field label="Chức danh" value={content.personalInfo.headline} onChange={(value) => setPersonal('headline', value)} /><Field label="Email" type="email" value={content.personalInfo.email} onChange={(value) => setPersonal('email', value)} /><Field label="Điện thoại" value={content.personalInfo.phone} onChange={(value) => setPersonal('phone', value)} /><Field label="Địa điểm" value={content.personalInfo.location} onChange={(value) => setPersonal('location', value)} /><Field label="Website" value={content.personalInfo.website} onChange={(value) => setPersonal('website', value)} /></div></EditorPanel>
        <EditorPanel id="cv-summary" title="Mục tiêu / Giới thiệu"><TextArea label="Giới thiệu bản thân" value={content.summary} onChange={(value) => setContent((current) => ({ ...current, summary: value }))} /></EditorPanel>
        <EditorPanel id="cv-experience" title="Kinh nghiệm" onAdd={() => setContent((current) => ({ ...current, experiences: [...current.experiences, blankExperience()] }))}>{content.experiences.map((item, index) => <ItemCard key={index} index={index} count={content.experiences.length} onRemove={() => removeList('experiences', index)} onMove={(direction) => moveList('experiences', index, direction)}><div className="cv-form-grid"><Field label="Vị trí" value={item.position} onChange={(value) => updateList('experiences', index, { ...item, position: value })} /><Field label="Công ty" value={item.company} onChange={(value) => updateList('experiences', index, { ...item, company: value })} /><Field label="Bắt đầu" value={item.startDate} onChange={(value) => updateList('experiences', index, { ...item, startDate: value })} /><Field label="Kết thúc" value={item.endDate} onChange={(value) => updateList('experiences', index, { ...item, endDate: value })} /></div><TextArea label="Mô tả và thành tựu" value={item.description} onChange={(value) => updateList('experiences', index, { ...item, description: value })} /></ItemCard>)}</EditorPanel>
        <EditorPanel id="cv-education" title="Học vấn" onAdd={() => setContent((current) => ({ ...current, education: [...current.education, blankEducation()] }))}>{content.education.map((item, index) => <ItemCard key={index} index={index} count={content.education.length} onRemove={() => removeList('education', index)} onMove={(direction) => moveList('education', index, direction)}><div className="cv-form-grid"><Field label="Trường" value={item.school} onChange={(value) => updateList('education', index, { ...item, school: value })} /><Field label="Bằng cấp / Chuyên ngành" value={item.degree} onChange={(value) => updateList('education', index, { ...item, degree: value })} /><Field label="Bắt đầu" value={item.startDate} onChange={(value) => updateList('education', index, { ...item, startDate: value })} /><Field label="Kết thúc" value={item.endDate} onChange={(value) => updateList('education', index, { ...item, endDate: value })} /></div><TextArea label="Mô tả" value={item.description} onChange={(value) => updateList('education', index, { ...item, description: value })} /></ItemCard>)}</EditorPanel>
        <EditorPanel id="cv-skills" title="Kỹ năng" onAdd={() => setContent((current) => ({ ...current, skills: [...current.skills, ''] }))}>{content.skills.map((skill, index) => <div className="cv-skill-row" key={index}><input aria-label={`Kỹ năng ${index + 1}`} value={skill} maxLength={120} onChange={(event) => setContent((current) => ({ ...current, skills: current.skills.map((value, itemIndex) => itemIndex === index ? event.target.value : value) }))} /><button type="button" aria-label="Xóa kỹ năng" onClick={() => setContent((current) => ({ ...current, skills: current.skills.filter((_, itemIndex) => itemIndex !== index) }))}><Trash2 /></button></div>)}</EditorPanel>
        <EditorPanel id="cv-projects" title="Dự án" onAdd={() => setContent((current) => ({ ...current, projects: [...current.projects, blankProject()] }))}>{content.projects.map((item, index) => <ItemCard key={index} index={index} count={content.projects.length} onRemove={() => removeList('projects', index)} onMove={(direction) => moveList('projects', index, direction)}><div className="cv-form-grid"><Field label="Tên dự án" value={item.name} onChange={(value) => updateList('projects', index, { ...item, name: value })} /><Field label="Liên kết" value={item.url} onChange={(value) => updateList('projects', index, { ...item, url: value })} /></div><TextArea label="Mô tả" value={item.description} onChange={(value) => updateList('projects', index, { ...item, description: value })} /></ItemCard>)}</EditorPanel>
        <EditorPanel id="cv-certifications" title="Chứng chỉ" onAdd={() => setContent((current) => ({ ...current, certifications: [...current.certifications, blankCertification()] }))}>{content.certifications.map((item, index) => <ItemCard key={index} index={index} count={content.certifications.length} onRemove={() => removeList('certifications', index)} onMove={(direction) => moveList('certifications', index, direction)}><div className="cv-form-grid"><Field label="Tên chứng chỉ" value={item.name} onChange={(value) => updateList('certifications', index, { ...item, name: value })} /><Field label="Đơn vị cấp" value={item.issuer} onChange={(value) => updateList('certifications', index, { ...item, issuer: value })} /><Field label="Thời gian" value={item.date} onChange={(value) => updateList('certifications', index, { ...item, date: value })} /></div></ItemCard>)}</EditorPanel>
        <NamedItemsPanel id="cv-awards" title="Giải thưởng" items={content.awards} onAdd={() => setContent((current) => ({ ...current, awards: [...current.awards, blankNamedItem()] }))} onChange={(index, item) => updateList('awards', index, item)} onRemove={(index) => removeList('awards', index)} onMove={(index, direction) => moveList('awards', index, direction)} />
        <NamedItemsPanel id="cv-activities" title="Hoạt động" items={content.activities} onAdd={() => setContent((current) => ({ ...current, activities: [...current.activities, blankNamedItem()] }))} onChange={(index, item) => updateList('activities', index, item)} onRemove={(index) => removeList('activities', index)} onMove={(index, direction) => moveList('activities', index, direction)} />
      </aside>
      <section className={`cv-editor__preview${mobileTab === 'content' ? ' is-mobile-hidden' : ''}`}><div className="cv-editor__preview-sticky"><div className="cv-zoom-toolbar"><span>Bản xem trước trực tiếp · A4</span><div><button type="button" aria-label="Thu nhỏ" onClick={() => setZoom((value) => Math.max(60, value - 10))}><Minus /></button><output aria-live="polite">{zoom}%</output><button type="button" aria-label="Phóng to" onClick={() => setZoom((value) => Math.min(110, value + 10))}><Plus /></button></div></div><div className="cv-zoom-canvas" style={{ '--cv-zoom': zoom / 100 } as CSSProperties}><CvPreview content={content} templateId={templateId} /></div></div></section>
    </div>
    {templatePickerOpen && <div className="cv-template-dialog" role="dialog" aria-modal="true" aria-labelledby="template-dialog-title" onMouseDown={(event) => { if (event.target === event.currentTarget) setTemplatePickerOpen(false) }}><div><header><div><span>Đổi giao diện</span><h2 id="template-dialog-title">Chọn mẫu CV</h2><p>Nội dung đang nhập được giữ nguyên khi đổi mẫu.</p></div><button type="button" onClick={() => setTemplatePickerOpen(false)} aria-label="Đóng">×</button></header><div className="cv-template-dialog__grid">{cvTemplates.map((template) => <button type="button" className={template.id === templateId ? 'is-active' : ''} onClick={() => { setTemplateId(template.id); setTemplatePickerOpen(false) }} key={template.id}><span className={`cv-template-swatch cv-template-swatch--${template.id}`} /><strong>{template.name}</strong><small>{template.style}</small>{template.id === templateId && <Check />}</button>)}</div></div></div>}
  </main>
}

function EditorPanel({ id, title, onAdd, children }: { id: string; title: string; onAdd?: () => void; children: ReactNode }) { return <section className="cv-editor-panel" id={id}><header><h2>{title}</h2>{onAdd && <button type="button" onClick={onAdd}><Plus /> Thêm</button>}</header>{children}</section> }
function Field({ label, value, onChange, type = 'text' }: { label: string; value: string; onChange: (value: string) => void; type?: string }) { return <label className="cv-field"><span>{label}</span><input type={type} value={value} maxLength={2048} onChange={(event) => onChange(event.target.value)} /></label> }
function TextArea({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) { return <label className="cv-field"><span>{label}</span><textarea value={value} maxLength={5000} rows={4} onChange={(event) => onChange(event.target.value)} /></label> }
function ItemCard({ index, count, onRemove, onMove, children }: { index: number; count: number; onRemove: () => void; onMove: (direction: -1 | 1) => void; children: ReactNode }) { return <div className="cv-editor-item"><div className="cv-editor-item__tools"><strong>Mục {index + 1}</strong><button type="button" disabled={index === 0} onClick={() => onMove(-1)} aria-label="Di chuyển lên"><ArrowUp /></button><button type="button" disabled={index === count - 1} onClick={() => onMove(1)} aria-label="Di chuyển xuống"><ArrowDown /></button><button type="button" className="is-danger" onClick={onRemove} aria-label="Xóa mục"><Trash2 /></button></div>{children}</div> }
function NamedItemsPanel({ id, title, items, onAdd, onChange, onRemove, onMove }: { id: string; title: string; items: CvNamedItem[]; onAdd: () => void; onChange: (index: number, item: CvNamedItem) => void; onRemove: (index: number) => void; onMove: (index: number, direction: -1 | 1) => void }) { return <EditorPanel id={id} title={title} onAdd={onAdd}>{items.map((item, index) => <ItemCard key={index} index={index} count={items.length} onRemove={() => onRemove(index)} onMove={(direction) => onMove(index, direction)}><div className="cv-form-grid"><Field label="Tên" value={item.name} onChange={(value) => onChange(index, { ...item, name: value })} /><Field label="Thời gian" value={item.date} onChange={(value) => onChange(index, { ...item, date: value })} /></div><TextArea label="Mô tả" value={item.description} onChange={(value) => onChange(index, { ...item, description: value })} /></ItemCard>)}</EditorPanel> }
