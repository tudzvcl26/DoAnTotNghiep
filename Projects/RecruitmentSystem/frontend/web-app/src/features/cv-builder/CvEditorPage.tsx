import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Check, Download, Eye, EyeOff, LayoutTemplate, Library, Minus, MoveDown, MoveUp, Palette, PanelLeft, Pencil, Plus, Redo2, Save, SlidersHorizontal, Sparkles, TextCursorInput, Undo2 } from 'lucide-react'
import { type CSSProperties, useCallback, useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { cvApi, saveBlob } from './cv.api'
import { CvPreview } from './components/CvPreview'
import { cvTemplates } from './cv.templates'
import { builtInSectionIds, cvThemes, emptyCvContent, type CvContent, type CvDensity, type CvDesignConfig, type CvFontFamily, type CvLanguage, type CvLayout, type CvTemplateId, type SaveCvPayload } from './cv.types'
import './cv-builder.css'

type ToolId = 'design' | 'add' | 'layout' | 'color' | 'spacing' | 'library'
type EditorSnapshot = { title: string; templateId: CvTemplateId; language: CvLanguage; content: CvContent }
const sectionLabels: Record<string, string> = { summary: 'Mục tiêu nghề nghiệp', experience: 'Kinh nghiệm làm việc', education: 'Học vấn', skills: 'Kỹ năng', projects: 'Dự án', certifications: 'Chứng chỉ', activities: 'Hoạt động', awards: 'Giải thưởng' }
const fonts: CvFontFamily[] = ['Roboto', 'Inter', 'Arial', 'Times New Roman', 'Georgia', 'Open Sans']
const fontScales = [{ value: .9, label: 'Nhỏ' }, { value: 1, label: 'Vừa' }, { value: 1.1, label: 'Lớn' }]
const densities: Array<{ value: CvDensity; label: string; hint: string }> = [{ value: 'compact', label: 'Gọn', hint: 'Nhiều nội dung' }, { value: 'normal', label: 'Chuẩn', hint: 'Cân bằng' }, { value: 'comfortable', label: 'Thoáng', hint: 'Dễ đọc' }]
const layouts: Array<{ value: CvLayout; label: string; hint: string }> = [{ value: 'single', label: 'Một cột', hint: 'Tuyến tính, ATS' }, { value: 'header', label: 'Header nổi bật', hint: 'Tên và liên hệ rõ ràng' }, { value: 'sidebar-left', label: 'Sidebar trái', hint: 'Kỹ năng ở bên trái' }, { value: 'sidebar-right', label: 'Sidebar phải', hint: 'Kỹ năng ở bên phải' }]
const zoomLevels = [50, 60, 75, 85, 100, 110]

const cloneSnapshot = (snapshot: EditorSnapshot): EditorSnapshot => structuredClone(snapshot)
const snapshotSignature = (snapshot: EditorSnapshot) => JSON.stringify({ title: snapshot.title.trim() || 'CV tiếng Việt', templateId: snapshot.templateId, language: snapshot.language, content: snapshot.content })

export function CvEditorPage() {
  const { id } = useParams()
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const initialTemplate = cvTemplates.some((item) => item.id === params.get('template')) ? params.get('template') as CvTemplateId : 'classic'
  const initialContent = emptyCvContent(initialTemplate)
  const [title, setTitle] = useState('CV tiếng Việt')
  const [templateId, setTemplateId] = useState<CvTemplateId>(initialTemplate)
  const [language, setLanguage] = useState<CvLanguage>('vi')
  const [content, setContent] = useState<CvContent>(() => initialContent)
  const [tool, setTool] = useState<ToolId>('design')
  const [mode, setMode] = useState<'edit' | 'preview'>('edit')
  const [templatePickerOpen, setTemplatePickerOpen] = useState(false)
  const [zoom, setZoom] = useState(85)
  const [savedSignature, setSavedSignature] = useState(() => snapshotSignature({ title: 'CV tiếng Việt', templateId: initialTemplate, language: 'vi', content: initialContent }))
  const [loadedId, setLoadedId] = useState<string | null>(null)
  const [historyRevision, setHistoryRevision] = useState(0)
  const profileImportStarted = useRef(false)
  const undoStack = useRef<EditorSnapshot[]>([])
  const redoStack = useRef<EditorSnapshot[]>([])
  const currentRef = useRef<EditorSnapshot>({ title, templateId, language, content })
  const existing = useQuery({ queryKey: ['candidate-cv', id], queryFn: () => cvApi.get(id!), enabled: Boolean(id) })

  const currentSnapshot: EditorSnapshot = { title, templateId, language, content }
  currentRef.current = currentSnapshot
  const currentPayload: SaveCvPayload = { title: title.trim() || 'CV tiếng Việt', templateId, language, content }
  const signature = snapshotSignature(currentSnapshot)
  const dirty = signature !== savedSignature

  useEffect(() => {
    if (!existing.data || loadedId === existing.data.id) return
    const loaded = { title: existing.data.title, templateId: existing.data.templateId, language: existing.data.language, content: existing.data.content }
    setTitle(loaded.title); setTemplateId(loaded.templateId); setLanguage(loaded.language); setContent(loaded.content)
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
  const restore = useCallback((snapshot: EditorSnapshot) => { setTitle(snapshot.title); setTemplateId(snapshot.templateId); setLanguage(snapshot.language); setContent(snapshot.content) }, [])
  const undo = useCallback(() => { const previous = undoStack.current.pop(); if (!previous) return; redoStack.current.push(cloneSnapshot(currentRef.current)); restore(previous); setHistoryRevision((value) => value + 1) }, [restore])
  const redo = useCallback(() => { const next = redoStack.current.pop(); if (!next) return; undoStack.current.push(cloneSnapshot(currentRef.current)); restore(next); setHistoryRevision((value) => value + 1) }, [restore])

  const save = useMutation({
    mutationFn: (payload: SaveCvPayload) => id ? cvApi.update(id, payload) : cvApi.create(payload),
    onSuccess: async (saved) => {
      const persisted = { title: saved.title, templateId: saved.templateId, language: saved.language, content: saved.content }
      setSavedSignature(snapshotSignature(persisted))
      await queryClient.invalidateQueries({ queryKey: ['candidate-cvs'] }); await queryClient.invalidateQueries({ queryKey: ['candidate-cv', saved.id] })
      if (!id) navigate(`/cv/${saved.id}/edit`, { replace: true })
    },
  })
  const autofill = useMutation({ mutationFn: () => cvApi.createFromProfile(title.trim() || 'CV từ hồ sơ', templateId), onSuccess: async (saved) => { await queryClient.invalidateQueries({ queryKey: ['candidate-cvs'] }); navigate(`/cv/${saved.id}/edit`, { replace: true }) } })
  const download = useMutation({ mutationFn: async () => { const saved = id ? await cvApi.update(id, currentPayload) : await cvApi.create(currentPayload); return { saved, blob: await cvApi.download(saved.id) } }, onSuccess: ({ saved, blob }) => { saveBlob(blob, `${saved.title}.pdf`); if (!id) navigate(`/cv/${saved.id}/edit`, { replace: true }) } })
  const persist = save.mutate

  useEffect(() => { if (id || params.get('source') !== 'profile' || profileImportStarted.current) return; profileImportStarted.current = true; autofill.mutate() }, [id, params, autofill])
  useEffect(() => {
    if (!dirty || save.isPending || autofill.isPending || params.get('source') === 'profile' || (id && loadedId !== id)) return
    const timer = window.setTimeout(() => { const current = currentRef.current; persist({ title: current.title.trim() || 'CV tiếng Việt', templateId: current.templateId, language: current.language, content: current.content }) }, 900)
    return () => window.clearTimeout(timer)
  }, [dirty, signature, id, loadedId, params, save.isPending, autofill.isPending, persist])
  useEffect(() => { const warn = (event: BeforeUnloadEvent) => { if (dirty) event.preventDefault() }; window.addEventListener('beforeunload', warn); return () => window.removeEventListener('beforeunload', warn) }, [dirty])
  useEffect(() => { const keyboardHistory = (event: KeyboardEvent) => { if (!(event.ctrlKey || event.metaKey)) return; if (event.key.toLowerCase() === 'z') { event.preventDefault(); if (event.shiftKey) redo(); else undo() } if (event.key.toLowerCase() === 'y') { event.preventDefault(); redo() } }; window.addEventListener('keydown', keyboardHistory); return () => window.removeEventListener('keydown', keyboardHistory) }, [redo, undo])
  useEffect(() => { if (!templatePickerOpen) return; const closeOnEscape = (event: KeyboardEvent) => { if (event.key === 'Escape') setTemplatePickerOpen(false) }; window.addEventListener('keydown', closeOnEscape); return () => window.removeEventListener('keydown', closeOnEscape) }, [templatePickerOpen])

  const updateDesign = (patch: Partial<CvDesignConfig>) => { checkpoint(); setContent((value) => ({ ...value, designConfig: { ...value.designConfig, ...patch } })) }
  const scrollTo = (section: string) => document.getElementById(`cv-section-${section}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  const ensureVisible = (section: string) => {
    checkpoint()
    setContent((value) => ({ ...value, designConfig: { ...value.designConfig, sectionOrder: value.designConfig.sectionOrder.includes(section) ? value.designConfig.sectionOrder : [...value.designConfig.sectionOrder, section], sectionVisibility: { ...value.designConfig.sectionVisibility, [section]: true } } }))
    window.setTimeout(() => scrollTo(section), 0)
  }
  const addSectionItem = (section: string) => {
    checkpoint()
    setContent((value) => {
      const designConfig = { ...value.designConfig, sectionVisibility: { ...value.designConfig.sectionVisibility, [section]: true } }
      if (section === 'experience') return { ...value, designConfig, experiences: [...value.experiences, { position: '', company: '', startDate: '', endDate: '', description: '' }] }
      if (section === 'education') return { ...value, designConfig, education: [...value.education, { school: '', degree: '', startDate: '', endDate: '', description: '' }] }
      if (section === 'skills') return { ...value, designConfig, skills: [...value.skills, ''] }
      if (section === 'projects') return { ...value, designConfig, projects: [...value.projects, { name: '', url: '', description: '' }] }
      if (section === 'certifications') return { ...value, designConfig, certifications: [...value.certifications, { name: '', issuer: '', date: '' }] }
      if (section === 'awards') return { ...value, designConfig, awards: [...value.awards, { name: '', date: '', description: '' }] }
      if (section === 'activities') return { ...value, designConfig, activities: [...value.activities, { name: '', date: '', description: '' }] }
      return { ...value, designConfig }
    })
    window.setTimeout(() => scrollTo(section), 0)
  }
  const addCustomSection = (titleValue: string) => {
    checkpoint(); const customId = crypto.randomUUID()
    setContent((value) => ({ ...value, customSections: [...value.customSections, { id: customId, title: titleValue, visible: true, items: [{ name: '', date: '', description: '' }] }], designConfig: { ...value.designConfig, sectionOrder: [...value.designConfig.sectionOrder, `custom:${customId}`] } }))
    window.setTimeout(() => scrollTo(`custom:${customId}`), 0)
  }
  const moveOrder = (section: string, direction: -1 | 1) => {
    const order = content.designConfig.sectionOrder; const index = order.indexOf(section); const target = index + direction
    if (index < 0 || target < 0 || target >= order.length) return
    const next = [...order]; [next[index], next[target]] = [next[target], next[index]]; updateDesign({ sectionOrder: next })
  }

  if (id && existing.isLoading) return <main className="cv-page"><div className="cv-state">Đang mở CV…</div></main>
  if (id && existing.isError) return <main className="cv-page"><div className="cv-state cv-state--error">Không tìm thấy CV hoặc bạn không có quyền truy cập.</div></main>

  const saveLabel = save.isPending ? 'Đang lưu…' : save.isError ? 'Không thể lưu' : dirty ? 'Có thay đổi chưa lưu' : <><Check /> Đã lưu</>
  const tools: Array<[ToolId, string, typeof Palette]> = [['design', 'Thiết kế & Font', TextCursorInput], ['add', 'Thêm mục', Plus], ['layout', 'Bố cục', PanelLeft], ['color', 'Màu CV', Palette], ['spacing', 'Khoảng cách', SlidersHorizontal], ['library', 'Thư viện CV', Library]]

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
        <button type="button" onClick={() => setMode((value) => value === 'edit' ? 'preview' : 'edit')}>{mode === 'edit' ? <><Eye /> Xem trước</> : <><Pencil /> Nội dung</>}</button>
        <button type="button" onClick={() => download.mutate()} disabled={download.isPending}><Download /> {download.isPending ? 'Đang tạo…' : 'PDF'}</button>
        <button className="is-primary" type="button" onClick={() => save.mutate(currentPayload)} disabled={save.isPending}><Save /> {save.isPending ? 'Đang lưu…' : 'Lưu CV'}</button>
      </div>
    </header>
    {(save.isError || autofill.isError || download.isError) && <div className="cv-editor__error">Không thể hoàn tất thao tác. Vui lòng kiểm tra dữ liệu và thử lại.</div>}
    {mode === 'preview' && <div className="cv-preview-mode-banner"><Eye /> Chế độ xem trước — mọi công cụ chỉnh sửa đã được ẩn.<button type="button" onClick={() => setMode('edit')}>Quay lại chỉnh sửa</button></div>}
    <div className="cv-inline-workspace">
      {mode === 'edit' && <aside className="cv-tool-sidebar" aria-label="Công cụ CV">
        <nav>{tools.map(([toolId, label, Icon]) => <button type="button" className={tool === toolId ? 'is-active' : ''} aria-pressed={tool === toolId} onClick={() => setTool(toolId)} key={toolId}><Icon /><span>{label}</span></button>)}</nav>
        <div className="cv-tool-panel">
          {tool === 'design' && <DesignPanel language={language} design={content.designConfig} zoom={zoom} onLanguage={(next) => { checkpoint(); setLanguage(next) }} onDesign={updateDesign} onZoom={setZoom} onTemplate={() => setTemplatePickerOpen(true)} />}
          {tool === 'add' && <><h2>Thêm mục</h2><p>Mục được bật và xuất hiện ngay trên CV để bạn nhập trực tiếp.</p><div className="cv-tool-list">{builtInSectionIds.map((section) => <button type="button" onClick={() => section === 'summary' ? ensureVisible(section) : addSectionItem(section)} key={section}><Plus /> {sectionLabels[section]}</button>)}</div><h3 className="cv-tool-subtitle">Mục mở rộng</h3><div className="cv-tool-list"><button type="button" onClick={() => addCustomSection('Sở thích')}><Plus /> Sở thích</button><button type="button" onClick={() => addCustomSection('Người tham chiếu')}><Plus /> Người tham chiếu</button><button type="button" onClick={() => addCustomSection('Thông tin khác')}><Plus /> Thông tin khác</button></div></>}
          {tool === 'layout' && <LayoutPanel design={content.designConfig} customTitles={Object.fromEntries(content.customSections.map((section) => [`custom:${section.id}`, section.title]))} onDesign={updateDesign} onMove={moveOrder} onShow={ensureVisible} />}
          {tool === 'color' && <ColorPanel selected={content.designConfig.theme.id} onSelect={(themeId) => updateDesign({ theme: { ...(cvThemes.find((theme) => theme.id === themeId) ?? cvThemes[0]) } })} />}
          {tool === 'spacing' && <SpacingPanel design={content.designConfig} onDesign={updateDesign} />}
          {tool === 'library' && <><h2>Thư viện CV</h2><p>Đổi template không làm mất nội dung, màu, font hoặc thứ tự section.</p><button className="cv-tool-action" type="button" onClick={() => setTemplatePickerOpen(true)}><LayoutTemplate /> Đổi mẫu CV</button><Link className="cv-tool-action" to="/cv">CV của tôi</Link><Link className="cv-tool-action" to="/cv/templates">Thư viện mẫu</Link></>}
        </div>
      </aside>}
      <section className="cv-inline-stage" aria-label={mode === 'edit' ? 'Vùng chỉnh sửa CV trực tiếp' : 'Vùng xem trước CV'}>
        {mode === 'edit' && <div className="cv-inline-hint"><Pencil /> Nhấp trực tiếp vào nội dung • rê chuột vào section để sắp xếp</div>}
        <div className="cv-inline-canvas" style={{ '--cv-zoom': zoom / 100 } as CSSProperties}><CvPreview content={content} templateId={templateId} language={language} editor={mode === 'edit' ? { onChange: setContent, onCheckpoint: checkpoint } : undefined} /></div>
      </section>
    </div>
    {templatePickerOpen && <div className="cv-template-dialog" role="dialog" aria-modal="true" aria-labelledby="template-dialog-title" onMouseDown={(event) => { if (event.target === event.currentTarget) setTemplatePickerOpen(false) }}><div><header><div><span>Đổi giao diện</span><h2 id="template-dialog-title">Chọn mẫu CV</h2><p>Nội dung và toàn bộ thiết lập thiết kế đang nhập được giữ nguyên.</p></div><button type="button" onClick={() => setTemplatePickerOpen(false)} aria-label="Đóng">×</button></header><div className="cv-template-dialog__grid">{cvTemplates.map((template) => <button type="button" className={template.id === templateId ? 'is-active' : ''} onClick={() => { checkpoint(); setTemplateId(template.id); setTemplatePickerOpen(false) }} key={template.id}><span className={`cv-template-swatch cv-template-swatch--${template.id}`} /><strong>{template.name}</strong><small>{template.style}</small>{template.id === templateId && <Check />}</button>)}</div></div></div>}
  </main>
}

function DesignPanel({ language, design, zoom, onLanguage, onDesign, onZoom, onTemplate }: { language: CvLanguage; design: CvDesignConfig; zoom: number; onLanguage: (language: CvLanguage) => void; onDesign: (patch: Partial<CvDesignConfig>) => void; onZoom: (zoom: number) => void; onTemplate: () => void }) {
  return <><h2>Thiết kế & Font</h2><p>Typography cập nhật trực tiếp nhưng không thay đổi nội dung.</p><button className="cv-tool-action" type="button" onClick={onTemplate}><LayoutTemplate /> Đổi mẫu CV</button><fieldset className="cv-control-group"><legend>Ngôn ngữ CV</legend><div className="cv-segmented"><button type="button" className={language === 'vi' ? 'is-selected' : ''} onClick={() => onLanguage('vi')}>Tiếng Việt</button><button type="button" className={language === 'en' ? 'is-selected' : ''} onClick={() => onLanguage('en')}>English</button></div></fieldset><label className="cv-control-label">Font chữ<select aria-label="Font chữ CV" value={design.fontFamily} onChange={(event) => onDesign({ fontFamily: event.target.value as CvFontFamily })}>{fonts.map((font) => <option value={font} key={font}>{font}</option>)}</select></label><fieldset className="cv-control-group"><legend>Cỡ chữ</legend><div className="cv-segmented">{fontScales.map((option) => <button type="button" className={design.fontScale === option.value ? 'is-selected' : ''} onClick={() => onDesign({ fontScale: option.value })} key={option.value}>{option.label}</button>)}</div></fieldset><fieldset className="cv-control-group"><legend>Zoom tài liệu</legend><div className="cv-zoom-presets">{zoomLevels.map((level) => <button type="button" className={zoom === level ? 'is-selected' : ''} onClick={() => onZoom(level)} key={level}>{level}%</button>)}</div><div className="cv-tool-zoom"><button type="button" aria-label="Thu nhỏ" onClick={() => onZoom(Math.max(50, zoom - 10))}><Minus /></button><output>{zoom}%</output><button type="button" aria-label="Phóng to" onClick={() => onZoom(Math.min(110, zoom + 10))}><Plus /></button></div></fieldset></>
}

function ColorPanel({ selected, onSelect }: { selected: string; onSelect: (theme: string) => void }) { return <><h2>Màu CV</h2><p>Một palette đồng bộ header, tiêu đề, đường nhấn và sidebar.</p><div className="cv-color-grid">{cvThemes.map((theme) => <button type="button" className={selected === theme.id ? 'is-selected' : ''} aria-label={`Chọn màu ${theme.id}`} onClick={() => onSelect(theme.id)} key={theme.id}><span style={{ background: theme.primaryColor }} /><strong>{theme.id}</strong>{selected === theme.id && <Check />}</button>)}</div></> }

function SpacingPanel({ design, onDesign }: { design: CvDesignConfig; onDesign: (patch: Partial<CvDesignConfig>) => void }) { return <><h2>Khoảng cách</h2><p>Điều chỉnh nhịp đoạn, section và item mà vẫn giữ khổ A4.</p><div className="cv-density-list">{densities.map((density) => <button type="button" className={design.density === density.value ? 'is-selected' : ''} onClick={() => onDesign({ density: density.value })} key={density.value}><span className={`cv-density-icon is-${density.value}`}><i /><i /><i /></span><span><strong>{density.label}</strong><small>{density.hint}</small></span>{design.density === density.value && <Check />}</button>)}</div></> }

function LayoutPanel({ design, customTitles, onDesign, onMove, onShow }: { design: CvDesignConfig; customTitles: Record<string, string>; onDesign: (patch: Partial<CvDesignConfig>) => void; onMove: (section: string, direction: -1 | 1) => void; onShow: (section: string) => void }) { return <><h2>Bố cục</h2><p>Chọn cấu trúc tài liệu và quản lý thứ tự/hiển thị section.</p><div className="cv-layout-grid">{layouts.map((layout) => <button type="button" className={design.layout === layout.value ? 'is-selected' : ''} onClick={() => onDesign({ layout: layout.value })} key={layout.value}><span className={`cv-layout-thumb is-${layout.value}`}><i /><i /></span><strong>{layout.label}</strong><small>{layout.hint}</small></button>)}</div><h3 className="cv-tool-subtitle">Thứ tự section</h3><div className="cv-section-order-list">{design.sectionOrder.map((section, index) => { const hidden = design.sectionVisibility[section] === false; return <div className={hidden ? 'is-hidden' : ''} key={section}><span>{customTitles[section] ?? sectionLabels[section] ?? 'Mục bổ sung'}</span><button type="button" aria-label={`Di chuyển ${customTitles[section] ?? sectionLabels[section]} lên`} disabled={index === 0} onClick={() => onMove(section, -1)}><MoveUp /></button><button type="button" aria-label={`Di chuyển ${customTitles[section] ?? sectionLabels[section]} xuống`} disabled={index === design.sectionOrder.length - 1} onClick={() => onMove(section, 1)}><MoveDown /></button>{hidden && <button type="button" aria-label={`Hiện ${customTitles[section] ?? sectionLabels[section]}`} onClick={() => onShow(section)}><EyeOff /> Hiện</button>}</div> })}</div></> }
