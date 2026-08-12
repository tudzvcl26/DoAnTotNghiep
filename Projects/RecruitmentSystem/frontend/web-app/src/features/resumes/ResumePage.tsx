import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  AlertCircle, CheckCircle2, Download, Eye, FileCheck2, FileText, LoaderCircle,
  RefreshCw, ShieldCheck, Trash2, UploadCloud, X,
} from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Button } from '../../components/ui/Button'
import { AppError, getErrorMessage } from '../../lib/api/error-adapter'
import type { ResumeAsset } from '../../types/models/resume'
import { useAuth } from '../auth/auth-context'
import { getCurrentResume } from '../candidate/candidate.api'
import { deleteResume, downloadResume, getResumes, uploadResume } from './resumes.api'
import './resume-page.css'

const MAX_FILE_BYTES = 10 * 1024 * 1024
const ACCEPTED_TYPES = new Set([
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
])
const ACCEPTED_EXTENSIONS = new Set(['pdf', 'doc', 'docx'])

function isNotFound(error: unknown) {
  return error instanceof AppError && error.status === 404
}

function formatFileSize(bytes: number) {
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  }).format(new Date(value))
}

function validateFile(file: File | null): string | null {
  if (!file) return 'Vui lòng chọn một tệp CV.'
  const extension = file.name.split('.').pop()?.toLowerCase() ?? ''
  if (!ACCEPTED_EXTENSIONS.has(extension) || !ACCEPTED_TYPES.has(file.type)) {
    return 'CV phải là tệp PDF, DOC hoặc DOCX hợp lệ.'
  }
  if (file.size <= 0) return 'Tệp CV đang trống.'
  if (file.size > MAX_FILE_BYTES) return 'CV không được vượt quá 10 MB.'
  return null
}

function ResumeSkeleton() {
  return <div className="resume-skeleton" aria-label="Đang tải danh sách CV"><span /><span /><span /></div>
}

function ErrorState({ error, retry }: { error: unknown; retry: () => void }) {
  return (
    <div className="resume-error" role="alert">
      <AlertCircle size={22} aria-hidden="true" />
      <div><strong>Chưa thể tải dữ liệu CV</strong><p>{getErrorMessage(error)}</p></div>
      <button type="button" onClick={retry}><RefreshCw size={16} /> Thử lại</button>
    </div>
  )
}

type ResumeCardProps = {
  resume: ResumeAsset
  current?: boolean
  busyAction: string | null
  onView: (resume: ResumeAsset) => void
  onDownload: (resume: ResumeAsset) => void
  onDelete: (resume: ResumeAsset) => void
}

function ResumeCard({ resume, current, busyAction, onView, onDownload, onDelete }: ResumeCardProps) {
  const isBusy = busyAction?.endsWith(resume.id) ?? false
  return (
    <article className={`resume-card${current ? ' resume-card--current' : ''}`}>
      <span className="resume-card__icon"><FileText size={26} aria-hidden="true" /></span>
      <div className="resume-card__body">
        <div className="resume-card__title">
          <h3 title={resume.originalFilename}>{resume.originalFilename}</h3>
          {current && <span><FileCheck2 size={14} /> CV hiện tại</span>}
        </div>
        <dl>
          <div><dt>Định dạng</dt><dd>{resume.contentType === 'application/pdf' ? 'PDF' : resume.originalFilename.split('.').pop()?.toUpperCase()}</dd></div>
          <div><dt>Dung lượng</dt><dd>{formatFileSize(resume.sizeBytes)}</dd></div>
          <div><dt>Phiên bản</dt><dd>{resume.assetVersion}</dd></div>
          <div><dt>Tải lên</dt><dd>{formatDate(resume.createdAt)}</dd></div>
        </dl>
        <div className="resume-card__actions">
          {resume.contentType === 'application/pdf' && (
            <button type="button" onClick={() => onView(resume)} disabled={isBusy}><Eye size={16} /> Xem</button>
          )}
          <button type="button" onClick={() => onDownload(resume)} disabled={isBusy}><Download size={16} /> Tải xuống</button>
          <button className="resume-card__delete" type="button" onClick={() => onDelete(resume)} disabled={isBusy}><Trash2 size={16} /> Xóa</button>
        </div>
      </div>
    </article>
  )
}

