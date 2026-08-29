import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Check, Download, Eye, LayoutTemplate, Library, Lightbulb, Minus, Palette, PanelLeft, Pencil, Plus, Redo2, Save, Sparkles, Undo2 } from 'lucide-react'
import { type CSSProperties, useCallback, useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { cvApi, saveBlob } from './cv.api'
import { CvPreview } from './components/CvPreview'
import { cvTemplates } from './cv.templates'
import { emptyCvContent, type CvContent, type CvTemplateId, type SaveCvPayload } from './cv.types'
import './cv-builder.css'

type ToolId = 'design' | 'add' | 'layout' | 'guide' | 'library'
type EditorSnapshot = { title: string; templateId: CvTemplateId; content: CvContent }
const sections = [
  ['personal', 'Thông tin cá nhân'], ['summary', 'Mục tiêu nghề nghiệp'], ['experience', 'Kinh nghiệm'], ['education', 'Học vấn'],
  ['skills', 'Kỹ năng'], ['projects', 'Dự án'], ['certifications', 'Chứng chỉ'], ['awards', 'Giải thưởng'], ['activities', 'Hoạt động'],
] as const

const cloneSnapshot = (snapshot: EditorSnapshot): EditorSnapshot => structuredClone(snapshot)
const snapshotSignature = (snapshot: EditorSnapshot) => JSON.stringify({ title: snapshot.title.trim() || 'CV tiếng Việt', templateId: snapshot.templateId, language: 'vi', content: snapshot.content })

export function CvEditorPage() {
  const { id } = useParams()
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const initialTemplate = cvTemplates.some((item) => item.id === params.get('template')) ? params.get('template') as CvTemplateId : 'classic'
  const [title, setTitle] = useState('CV tiếng Việt')
  const [templateId, setTemplateId] = useState<CvTemplateId>(initialTemplate)
  const [content, setContent] = useState<CvContent>(emptyCvContent)
  const [tool, setTool] = useState<ToolId>('add')
  const [mode, setMode] = useState<'edit' | 'preview'>('edit')
  const [templatePickerOpen, setTemplatePickerOpen] = useState(false)
  const [zoom, setZoom] = useState(90)
  const [savedSignature, setSavedSignature] = useState(() => snapshotSignature({ title: 'CV tiếng Việt', templateId: initialTemplate, content: emptyCvContent() }))
  const [loadedId, setLoadedId] = useState<string | null>(null)
  const [historyRevision, setHistoryRevision] = useState(0)
  const profileImportStarted = useRef(false)
  const undoStack = useRef<EditorSnapshot[]>([])
  const redoStack = useRef<EditorSnapshot[]>([])
  const currentRef = useRef<EditorSnapshot>({ title, templateId, content })
  const existing = useQuery({ queryKey: ['candidate-cv', id], queryFn: () => cvApi.get(id!), enabled: Boolean(id) })

  const currentSnapshot: EditorSnapshot = { title, templateId, content }
  currentRef.current = currentSnapshot
  const currentPayload: SaveCvPayload = { title: title.trim() || 'CV tiếng Việt', templateId, language: 'vi', content }
  const signature = snapshotSignature(currentSnapshot)
  const dirty = signature !== savedSignature

  useEffect(() => {
    if (!existing.data || loadedId === existing.data.id) return
    const loaded = { title: existing.data.title, templateId: existing.data.templateId, content: existing.data.content }
    setTitle(loaded.title); setTemplateId(loaded.templateId); setContent(loaded.content)
    setSavedSignature(snapshotSignature(loaded)); setLoadedId(existing.data.id)
    undoStack.current = []; redoStack.current = []; setHistoryRevision((value) => value + 1)
  }, [existing.data, loadedId])

  const checkpoint = useCallback(() => {
    const snapshot = cloneSnapshot(currentRef.current)
    const last = undoStack.current.at(-1)
    if (!last || snapshotSignature(last) !== snapshotSignature(snapshot)) undoStack.current.push(snapshot)
    if (undoStack.current.length > 60) undoStack.current.shift()
    redoStack.current = []
    setHistoryRevision((value) => value + 1)
  }, [])

  const restore = useCallback((snapshot: EditorSnapshot) => {
    setTitle(snapshot.title); setTemplateId(snapshot.templateId); setContent(snapshot.content)
  }, [])
  const undo = useCallback(() => {
    const previous = undoStack.current.pop()
    if (!previous) return
    redoStack.current.push(cloneSnapshot(currentRef.current)); restore(previous); setHistoryRevision((value) => value + 1)
  }, [restore])
  const redo = useCallback(() => {
    const next = redoStack.current.pop()
    if (!next) return
    undoStack.current.push(cloneSnapshot(currentRef.current)); restore(next); setHistoryRevision((value) => value + 1)
  }, [restore])

  const save = useMutation({
    mutationFn: (payload: SaveCvPayload) => id ? cvApi.update(id, payload) : cvApi.create(payload),
    onSuccess: async (saved) => {
      const persisted = { title: saved.title, templateId: saved.templateId, content: saved.content }
      setSavedSignature(snapshotSignature(persisted))
      await queryClient.invalidateQueries({ queryKey: ['candidate-cvs'] })
      await queryClient.invalidateQueries({ queryKey: ['candidate-cv', saved.id] })
      if (!id) navigate(`/cv/${saved.id}/edit`, { replace: true })
    },
  })
  const autofill = useMutation({ mutationFn: () => cvApi.createFromProfile(title.trim() || 'CV từ hồ sơ', templateId), onSuccess: async (saved) => { await queryClient.invalidateQueries({ queryKey: ['candidate-cvs'] }); navigate(`/cv/${saved.id}/edit`, { replace: true }) } })
  const download = useMutation({ mutationFn: async () => { const saved = id ? await cvApi.update(id, currentPayload) : await cvApi.create(currentPayload); return { saved, blob: await cvApi.download(saved.id) } }, onSuccess: ({ saved, blob }) => { saveBlob(blob, `${saved.title}.pdf`); if (!id) navigate(`/cv/${saved.id}/edit`, { replace: true }) } })
  const persist = save.mutate
  const savePending = save.isPending

  useEffect(() => {
    if (id || params.get('source') !== 'profile' || profileImportStarted.current) return
    profileImportStarted.current = true
    autofill.mutate()
  }, [id, params, autofill])

  useEffect(() => {
    if (!dirty || savePending || autofill.isPending || params.get('source') === 'profile' || (id && loadedId !== id)) return
    const timer = window.setTimeout(() => persist({ title: currentRef.current.title.trim() || 'CV tiếng Việt', templateId: currentRef.current.templateId, language: 'vi', content: currentRef.current.content }), 900)
    return () => window.clearTimeout(timer)
  }, [dirty, signature, id, loadedId, params, savePending, autofill.isPending, persist])

  useEffect(() => {
    const warn = (event: BeforeUnloadEvent) => { if (dirty) event.preventDefault() }
    window.addEventListener('beforeunload', warn)
    return () => window.removeEventListener('beforeunload', warn)
  }, [dirty])

  useEffect(() => {
    const keyboardHistory = (event: KeyboardEvent) => {
      if (!(event.ctrlKey || event.metaKey)) return
      if (event.key.toLowerCase() === 'z') { event.preventDefault(); if (event.shiftKey) redo(); else undo() }
      if (event.key.toLowerCase() === 'y') { event.preventDefault(); redo() }
    }
    window.addEventListener('keydown', keyboardHistory)
    return () => window.removeEventListener('keydown', keyboardHistory)
  }, [redo, undo])

  useEffect(() => {
    if (!templatePickerOpen) return
    const closeOnEscape = (event: KeyboardEvent) => { if (event.key === 'Escape') setTemplatePickerOpen(false) }
    window.addEventListener('keydown', closeOnEscape)
    return () => window.removeEventListener('keydown', closeOnEscape)
  }, [templatePickerOpen])

  const scrollTo = (section: string) => document.getElementById(`cv-section-${section}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  const addSectionItem = (section: string) => {
    checkpoint()
    if (section === 'experience') setContent((value) => ({ ...value, experiences: [...value.experiences, { position: '', company: '', startDate: '', endDate: '', description: '' }] }))
    if (section === 'education') setContent((value) => ({ ...value, education: [...value.education, { school: '', degree: '', startDate: '', endDate: '', description: '' }] }))
    if (section === 'skills') setContent((value) => ({ ...value, skills: [...value.skills, ''] }))
    if (section === 'projects') setContent((value) => ({ ...value, projects: [...value.projects, { name: '', url: '', description: '' }] }))
    if (section === 'certifications') setContent((value) => ({ ...value, certifications: [...value.certifications, { name: '', issuer: '', date: '' }] }))
    if (section === 'awards') setContent((value) => ({ ...value, awards: [...value.awards, { name: '', date: '', description: '' }] }))
    if (section === 'activities') setContent((value) => ({ ...value, activities: [...value.activities, { name: '', date: '', description: '' }] }))
    window.setTimeout(() => scrollTo(section), 0)
  }

  if (id && existing.isLoading) return <main className="cv-page"><div className="cv-state">Đang mở CV…</div></main>
  if (id && existing.isError) return <main className="cv-page"><div className="cv-state cv-state--error">Không tìm thấy CV hoặc bạn không có quyền truy cập.</div></main>

  const saveLabel = save.isPending ? 'Đang lưu…' : save.isError ? 'Không thể lưu' : dirty ? 'Có thay đổi chưa lưu' : <><Check /> Đã lưu</>
  const tools: Array<[ToolId, string, typeof Palette]> = [['design', 'Thiết kế & Font', Palette], ['add', 'Thêm mục', Plus], ['layout', 'Bố cục', PanelLeft], ['guide', 'Gợi ý viết CV', Lightbulb], ['library', 'Thư viện CV', Library]]

  return <main className={`cv-editor cv-inline-editor${mode === 'preview' ? ' is-preview-mode' : ''}`}>
    <header className="cv-editor__toolbar">
      <Link to="/cv"><ArrowLeft /> CV của tôi</Link>
      <input aria-label="Tên CV" value={title} maxLength={150} onFocus={checkpoint} onChange={(event) => setTitle(event.target.value)} />
      <button type="button" className="cv-editor__template-trigger" onClick={() => setTemplatePickerOpen(true)}><LayoutTemplate /> {cvTemplates.find((item) => item.id === templateId)?.name}</button>
      <span className={`cv-save-state${save.isError ? ' is-error' : ''}`}>{saveLabel}</span>
      <div className="cv-editor__toolbar-actions">
        <button type="button" aria-label="Hoàn tác" title="Hoàn tác (Ctrl+Z)" onClick={undo} disabled={undoStack.current.length === 0} data-history-revision={historyRevision}><Undo2 /></button>
        <button type="button" aria-label="Làm lại" title="Làm lại (Ctrl+Shift+Z)" onClick={redo} disabled={redoStack.current.length === 0}><Redo2 /></button>
        {!id && params.get('source') !== 'profile' && <button type="button" onClick={() => autofill.mutate()} disabled={autofill.isPending}><Sparkles /> Điền từ hồ sơ</button>}
        <button type="button" onClick={() => setMode((value) => value === 'edit' ? 'preview' : 'edit')}>{mode === 'edit' ? <><Eye /> Xem trước</> : <><Pencil /> Chỉnh sửa</>}</button>
        <button type="button" onClick={() => download.mutate()} disabled={download.isPending}><Download /> {download.isPending ? 'Đang tạo…' : 'PDF'}</button>
        <button className="is-primary" type="button" onClick={() => save.mutate(currentPayload)} disabled={save.isPending}><Save /> {save.isPending ? 'Đang lưu…' : 'Lưu CV'}</button>
      </div>
    </header>
    {(save.isError || autofill.isError || download.isError) && <div className="cv-editor__error">Không thể hoàn tất thao tác. Vui lòng kiểm tra dữ liệu và thử lại.</div>}
    {mode === 'preview' && <div className="cv-preview-mode-banner"><Eye /> Chế độ xem trước — mọi công cụ chỉnh sửa đã được ẩn.<button type="button" onClick={() => setMode('edit')}>Quay lại chỉnh sửa</button></div>}
    <div className="cv-inline-workspace">
      {mode === 'edit' && <aside className="cv-tool-sidebar" aria-label="Công cụ CV">
        <nav>{tools.map(([idValue, label, Icon]) => <button type="button" className={tool === idValue ? 'is-active' : ''} aria-pressed={tool === idValue} onClick={() => setTool(idValue)} key={idValue}><Icon /><span>{label}</span></button>)}</nav>
        <div className="cv-tool-panel">
          {tool === 'design' && <><h2>Thiết kế & Font</h2><p>Đổi mẫu mà không làm mất nội dung.</p><button className="cv-tool-action" type="button" onClick={() => setTemplatePickerOpen(true)}><LayoutTemplate /> Đổi mẫu CV</button><label>Ngôn ngữ<select disabled aria-label="Ngôn ngữ CV"><option>Tiếng Việt</option></select></label><div className="cv-tool-zoom"><span>Kích thước hiển thị</span><div><button type="button" aria-label="Thu nhỏ" onClick={() => setZoom((value) => Math.max(60, value - 10))}><Minus /></button><output>{zoom}%</output><button type="button" aria-label="Phóng to" onClick={() => setZoom((value) => Math.min(110, value + 10))}><Plus /></button></div></div></>}
          {tool === 'add' && <><h2>Thêm mục</h2><p>Mục mới xuất hiện ngay trên CV để bạn nhập trực tiếp.</p><div className="cv-tool-list">{sections.map(([section, label]) => <button type="button" onClick={() => section === 'personal' || section === 'summary' ? scrollTo(section) : addSectionItem(section)} key={section}><Plus /> {label}</button>)}</div></>}
          {tool === 'layout' && <><h2>Bố cục</h2><p>Đi tới và chỉnh trực tiếp từng phần.</p><div className="cv-tool-list">{sections.map(([section, label]) => <button type="button" onClick={() => scrollTo(section)} key={section}><PanelLeft /> {label}</button>)}</div></>}
          {tool === 'guide' && <><h2>Gợi ý viết CV</h2><p>Nhấp vào đoạn văn trên CV, viết ngắn gọn và ưu tiên kết quả có số liệu.</p><ul><li>Dùng động từ hành động.</li><li>Giữ tên công nghệ bằng tiếng Anh.</li><li>Mỗi thành tựu nên có kết quả cụ thể.</li></ul></>}
          {tool === 'library' && <><h2>Thư viện CV</h2><p>Quản lý CV đã tạo hoặc bắt đầu từ một thiết kế khác.</p><Link className="cv-tool-action" to="/cv">CV của tôi</Link><Link className="cv-tool-action" to="/cv/templates">Thư viện mẫu</Link></>}
        </div>
      </aside>}
      <section className="cv-inline-stage" aria-label={mode === 'edit' ? 'Vùng chỉnh sửa CV trực tiếp' : 'Vùng xem trước CV'}>
        {mode === 'edit' && <div className="cv-inline-hint"><Pencil /> Nhấp trực tiếp vào bất kỳ nội dung nào để chỉnh sửa</div>}
        <div className="cv-inline-canvas" style={{ '--cv-zoom': zoom / 100 } as CSSProperties}><CvPreview content={content} templateId={templateId} editor={mode === 'edit' ? { onChange: setContent, onCheckpoint: checkpoint } : undefined} /></div>
      </section>
    </div>
    {templatePickerOpen && <div className="cv-template-dialog" role="dialog" aria-modal="true" aria-labelledby="template-dialog-title" onMouseDown={(event) => { if (event.target === event.currentTarget) setTemplatePickerOpen(false) }}><div><header><div><span>Đổi giao diện</span><h2 id="template-dialog-title">Chọn mẫu CV</h2><p>Nội dung đang nhập được giữ nguyên khi đổi mẫu.</p></div><button type="button" onClick={() => setTemplatePickerOpen(false)} aria-label="Đóng">×</button></header><div className="cv-template-dialog__grid">{cvTemplates.map((template) => <button type="button" className={template.id === templateId ? 'is-active' : ''} onClick={() => { checkpoint(); setTemplateId(template.id); setTemplatePickerOpen(false) }} key={template.id}><span className={`cv-template-swatch cv-template-swatch--${template.id}`} /><strong>{template.name}</strong><small>{template.style}</small>{template.id === templateId && <Check />}</button>)}</div></div></div>}
  </main>
}
