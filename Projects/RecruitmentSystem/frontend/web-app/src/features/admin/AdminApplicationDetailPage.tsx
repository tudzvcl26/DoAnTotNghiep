import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, FileUser } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { getAdminApplication } from './admin.api'
import { applicationStatusLabels } from '../applications/application-presenter'

export function AdminApplicationDetailPage() {
  const { applicationId = '' } = useParams(); const application = useQuery({ queryKey: ['admin-application', applicationId], queryFn: () => getAdminApplication(applicationId), enabled: Boolean(applicationId) })
  if (!application.data) return <main className="admin-page"><section className="admin-state"><p>Đang tải Application...</p></section></main>
  const data = application.data; const candidate = data.candidateProfileSnapshot
  return <main className="admin-page"><Link to="/admin/applications"><ArrowLeft size={16} /> Applications</Link><header className="admin-page__hero"><div><span>{applicationStatusLabels[data.status]}</span><h1>{candidate?.displayName || data.candidateId}</h1><p>Application {data.id}</p></div><FileUser /></header><section className="admin-grid"><article className="admin-card"><h2>Candidate snapshot</h2><dl><div><dt>Headline</dt><dd>{candidate?.headline || '—'}</dd></div><div><dt>Email</dt><dd>{candidate?.contactEmail || '—'}</dd></div><div><dt>Phone</dt><dd>{candidate?.contactPhone || '—'}</dd></div><div><dt>Captured</dt><dd>{candidate?.capturedAt || 'Legacy application'}</dd></div></dl></article><article className="admin-card"><h2>Business references</h2><dl><div><dt>Company</dt><dd>{data.companyId}</dd></div><div><dt>Job</dt><dd>{data.jobId}</dd></div><div><dt>Applied</dt><dd>{new Date(data.appliedAtInstant ?? data.appliedAt).toLocaleString('vi-VN')}</dd></div><div><dt>Cover letter</dt><dd>{data.coverLetter || '—'}</dd></div></dl></article></section></main>
}