export function ResumePage() {
  const { currentUser } = useAuth()
  const userId = currentUser?.id ?? ''
  const queryClient = useQueryClient()
  const inputRef = useRef<HTMLInputElement>(null)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [validationError, setValidationError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [busyAction, setBusyAction] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [preview, setPreview] = useState<{ name: string; url: string } | null>(null)

  useEffect(() => () => {
    if (preview) URL.revokeObjectURL(preview.url)
  }, [preview])

  const resumesQuery = useQuery({
    queryKey: ['candidate-resumes', userId],
    queryFn: () => getResumes(userId),
    enabled: Boolean(userId),
  })
  const currentQuery = useQuery({
    queryKey: ['candidate-current-resume', userId],
    queryFn: () => getCurrentResume(userId),
    enabled: Boolean(userId),
    retry: (count, error) => !isNotFound(error) && count < 1,
  })

  const invalidateResumeQueries = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['candidate-resumes', userId] }),
      queryClient.invalidateQueries({ queryKey: ['candidate-current-resume', userId] }),
      queryClient.invalidateQueries({ queryKey: ['candidate-profile'] }),
    ])
  }

  const uploadMutation = useMutation({
    mutationFn: (file: File) => uploadResume(userId, file),
    onSuccess: async (resume) => {
      setSelectedFile(null)
      setValidationError(null)
      setActionError(null)
      setSuccessMessage(`${resume.originalFilename} đã được tải lên và trở thành CV hiện tại.`)
      if (inputRef.current) inputRef.current.value = ''
      await invalidateResumeQueries()
    },
    onError: (error) => {
      setSuccessMessage(null)
      setActionError(getErrorMessage(error))
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (resume: ResumeAsset) => deleteResume(userId, resume.id),
    onSuccess: async (_, resume) => {
      setActionError(null)
      setSuccessMessage(`${resume.originalFilename} đã được xóa.`)
      await invalidateResumeQueries()
    },
    onError: (error) => {
      setSuccessMessage(null)
      setActionError(getErrorMessage(error))
    },
    onSettled: () => setBusyAction(null),
  })

  const handleFileChange = (file: File | null) => {
    setSuccessMessage(null)
    setActionError(null)
    const error = validateFile(file)
    setValidationError(error)
    setSelectedFile(error ? null : file)
  }

  const handleUpload = () => {
    const error = validateFile(selectedFile)
    setValidationError(error)
    if (!error && selectedFile) uploadMutation.mutate(selectedFile)
  }

  const handleFileAction = async (resume: ResumeAsset, mode: 'view' | 'download') => {
    const actionKey = `${mode}-${resume.id}`
    setBusyAction(actionKey)
    setActionError(null)
    try {
      const blob = await downloadResume(userId, resume.id)
      const objectUrl = URL.createObjectURL(blob)
      if (mode === 'view') {
        setPreview({ name: resume.originalFilename, url: objectUrl })
      } else {
        const anchor = document.createElement('a')
        anchor.href = objectUrl
        anchor.download = resume.originalFilename
        document.body.appendChild(anchor)
        anchor.click()
        anchor.remove()
        window.setTimeout(() => URL.revokeObjectURL(objectUrl), 60_000)
      }
    } catch (error) {
      setActionError(getErrorMessage(error))
    } finally {
      setBusyAction(null)
    }
  }

  const handleDelete = (resume: ResumeAsset) => {
    const detail = currentQuery.data?.id === resume.id
      ? 'Đây là CV hiện tại. Sau khi xóa, bạn sẽ chưa có CV để ứng tuyển cho đến khi tải CV mới lên.'
      : 'Thao tác này không thể hoàn tác.'
    if (!window.confirm(`Bạn có chắc muốn xóa “${resume.originalFilename}”?\n\n${detail}`)) return
    setBusyAction(`delete-${resume.id}`)
    deleteMutation.mutate(resume)
  }

  const resumes = resumesQuery.data?.content ?? []
  const currentMissing = currentQuery.isError && isNotFound(currentQuery.error)
  const currentResume = currentMissing ? undefined : currentQuery.data
  const otherResumes = resumes.filter((resume) => resume.id !== currentResume?.id)

  return (
    <main className="resume-page">
      <header className="resume-page__header">
        <div><span>Candidate Portal</span><h1>CV của tôi</h1><p>Quản lý hồ sơ CV dùng khi ứng tuyển.</p></div>
        <span className="resume-page__secure"><ShieldCheck size={18} /> Tệp được bảo vệ</span>
      </header>

      <section className="resume-upload" aria-labelledby="resume-upload-title">
        <div className="resume-upload__intro">
          <span><UploadCloud size={25} /></span>
          <div><h2 id="resume-upload-title">Tải CV lên</h2><p>PDF, DOC hoặc DOCX · Tối đa 10 MB. CV mới sẽ tự động trở thành CV hiện tại.</p></div>
        </div>
        <div className="resume-upload__controls">
          <label className="resume-file-picker" htmlFor="resume-file">
            <input
              ref={inputRef}
              id="resume-file"
              type="file"
              accept=".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
              onChange={(event) => handleFileChange(event.target.files?.[0] ?? null)}
              disabled={uploadMutation.isPending}
            />
            <FileText size={18} /> {selectedFile ? 'Chọn tệp khác' : 'Chọn CV từ thiết bị'}
          </label>
          {selectedFile && <div className="resume-selected-file"><FileCheck2 size={18} /><span><strong>{selectedFile.name}</strong><small>{formatFileSize(selectedFile.size)}</small></span></div>}
          <Button type="button" onClick={handleUpload} disabled={!selectedFile || uploadMutation.isPending}>
            {uploadMutation.isPending ? <><LoaderCircle className="resume-spin" size={17} /> Đang tải lên...</> : <><UploadCloud size={17} /> Tải CV lên</>}
          </Button>
        </div>
        <div className="resume-feedback" aria-live="polite">
          {validationError && <p className="resume-feedback--error"><AlertCircle size={16} /> {validationError}</p>}
          {actionError && <p className="resume-feedback--error"><AlertCircle size={16} /> {actionError}</p>}
          {successMessage && <p className="resume-feedback--success"><CheckCircle2 size={16} /> {successMessage}</p>}
        </div>
      </section>

      {(resumesQuery.isLoading || currentQuery.isLoading) && <ResumeSkeleton />}
      {resumesQuery.isError && <ErrorState error={resumesQuery.error} retry={() => void resumesQuery.refetch()} />}
      {currentQuery.isError && !currentMissing && <ErrorState error={currentQuery.error} retry={() => void currentQuery.refetch()} />}

      {resumesQuery.isSuccess && resumes.length === 0 && currentMissing && (
        <section className="resume-empty">
          <span><FileText size={34} /></span><h2>Bạn chưa có CV</h2><p>Một CV hoàn chỉnh giúp bạn sẵn sàng ứng tuyển.</p>
          <Button type="button" onClick={() => inputRef.current?.click()}><UploadCloud size={17} /> Tải CV lên</Button>
        </section>
      )}

      {currentResume && (
        <section className="resume-section" aria-labelledby="current-resume-title">
          <div className="resume-section__heading"><div><span>Ưu tiên ứng tuyển</span><h2 id="current-resume-title">CV hiện tại</h2></div><p>CV này sẽ được dùng cho đơn ứng tuyển tiếp theo.</p></div>
          <ResumeCard resume={currentResume} current busyAction={busyAction} onView={(resume) => void handleFileAction(resume, 'view')} onDownload={(resume) => void handleFileAction(resume, 'download')} onDelete={handleDelete} />
        </section>
      )}

      {resumesQuery.isSuccess && otherResumes.length > 0 && (
        <section className="resume-section" aria-labelledby="other-resumes-title">
          <div className="resume-section__heading"><div><span>Lịch sử tải lên</span><h2 id="other-resumes-title">CV khác</h2></div><p>{otherResumes.length} CV</p></div>
          <div className="resume-list">{otherResumes.map((resume) => <ResumeCard key={resume.id} resume={resume} busyAction={busyAction} onView={(item) => void handleFileAction(item, 'view')} onDownload={(item) => void handleFileAction(item, 'download')} onDelete={handleDelete} />)}</div>
        </section>
      )}

      {preview && (
        <div className="resume-preview" role="dialog" aria-modal="true" aria-labelledby="resume-preview-title">
          <div className="resume-preview__panel">
            <header><div><span>Xem CV</span><h2 id="resume-preview-title">{preview.name}</h2></div><button type="button" onClick={() => setPreview(null)} aria-label="Đóng bản xem trước"><X size={20} /></button></header>
            <iframe src={preview.url} title={`Bản xem trước ${preview.name}`} />
          </div>
        </div>
      )}
    </main>
  )
}
