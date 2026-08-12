import { BookOpenCheck, CalendarClock, CheckCircle2, ClipboardList, Info } from 'lucide-react'
import type { JobDetail } from '../../../types/models/job'

function ContentSection({ icon: Icon, title, content }: { icon: typeof Info; title: string; content: string }) {
  return <section className="job-content-section"><h2><span><Icon /></span>{title}</h2><div className="job-rich-text">{content.split(/\r?\n/).filter(Boolean).map((line, index) => <p key={`${title}-${index}`}>{line}</p>)}</div></section>
}

export function JobDetailContent({ job }: { job: JobDetail }) {
  const hasExtra = Boolean(job.experienceLevel || job.applicationDeadline || job.expiredAt || job.jobCode)
  return <article className="job-detail-content">
    {job.description && <ContentSection icon={BookOpenCheck} title="Mô tả công việc" content={job.description} />}
    {job.responsibilities && <ContentSection icon={ClipboardList} title="Trách nhiệm" content={job.responsibilities} />}
    {job.requirements && <ContentSection icon={CheckCircle2} title="Yêu cầu ứng viên" content={job.requirements} />}
    {hasExtra && <section className="job-content-section"><h2><span><Info /></span>Thông tin khác</h2><dl className="job-other-info">{job.jobCode && <div><dt>Mã công việc</dt><dd>{job.jobCode}</dd></div>}{job.experienceLevel && <div><dt>Kinh nghiệm</dt><dd>{job.experienceLevel.replaceAll('_', ' ')}</dd></div>}{job.applicationDeadline && <div><dt>Hạn ứng tuyển</dt><dd><CalendarClock /> {new Intl.DateTimeFormat('vi-VN').format(new Date(job.applicationDeadline))}</dd></div>}{job.expiredAt && <div><dt>Ngày hết hiệu lực</dt><dd>{new Intl.DateTimeFormat('vi-VN').format(new Date(job.expiredAt))}</dd></div>}</dl></section>}
  </article>
}
