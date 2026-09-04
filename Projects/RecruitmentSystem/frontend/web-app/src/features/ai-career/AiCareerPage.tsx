import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  AlertCircle, ArrowRight, BrainCircuit, BriefcaseBusiness, CheckCircle2, FileSearch,
  FileText, GraduationCap, History, LoaderCircle, MessageCircle, RefreshCw, Send,
  ShieldCheck, Sparkles, Target, Trash2, UploadCloud, X,
} from 'lucide-react'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { normalizeApiError } from '../../lib/api/error-adapter'
import { getJobs } from '../jobs/jobs.api'
import {
  analyzeAiResume, deleteAiResume, queueInterviewPreparation, getLatestInterviewTask, getInterviewPreparation, queueMatchExplanation, getLatestExplanationTask, getMatchExplanation,
  getAiResumeAnalysis, getAiResumes, getAiTasks, getResumeMatches, matchJob,
  runCandidateAssistant, uploadAiResume, getJobRecommendations, refreshJobRecommendations,
  chatWithCareerCompanion,
} from './ai-career.api'
import {
  candidateAssistantTasks, type AssistantResponse, type CandidateAssistantTask, type CareerChatResponse,
  type JsonValue, type MatchingResult,
} from './ai-career.types'
import { aiCareerLabels } from './ai-career.labels'
import './ai-career.css'

const MAX_FILE_SIZE = 10 * 1024 * 1024
const acceptedExtensions = new Set(['pdf', 'docx', 'txt'])

const statusLabels: Record<string, string> = {
  READY: 'Sẵn sàng', ANALYZED: 'Đã phân tích', FAILED: 'Thất bại', PENDING: 'Đang chờ',
  RUNNING: 'Đang xử lý', COMPLETED: 'Hoàn tất', PARTIAL: 'Hoàn tất một phần', CANCELLED: 'Đã hủy',
  PLANNED: 'Đã lên kế hoạch',
}

const taskContent: Record<CandidateAssistantTask, { title: string; description: string }> = {
  CAREER_ROADMAP: { title: 'Lộ trình sự nghiệp', description: 'Xây dựng các bước phát triển nghề nghiệp từ dữ liệu CV đã phân tích.' },
  LEARNING_ROADMAP: { title: 'Lộ trình học tập', description: 'Sắp xếp ưu tiên học tập phù hợp với nền tảng hiện tại.' },
  SKILL_ROADMAP: { title: 'Lộ trình kỹ năng', description: 'Nhận diện khoảng trống và thứ tự nâng cấp kỹ năng.' },
  CERTIFICATE_RECOMMENDATION: { title: 'Gợi ý chứng chỉ', description: 'Đề xuất chứng chỉ phù hợp với hồ sơ và định hướng.' },
  PORTFOLIO_RECOMMENDATION: { title: 'Nâng cấp portfolio', description: 'Gợi ý dự án và cách trình bày năng lực nổi bật hơn.' },
  JOB_SEARCH_ADVICE: { title: 'Chiến lược tìm việc', description: 'Nhận lời khuyên tìm việc dựa trên nội dung CV.' },
  RESUME_IMPROVEMENT: { title: 'Cải thiện CV', description: 'Xác định vấn đề, ưu tiên và hành động cải thiện CV.' },
}

const taskLabels: Record<string, string> = {
  ...Object.fromEntries(candidateAssistantTasks.map((task) => [task, taskContent[task].title])),
  RESUME_ANALYSIS: 'Phân tích CV', MATCHING: 'Đánh giá độ phù hợp',
  MATCH_EXPLANATION: 'Giải thích độ phù hợp', INTERVIEW_PREPARATION: 'Chuẩn bị phỏng vấn',
  CANDIDATE_ASSISTANT: 'Trợ lý ứng viên', RECRUITER_ASSISTANT: 'Trợ lý nhà tuyển dụng',
  JOB_RECOMMENDATION_REFRESH: 'Cập nhật gợi ý việc làm',
}

function formatDate(value: string | null | undefined) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

function formatSize(bytes: number) {
  return bytes < 1024 * 1024 ? `${Math.max(1, Math.round(bytes / 1024))} KB` : `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

function humanize(value: string) {
  return aiCareerLabels[value] ?? 'Thông tin bổ sung'
}

export function JsonResult({ value }: { value: JsonValue }) {
  if (value == null || (typeof value === 'string' && ['null', 'undefined', '[object Object]', ''].includes(value.trim()))) return <p className="ai-json__empty">Chưa có thông tin.</p>
  if (Array.isArray(value)) {
    return value.length ? <ul className="ai-json__list">{value.map((item, index) => <li key={index}><JsonResult value={item} /></li>)}</ul> : <p className="ai-json__empty">Chưa có thông tin.</p>
  }
  if (typeof value === 'object') {
    return <div className="ai-json__object">{Object.entries(value).map(([key, item]) => <section key={key}><h4>{humanize(key)}</h4><JsonResult value={item} /></section>)}</div>
  }
  const enumLabels: Record<string, string> = { HIGH: 'Cao', MEDIUM: 'Trung bình', LOW: 'Thấp', EASY: 'Dễ', HARD: 'Khó', CANDIDATE: 'Ứng viên', RECRUITER: 'Nhà tuyển dụng', true: 'Có', false: 'Không', vi: 'Tiếng Việt' }
  return <p>{enumLabels[String(value)] ?? String(value)}</p>
}

function ErrorNotice({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  const normalized = normalizeApiError(error)
  return <div className="ai-error" role="alert"><AlertCircle aria-hidden="true" /><div><strong>Yêu cầu chưa hoàn tất</strong><p>{normalized.message}</p>{normalized.correlationId && <small>Mã đối chiếu: {normalized.correlationId}</small>}</div>{onRetry && <button type="button" onClick={onRetry}><RefreshCw /> Thử lại</button>}</div>
}

function ResultMeta({ provider, model, duration }: { provider: string; model: string; duration: number }) {
  return <div className="ai-result-meta"><span>{provider}</span><span>{model}</span><span>{(duration / 1000).toFixed(1)} giây</span></div>
}

export function AiCareerPage() {
  const queryClient = useQueryClient()
  const fileInput = useRef<HTMLInputElement>(null)
  const [selectedResumeId, setSelectedResumeId] = useState('')
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [fileError, setFileError] = useState('')
  const [notice, setNotice] = useState('')
  const [storedAssistantResult, setAssistantResult] = useState<AssistantResponse | null>(null)
  const [assistantTask, setAssistantTask] = useState<CandidateAssistantTask | null>(null)
  const [jobId, setJobId] = useState('')
  const [storedMatch, setActiveMatch] = useState<MatchingResult | null>(null)
  const activeMatch = storedMatch?.resumeId === selectedResumeId ? storedMatch : null
  const assistantResult = storedAssistantResult?.resumeId === selectedResumeId ? storedAssistantResult : null
  const explanationTask = useQuery({
    queryKey: ['ai-explanation-task', activeMatch?.id],
    queryFn: () => getLatestExplanationTask(activeMatch!.id),
    enabled: Boolean(activeMatch),
    refetchInterval: query => query.state.status !== 'error' && query.state.dataUpdateCount < 900 && ['PENDING', 'RUNNING'].includes(query.state.data?.status ?? '') ? 2000 : false,
  })
  const explanationProcessing = ['PENDING', 'RUNNING'].includes(explanationTask.data?.status ?? '')
  const explanationQuery = useQuery({
    queryKey: ['ai-explanation', activeMatch?.id, explanationTask.data?.id, explanationTask.data?.status],
    queryFn: () => getMatchExplanation(activeMatch!.id),
    enabled: Boolean(activeMatch && explanationTask.isSuccess && !explanationProcessing && explanationTask.data?.status !== 'FAILED'),
    retry: false,
  })
  const explanation = explanationQuery.data?.matchId === activeMatch?.id ? explanationQuery.data : null
  const interviewTask = useQuery({
    queryKey: ['ai-interview-task', activeMatch?.id],
    queryFn: () => getLatestInterviewTask(activeMatch!.id),
    enabled: Boolean(activeMatch),
    refetchInterval: query => query.state.status !== 'error' && query.state.dataUpdateCount < 900 && ['PENDING', 'RUNNING'].includes(query.state.data?.status ?? '') ? 2000 : false,
  })
  const interviewProcessing = ['PENDING', 'RUNNING'].includes(interviewTask.data?.status ?? '')
  const interviewQuery = useQuery({
    queryKey: ['ai-interview', activeMatch?.id, interviewTask.data?.id, interviewTask.data?.status],
    queryFn: () => getInterviewPreparation(activeMatch!.id),
    enabled: Boolean(activeMatch && interviewTask.isSuccess && !interviewProcessing && interviewTask.data?.status !== 'FAILED'),
    retry: false,
  })
  const interview = interviewQuery.data?.matchId === activeMatch?.id ? interviewQuery.data : null
  const [chatMessage, setChatMessage] = useState('')
  const [lastChatMessage, setLastChatMessage] = useState('')
  const [storedChat, setChatResult] = useState<{ context: string; result: CareerChatResponse } | null>(null)
  const chatContext = `${selectedResumeId}:${jobId}`
  const chatResult = storedChat?.context === chatContext ? storedChat.result : null

  const resumes = useQuery({ queryKey: ['ai-resumes'], queryFn: getAiResumes })
  const resumeItems = useMemo(() => resumes.data?.content ?? [], [resumes.data])
  const selectedResume = resumeItems.find((resume) => resume.id === selectedResumeId)

  useEffect(() => {
    if (!selectedResumeId && resumeItems.length > 0) setSelectedResumeId(resumeItems[0].id)
    if (selectedResumeId && resumeItems.length > 0 && !resumeItems.some((resume) => resume.id === selectedResumeId)) setSelectedResumeId(resumeItems[0].id)
  }, [resumeItems, selectedResumeId])

  const analysis = useQuery({
    queryKey: ['ai-resume-analysis', selectedResumeId],
    queryFn: () => getAiResumeAnalysis(selectedResumeId),
    enabled: Boolean(selectedResumeId && selectedResume?.status === 'ANALYZED'),
  })
  const matches = useQuery({
    queryKey: ['ai-resume-matches', selectedResumeId],
    queryFn: () => getResumeMatches(selectedResumeId),
    enabled: Boolean(selectedResumeId && selectedResume?.status === 'ANALYZED'),
  })
  const tasks = useQuery({ queryKey: ['ai-tasks'], queryFn: getAiTasks, refetchInterval: 5000 })
  const recommendations = useQuery({
    queryKey: ['ai-job-recommendations', selectedResumeId],
    queryFn: () => getJobRecommendations(selectedResumeId),
    enabled: Boolean(selectedResumeId && selectedResume?.status === 'ANALYZED'),
  })
  const jobs = useQuery({
    queryKey: ['jobs', 'ai-career-selector'],
    queryFn: () => getJobs({ keyword: '', page: 0, size: 24, sort: 'publishedAt,desc' }),
  })
  const jobsById = useMemo(() => new Map((jobs.data?.content ?? []).map((job) => [job.id, job])), [jobs.data?.content])

  useEffect(() => {
    if (!activeMatch && matches.data?.content[0]) setActiveMatch(matches.data.content[0])
  }, [activeMatch, matches.data])

  const refreshAiData = async () => Promise.all([
    queryClient.invalidateQueries({ queryKey: ['ai-resumes'] }),
    queryClient.invalidateQueries({ queryKey: ['ai-tasks'] }),
  ])

  const upload = useMutation({
    mutationFn: uploadAiResume,
    onSuccess: async (resume) => {
      setSelectedFile(null); setFileError(''); setNotice(`Đã tải “${resume.originalFilename}” lên AI Career Center.`)
      if (fileInput.current) fileInput.current.value = ''
      await refreshAiData(); setSelectedResumeId(resume.id)
    },
  })
  const remove = useMutation({
    mutationFn: deleteAiResume,
    onSuccess: async () => { setNotice('Đã xóa CV dùng cho AI.'); setSelectedResumeId(''); await refreshAiData() },
  })
  const analyze = useMutation({
    mutationFn: analyzeAiResume,
    onSuccess: async (result) => {
      setNotice('Phân tích CV đã hoàn tất.'); await refreshAiData()
      queryClient.setQueryData(['ai-resume-analysis', result.resumeDocumentId], result)
    },
  })
  const assistant = useMutation({
    mutationFn: ({ task, resumeId, matchId }: { task: CandidateAssistantTask; resumeId: string; matchId?: string }) => runCandidateAssistant(task, resumeId, matchId),
    onSuccess: async (result) => { setAssistantResult(result); await queryClient.invalidateQueries({ queryKey: ['ai-tasks'] }) },
  })
  const careerChat = useMutation({
    mutationFn: async (message: string) => ({ context: chatContext, result: await chatWithCareerCompanion({
      message,
      ...(selectedResume?.status === 'ANALYZED' ? { resumeId: selectedResume.id } : {}),
      ...(jobId ? { jobId } : {}),
    }) }),
    onSuccess: (result) => setChatResult(result),
  })
  const matching = useMutation({
    mutationFn: ({ selectedJob, resumeId }: { selectedJob: string; resumeId: string }) => matchJob(selectedJob, resumeId),
    onSuccess: async (result) => {
      setActiveMatch(result); setNotice('Đánh giá độ phù hợp đã hoàn tất.')
      await Promise.all([queryClient.invalidateQueries({ queryKey: ['ai-resume-matches', result.resumeId] }), queryClient.invalidateQueries({ queryKey: ['ai-tasks'] })])
    },
  })
  const explain = useMutation({
    mutationFn: queueMatchExplanation,
    onSuccess: async (result, matchId) => {
      queryClient.setQueryData(['ai-explanation-task', matchId], result)
      await queryClient.invalidateQueries({ queryKey: ['ai-tasks'] })
    },
  })
  const prepareInterview = useMutation({
    mutationFn: queueInterviewPreparation,
    onSuccess: async (result, matchId) => {
      queryClient.setQueryData(['ai-interview-task', matchId], result)
      await queryClient.invalidateQueries({ queryKey: ['ai-tasks'] })
    },
  })
  const refreshRecommendations = useMutation({
    mutationFn: refreshJobRecommendations,
    onSuccess: async () => {
      setNotice('Đã xếp hàng làm mới gợi ý việc làm. Bạn có thể tiếp tục sử dụng trang trong khi hệ thống xử lý.')
      await queryClient.invalidateQueries({ queryKey: ['ai-tasks'] })
    },
  })

  const recommendationTask = tasks.data?.content.find((task) => task.taskType === 'JOB_RECOMMENDATION_REFRESH')
  const recommendationTaskStatus = recommendationTask?.status
  useEffect(() => {
    if (recommendationTaskStatus && ['COMPLETED', 'PARTIAL'].includes(recommendationTaskStatus)) {
      void queryClient.invalidateQueries({ queryKey: ['ai-job-recommendations', selectedResumeId] })
    }
  }, [queryClient, recommendationTaskStatus, selectedResumeId])

  const chooseFile = (file: File | null) => {
    setNotice(''); setFileError('')
    if (!file) { setSelectedFile(null); return }
    const extension = file.name.split('.').pop()?.toLowerCase() ?? ''
    if (!acceptedExtensions.has(extension)) { setSelectedFile(null); setFileError('Chỉ hỗ trợ PDF, DOCX hoặc TXT UTF-8.'); return }
    if (file.size <= 0 || file.size > MAX_FILE_SIZE) { setSelectedFile(null); setFileError('Tệp phải có dữ liệu và không vượt quá 10 MB.'); return }
    setSelectedFile(file)
  }

  const runTask = (task: CandidateAssistantTask) => {
    if (!selectedResumeId) return
    setAssistantTask(task); setAssistantResult(null)
    assistant.mutate({ task, resumeId: selectedResumeId, matchId: activeMatch?.id })
  }

  const sendChat = () => {
    const message = chatMessage.trim()
    if (message.length < 3 || careerChat.isPending) return
    setLastChatMessage(message); setChatResult(null)
    careerChat.mutate(message)
  }

  const mutationError = upload.error ?? remove.error

  return <main className="ai-career-page">
    <header className="ai-career-hero">
      <div><span className="ai-career-eyebrow"><Sparkles /> Trợ lý nghề nghiệp có kiểm soát</span><h1>AI Career Center</h1><p>Phân tích CV, xây dựng lộ trình và chuẩn bị phỏng vấn dựa trên dữ liệu thật của bạn.</p><div className="ai-career-hero__trust"><ShieldCheck /> AI chỉ hỗ trợ ra quyết định — không thay đổi điểm hoặc trạng thái tuyển dụng.</div></div>
      <div className="ai-career-hero__model"><BrainCircuit /><span>AI đang sử dụng</span><strong>Qwen2.5:3B-Instruct</strong><small>Chạy cục bộ qua hệ thống bảo mật</small></div>
    </header>

    <nav className="ai-career-nav" aria-label="Đi đến khu vực AI Career"><a href="#career-chat">Hỏi trợ lý</a><a href="#ai-resume">Phân tích CV</a><a href="#job-recommendations">Gợi ý việc làm</a><a href="#career-assistant">Lộ trình nghề nghiệp</a><a href="#job-matching">Độ phù hợp</a><a href="#ai-history">Lịch sử</a></nav>

    {notice && <div className="ai-success" role="status"><CheckCircle2 /> {notice}</div>}
    {mutationError && <ErrorNotice error={mutationError} />}

    <section className="ai-section" id="career-chat" aria-labelledby="career-chat-title">
      <div className="ai-section__heading"><div><span>AI Career Companion</span><h2 id="career-chat-title">Hỏi trợ lý nghề nghiệp</h2><p>Đặt câu hỏi bằng bất kỳ ngôn ngữ nào; câu trả lời luôn bằng tiếng Việt và chỉ dùng dữ liệu thuộc về bạn.</p></div><MessageCircle /></div>
      <div className="ai-chat-context" aria-label="Ngữ cảnh trợ lý đang sử dụng">
        <span>Hồ sơ ứng viên hiện tại</span>
        <span>{selectedResume?.status === 'ANALYZED' ? `CV: ${selectedResume.originalFilename}` : 'Chưa chọn CV đã phân tích'}</span>
        <span>{jobId ? `Việc làm: ${jobsById.get(jobId)?.title ?? 'đang chọn'}` : 'Không chọn việc làm cụ thể'}</span>
      </div>
      <div className="ai-chat-compose">
        <label htmlFor="career-chat-message">Câu hỏi của bạn</label>
        <textarea id="career-chat-message" rows={4} maxLength={2000} value={chatMessage} onChange={(event) => setChatMessage(event.target.value)} placeholder="Ví dụ: Tôi nên cải thiện kỹ năng nào cho vị trí Java Developer?" disabled={careerChat.isPending} onKeyDown={(event) => { if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) sendChat() }} />
        <div><small>{chatMessage.length}/2000 · Ctrl/⌘ + Enter để gửi</small><span><button type="button" className="ai-chat-clear" onClick={() => { setChatMessage(''); setChatResult(null); careerChat.reset() }} disabled={careerChat.isPending || (!chatMessage && !chatResult)}><X /> Xóa</button><button type="button" className="ai-primary" onClick={sendChat} disabled={chatMessage.trim().length < 3 || careerChat.isPending}>{careerChat.isPending ? <><LoaderCircle className="ai-spin" /> Đang suy nghĩ...</> : <><Send /> Gửi câu hỏi</>}</button></span></div>
      </div>
      {careerChat.isError && <ErrorNotice error={careerChat.error} onRetry={() => lastChatMessage && careerChat.mutate(lastChatMessage)} />}
      {!careerChat.isPending && !careerChat.isError && !chatResult && <div className="ai-empty ai-chat-empty"><MessageCircle /><strong>Chưa có câu trả lời</strong><p>Trợ lý không dùng phản hồi mẫu và chỉ gọi mô hình khi bạn gửi câu hỏi.</p></div>}
      {chatResult && <article className="ai-generated-result ai-chat-answer" aria-live="polite"><header><div><span>Câu trả lời tiếng Việt</span><h3>Trợ lý nghề nghiệp</h3></div><Sparkles /></header><p>{chatResult.answer}</p><ResultMeta provider={chatResult.providerName} model={chatResult.modelName} duration={chatResult.generationDurationMs} /></article>}
    </section>

    <section className="ai-section" id="ai-resume" aria-labelledby="ai-resume-title">
      <div className="ai-section__heading"><div><span>Bước 1 · Dữ liệu đầu vào</span><h2 id="ai-resume-title">CV dùng cho AI</h2><p>Dịch vụ AI có kho CV riêng. Bạn cần tải CV lên đây dù đã có CV ứng tuyển trong hồ sơ ứng viên.</p></div><FileSearch /></div>
      <div className="ai-resume-layout">
        <div className="ai-upload-card" aria-busy={upload.isPending}>
          <UploadCloud /><h3>Tải CV để phân tích</h3><p>PDF, DOCX hoặc TXT UTF-8 · tối đa 10 MB</p>
          <label htmlFor="ai-resume-file">Chọn tệp CV<input ref={fileInput} id="ai-resume-file" type="file" accept=".pdf,.docx,.txt,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document,text/plain" onChange={(event) => chooseFile(event.target.files?.[0] ?? null)} disabled={upload.isPending} /></label>
          {selectedFile && <div className="ai-selected-file"><FileText /><span><strong>{selectedFile.name}</strong><small>{formatSize(selectedFile.size)}</small></span></div>}
          {fileError && <p className="ai-inline-error" role="alert">{fileError}</p>}
          <button type="button" className="ai-primary" disabled={!selectedFile || upload.isPending} onClick={() => selectedFile && upload.mutate(selectedFile)}>{upload.isPending ? <><LoaderCircle className="ai-spin" /> Đang tải lên...</> : <><UploadCloud /> Tải lên AI</>}</button>
        </div>
        <div className="ai-resume-list">
          <h3>CV đã tải lên AI <span>{resumeItems.length}</span></h3>
          {resumes.isPending && <p className="ai-loading"><LoaderCircle className="ai-spin" /> Đang tải danh sách...</p>}
          {resumes.isError && <ErrorNotice error={resumes.error} onRetry={() => void resumes.refetch()} />}
          {resumes.isSuccess && resumeItems.length === 0 && <div className="ai-empty"><FileText /><strong>Chưa có CV dùng cho AI</strong><p>Tải một CV hợp lệ để bắt đầu phân tích.</p></div>}
          {resumeItems.map((resume) => <article key={resume.id} className={`ai-resume-item${selectedResumeId === resume.id ? ' is-selected' : ''}`}>
            <button type="button" className="ai-resume-item__select" onClick={() => { setSelectedResumeId(resume.id); setActiveMatch(null); setAssistantResult(null) }}><FileText /><span><strong>{resume.originalFilename}</strong><small>{formatSize(resume.fileSize)} · {formatDate(resume.uploadTime)}</small></span><em className={`ai-status ai-status--${resume.status.toLowerCase()}`}>{statusLabels[resume.status] ?? 'Chưa xác định'}</em></button>
            <button type="button" className="ai-icon-button" aria-label={`Xóa ${resume.originalFilename}`} onClick={() => window.confirm(`Xóa “${resume.originalFilename}” khỏi AI Career Center?`) && remove.mutate(resume.id)} disabled={remove.isPending}><Trash2 /></button>
          </article>)}
        </div>
      </div>

      {selectedResume && <div className="ai-analysis" aria-busy={analyze.isPending}>
        <div className="ai-analysis__action"><div><span>CV đang chọn</span><h3>{selectedResume.originalFilename}</h3><p>{selectedResume.status === 'ANALYZED' ? 'CV đã có kết quả phân tích được lưu trên máy chủ.' : 'Chạy phân tích trước khi dùng trợ lý lộ trình nghề nghiệp hoặc đánh giá độ phù hợp.'}</p></div><button type="button" className="ai-primary" onClick={() => analyze.mutate(selectedResume.id)} disabled={analyze.isPending}>{analyze.isPending ? <><LoaderCircle className="ai-spin" /> AI đang phân tích...</> : <><Sparkles /> {selectedResume.status === 'ANALYZED' ? 'Phân tích lại' : 'Phân tích CV'}</>}</button></div>
        {analyze.isError && <ErrorNotice error={analyze.error} onRetry={() => analyze.mutate(selectedResume.id)} />}
        {analyze.isPending && <p role="status">Mô hình cục bộ có thể cần vài phút. Bạn có thể rời trang; kết quả hoàn tất sẽ được lưu. Không cần gửi lại yêu cầu.</p>}
        {analysis.isPending && selectedResume.status === 'ANALYZED' && <p className="ai-loading"><LoaderCircle className="ai-spin" /> Đang lấy kết quả phân tích...</p>}
        {analysis.isError && <ErrorNotice error={analysis.error} onRetry={() => void analysis.refetch()} />}
        {analysis.data && <div className="ai-analysis__result"><div className="ai-score"><strong>{analysis.data.qualityScore}</strong><span>/ 100</span><p>Chất lượng CV</p></div><div className="ai-analysis__details"><div className="ai-breakdowns">{Object.entries(analysis.data.scoreBreakdown).map(([key, item]) => <div key={key}><span>{humanize(key)}</span><strong>{item.score}/{item.maximum}</strong><p>{item.rationale}</p></div>)}</div><div className="ai-tags"><h4>Kỹ năng được nhận diện</h4>{analysis.data.skills.filter((skill, index, all) => all.findIndex((item) => item.name.trim().toLocaleLowerCase('vi') === skill.name.trim().toLocaleLowerCase('vi')) === index).map((skill) => <span key={`${skill.category}-${skill.name}`}>{skill.name}</span>)}</div><details><summary>Xem toàn bộ dữ liệu CV có cấu trúc</summary><JsonResult value={analysis.data.structuredData} /></details><ResultMeta provider={analysis.data.providerName} model={analysis.data.modelName} duration={analysis.data.analysisDurationMs} /></div></div>}
      </div>}
    </section>

    <section className="ai-section" id="job-recommendations" aria-labelledby="recommendations-title">
      <div className="ai-section__heading"><div><span>Gợi ý cá nhân hóa</span><h2 id="recommendations-title">Việc làm phù hợp với CV</h2><p>Kết quả đã tạo được lưu an toàn; yêu cầu làm mới chạy nền để bạn có thể tiếp tục sử dụng trang.</p></div><BriefcaseBusiness /></div>
        <div className="ai-analysis__action"><div><strong>{recommendationTask ? `Trạng thái cập nhật: ${statusLabels[recommendationTask.status] ?? 'Chưa xác định'}` : 'Chưa yêu cầu cập nhật gợi ý'}</strong><p>{recommendationTask?.status === 'FAILED' ? recommendationTask.errorMessage : 'Bật quyền dùng dữ liệu cho gợi ý trong Hồ sơ ứng viên trước khi yêu cầu kết quả mới.'}</p></div><button type="button" className="ai-primary" disabled={!analysis.data || refreshRecommendations.isPending || recommendationTask?.status === 'PENDING' || recommendationTask?.status === 'RUNNING'} onClick={() => refreshRecommendations.mutate(selectedResumeId)}>{refreshRecommendations.isPending ? <><LoaderCircle className="ai-spin" /> Đang gửi yêu cầu...</> : <><RefreshCw /> Cập nhật gợi ý</>}</button></div>
      {refreshRecommendations.isError && <ErrorNotice error={refreshRecommendations.error} onRetry={() => selectedResumeId && refreshRecommendations.mutate(selectedResumeId)} />}
      {recommendations.isLoading && <p className="ai-loading"><LoaderCircle className="ai-spin" /> Đang đọc gợi ý đã lưu...</p>}
      {recommendations.isError && <ErrorNotice error={recommendations.error} onRetry={() => void recommendations.refetch()} />}
      {recommendations.data?.content.length === 0 && <div className="ai-empty"><BriefcaseBusiness /><strong>Chưa có gợi ý đã lưu</strong><p>Bật consent, chọn CV đã phân tích rồi yêu cầu làm mới.</p></div>}
      {recommendations.data && recommendations.data.content.length > 0 && <div className="ai-task-grid">{recommendations.data.content.map((item) => <article key={item.id}><span><strong>{item.overallScore}/100</strong></span><h3>{jobsById.get(item.jobId)?.title ?? `Việc làm ${item.jobId.slice(0, 8)}`}</h3><JsonResult value={item.recommendation} /><Link to={`/jobs/${item.jobId}`}>Xem việc làm <ArrowRight /></Link></article>)}</div>}
    </section>

    <section className="ai-section" id="career-assistant" aria-labelledby="assistant-title">
      <div className="ai-section__heading"><div><span>Bước 2 · Phát triển sự nghiệp</span><h2 id="assistant-title">Trợ lý lộ trình nghề nghiệp</h2><p>Chọn mục tiêu bạn cần. AI chỉ xử lý khi bạn chủ động yêu cầu.</p></div><GraduationCap /></div>
      {!analysis.data && <div className="ai-prerequisite"><AlertCircle /><div><strong>Cần CV đã phân tích</strong><p>Chọn CV có trạng thái “Đã phân tích” hoặc hoàn tất phân tích ở bước 1.</p></div><a href="#ai-resume">Đi đến CV cho AI <ArrowRight /></a></div>}
      <div className="ai-task-grid">{candidateAssistantTasks.map((task) => <article key={task} className={assistantTask === task ? 'is-active' : ''}><span><Target /></span><h3>{taskContent[task].title}</h3><p>{taskContent[task].description}</p><button type="button" onClick={() => runTask(task)} disabled={!analysis.data || assistant.isPending}>{assistant.isPending && assistantTask === task ? <><LoaderCircle className="ai-spin" /> Đang tạo...</> : <>Tạo kết quả <ArrowRight /></>}</button></article>)}</div>
      {assistant.isError && <ErrorNotice error={assistant.error} onRetry={() => assistantTask && runTask(assistantTask)} />}
      {assistantResult && <article className="ai-generated-result" aria-live="polite"><header><div><span>Kết quả AI</span><h3>{taskContent[assistantResult.taskType].title}</h3></div><Sparkles /></header><JsonResult value={assistantResult.response} /><ResultMeta provider={assistantResult.providerName} model={assistantResult.modelName} duration={assistantResult.generationDurationMs} /></article>}
    </section>

    <section className="ai-section" id="job-matching" aria-labelledby="matching-title">
      <div className="ai-section__heading"><div><span>Bước 3 · Đánh giá cơ hội</span><h2 id="matching-title">Đánh giá độ phù hợp</h2><p>So sánh một việc làm đang tuyển với CV đã phân tích để nhận điểm mạnh, khoảng trống và gợi ý chuẩn bị.</p></div><BriefcaseBusiness /></div>
      <div className="ai-match-form"><label>Việc làm đang tuyển<select value={jobId} onChange={(event) => setJobId(event.target.value)}><option value="">Chọn một việc làm</option>{jobs.data?.content.map((job) => <option key={job.id} value={job.id}>{job.title} · {job.jobCode}</option>)}</select></label><label>CV dùng để đánh giá<select value={selectedResumeId} onChange={(event) => setSelectedResumeId(event.target.value)}><option value="">Chọn CV</option>{resumeItems.filter((resume) => resume.status === 'ANALYZED').map((resume) => <option key={resume.id} value={resume.id}>{resume.originalFilename}</option>)}</select></label><button className="ai-primary" type="button" disabled={!jobId || !analysis.data || matching.isPending} onClick={() => matching.mutate({ selectedJob: jobId, resumeId: selectedResumeId })}>{matching.isPending ? <><LoaderCircle className="ai-spin" /> Đang đánh giá...</> : <><Target /> Phân tích độ phù hợp</>}</button></div>
      {jobs.isError && <ErrorNotice error={jobs.error} onRetry={() => void jobs.refetch()} />}
      {matching.isError && <ErrorNotice error={matching.error} onRetry={() => jobId && matching.mutate({ selectedJob: jobId, resumeId: selectedResumeId })} />}
      {matches.data && matches.data.content.length > 0 && <label className="ai-match-history">Kết quả gần đây<select value={activeMatch?.id ?? ''} onChange={(event) => { const next = matches.data.content.find((match) => match.id === event.target.value) ?? null; setActiveMatch(next) }}><option value="">Chọn kết quả</option>{matches.data.content.map((match) => <option key={match.id} value={match.id}>{match.overallScore}/100 · {formatDate(match.updatedAt)}</option>)}</select></label>}
      {activeMatch && <article className="ai-match-result"><div className="ai-match-score"><strong>{activeMatch.overallScore}</strong><span>/100</span><p>Độ phù hợp</p></div><div className="ai-match-content"><div className="ai-breakdowns">{activeMatch.scoreBreakdown.map((item) => <div key={item.dimension}><span>{humanize(item.dimension)}</span><strong>{item.actualScore}/{item.maximumScore}</strong><p>{item.reason}</p></div>)}</div><div className="ai-match-columns"><div><h4>Điểm mạnh</h4><ul>{activeMatch.strengths.map((value) => <li key={value}>{value}</li>)}</ul><h4>Kỹ năng phù hợp</h4><div className="ai-tags">{activeMatch.matchedSkills.map((value) => <span key={value}>{value}</span>)}</div></div><div><h4>Khoảng trống</h4><ul>{activeMatch.gapAnalysis.map((value) => <li key={value}>{value}</li>)}</ul><h4>Kỹ năng còn thiếu</h4><div className="ai-tags ai-tags--warning">{activeMatch.missingSkills.map((value) => <span key={value}>{value}</span>)}</div></div></div><div className="ai-match-actions"><button type="button" onClick={() => explain.mutate(activeMatch.id)} disabled={explain.isPending || explanationProcessing}>{explain.isPending || explanationProcessing ? <><LoaderCircle className="ai-spin" /> Đang xử lý nền...</> : <><Sparkles /> Vì sao công việc phù hợp?</>}</button><button type="button" onClick={() => prepareInterview.mutate(activeMatch.id)} disabled={prepareInterview.isPending || interviewProcessing}>{prepareInterview.isPending || interviewProcessing ? <><LoaderCircle className="ai-spin" /> Đang chuẩn bị...</> : <><GraduationCap /> Chuẩn bị phỏng vấn</>}</button></div></div></article>}
      {explain.isError && explain.variables === activeMatch?.id && <ErrorNotice error={explain.error} onRetry={() => activeMatch && explain.mutate(activeMatch.id)} />}
      {explanationProcessing && <p role="status">Giải thích đang được xử lý nền. Bạn có thể rời trang hoặc tải lại; tác vụ và kết quả được lưu trên máy chủ.</p>}
      {explanationTask.isError && <ErrorNotice error={explanationTask.error} onRetry={() => void explanationTask.refetch()} />}
      {explanationQuery.isError && normalizeApiError(explanationQuery.error).status !== 404 && <ErrorNotice error={explanationQuery.error} onRetry={() => void explanationQuery.refetch()} />}
      {explanationTask.data?.status === 'FAILED' && <div className="ai-error" role="alert"><p>{explanationTask.data.errorMessage ?? 'Tác vụ bị gián đoạn. Vui lòng thử lại.'}</p><button type="button" disabled={explain.isPending} onClick={() => activeMatch && explain.mutate(activeMatch.id)}>Thử lại giải thích</button></div>}
      {explanation && <article className="ai-generated-result"><header><div><span>Giải thích từ AI và bằng chứng đối chiếu theo quy tắc</span><h3>Vì sao công việc phù hợp với bạn</h3></div><Sparkles /></header><JsonResult value={explanation.explanation} /><ResultMeta provider={explanation.providerName} model={explanation.modelName} duration={explanation.generationDurationMs} /></article>}
      {prepareInterview.isError && <ErrorNotice error={prepareInterview.error} onRetry={() => activeMatch && prepareInterview.mutate(activeMatch.id)} />}
      {interviewProcessing && <p role="status">Bộ câu hỏi đang được xử lý nền. Bạn có thể rời trang hoặc tải lại; kết quả được lưu trên máy chủ. Tự động kiểm tra tối đa 30 phút mỗi phiên.</p>}
      {interviewTask.isError && <ErrorNotice error={interviewTask.error} onRetry={() => void interviewTask.refetch()} />}
      {interviewQuery.isError && normalizeApiError(interviewQuery.error).status !== 404 && <ErrorNotice error={interviewQuery.error} onRetry={() => void interviewQuery.refetch()} />}
      {interviewTask.data?.status === 'FAILED' && <ErrorNotice error={new Error(interviewTask.data.errorMessage || 'Tác vụ chuẩn bị phỏng vấn chưa hoàn tất.')} onRetry={() => activeMatch && prepareInterview.mutate(activeMatch.id)} />}
      {interview && <article className="ai-generated-result"><header><div><span>Bộ câu hỏi thực tế</span><h3>Chuẩn bị phỏng vấn</h3></div><GraduationCap /></header><JsonResult value={interview.questionSet} /><ResultMeta provider={interview.providerName} model={interview.modelName} duration={interview.generationDurationMs} /></article>}
    </section>

    <section className="ai-section" id="ai-history" aria-labelledby="history-title">
      <div className="ai-section__heading"><div><span>Hoạt động đã lưu</span><h2 id="history-title">Lịch sử tác vụ AI</h2><p>Trạng thái thật từ AI Service, không lưu kết quả tạm trong trình duyệt.</p></div><History /></div>
      {tasks.isPending && <p className="ai-loading"><LoaderCircle className="ai-spin" /> Đang tải lịch sử...</p>}
      {tasks.isError && <ErrorNotice error={tasks.error} onRetry={() => void tasks.refetch()} />}
      {tasks.data && tasks.data.content.length === 0 && <div className="ai-empty"><History /><strong>Chưa có tác vụ AI</strong><p>Các tác vụ sẽ xuất hiện sau khi bạn chủ động chạy.</p></div>}
      {tasks.data && tasks.data.content.length > 0 && <div className="ai-task-history">{tasks.data.content.map((task) => <article key={task.id}><span className={`ai-status ai-status--${task.status.toLowerCase()}`}>{statusLabels[task.status] ?? 'Chưa xác định'}</span><div><h3>{taskLabels[task.taskType] ?? humanize(task.taskType)}</h3><p>{task.errorMessage ?? `${task.providerName ?? 'Hệ thống'} · ${task.modelName ?? 'Không dùng mô hình sinh'}`}</p></div><time dateTime={task.createdAt}>{formatDate(task.completedAt ?? task.createdAt)}</time></article>)}</div>}
    </section>
  </main>
}
