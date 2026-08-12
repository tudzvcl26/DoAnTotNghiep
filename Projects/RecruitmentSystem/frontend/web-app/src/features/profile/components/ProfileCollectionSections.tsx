import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Award, BookOpen, Briefcase, Code2, Edit3, Globe2, Languages, Plus, RefreshCw, Trash2, X } from 'lucide-react'
import { useEffect, useState, type ReactNode } from 'react'
import { useForm } from 'react-hook-form'
import { Button } from '../../../components/ui/Button'
import { getErrorMessage } from '../../../lib/api/error-adapter'
import type { SpringPage } from '../../../types/api/common'
import type {
  CandidateLanguage, CandidateLanguageRequest, CandidateSkill, CandidateSkillRequest, Certificate,
  CertificateRequest, Education, EducationRequest, Experience, ExperienceRequest, SocialLink, SocialLinkRequest,
} from '../../../types/models/profile'
import { certificateApi, educationApi, experienceApi, languageApi, skillApi, socialLinkApi } from '../profile.api'
import {
  certificateSchema, educationSchema, experienceSchema, languageSchema, skillSchema, socialLinkSchema,
  type CertificateForm, type EducationForm, type ExperienceForm, type LanguageForm, type SkillForm, type SocialLinkForm,
} from '../profile.schemas'

type Identified = { id: string; version: number }
type CrudApi<T, P> = { list: (userId: string) => Promise<SpringPage<T>>; create: (userId: string, payload: P) => Promise<T>; update: (userId: string, id: string, payload: P) => Promise<T>; remove: (userId: string, id: string) => Promise<void> }

function cleanRecord<T extends Record<string, unknown>>(values: T): T {
  return Object.fromEntries(Object.entries(values).map(([key, value]) => [key, value === '' ? null : value])) as T
}

function useProfileCollection<T extends Identified, P>(key: string, userId: string, api: CrudApi<T, P>) {
  const client = useQueryClient()
  const queryKey = ['candidate-profile-section', key, userId]
  const query = useQuery({ queryKey, queryFn: () => api.list(userId) })
  const save = useMutation({
    mutationFn: ({ item, payload }: { item: T | null; payload: P }) => item ? api.update(userId, item.id, payload) : api.create(userId, payload),
    onSuccess: async () => { await client.invalidateQueries({ queryKey }); await client.invalidateQueries({ queryKey: ['candidate-profile'] }) },
  })
  const remove = useMutation({
    mutationFn: (id: string) => api.remove(userId, id),
    onSuccess: async () => { await client.invalidateQueries({ queryKey }); await client.invalidateQueries({ queryKey: ['candidate-profile'] }) },
  })
  return { query, save, remove }
}

type CollectionCardProps<T extends Identified, P> = {
  title: string; eyebrow: string; icon: ReactNode; emptyText: string; userId: string; queryKey: string; api: CrudApi<T, P>
  renderItem: (item: T) => ReactNode
  renderEditor: (props: { item: T | null; saving: boolean; error: unknown; onSave: (payload: P) => void; onCancel: () => void }) => ReactNode
}

function CollectionCard<T extends Identified, P>({ title, eyebrow, icon, emptyText, userId, queryKey, api, renderItem, renderEditor }: CollectionCardProps<T, P>) {
  const { query, save, remove } = useProfileCollection(queryKey, userId, api)
  const [editing, setEditing] = useState<T | null | undefined>(undefined)
  const items = query.data?.content ?? []
  const finishSave = (payload: P) => save.mutate({ item: editing ?? null, payload }, { onSuccess: () => setEditing(undefined) })

  return (
    <section className="profile-section profile-collection-section">
      <div className="profile-section__heading"><div><span>{eyebrow}</span><h2>{title}</h2></div>{editing === undefined && <button type="button" onClick={() => setEditing(null)}><Plus size={16} /> Thêm mới</button>}</div>
      {query.isLoading && <div className="profile-mini-skeleton" />}
      {query.isError && <div className="profile-section-failure"><div><strong>Không thể tải dữ liệu</strong><p>{getErrorMessage(query.error)}</p></div><button type="button" onClick={() => void query.refetch()}><RefreshCw size={15} /> Thử lại</button></div>}
      {editing !== undefined && renderEditor({ item: editing, saving: save.isPending, error: save.error, onSave: finishSave, onCancel: () => setEditing(undefined) })}
      {!query.isError && editing === undefined && !items.length && <div className="profile-collection-empty">{icon}<p>{emptyText}</p><button type="button" onClick={() => setEditing(null)}><Plus size={15} /> Thêm thông tin</button></div>}
      {editing === undefined && items.length > 0 && <div className="profile-record-list">{items.map((item) => <article key={item.id}><div>{renderItem(item)}</div><div className="profile-record-actions"><button type="button" onClick={() => setEditing(item)} aria-label={`Chỉnh sửa ${title}`}><Edit3 size={16} /></button><button type="button" onClick={() => { if (window.confirm('Xóa thông tin này?')) remove.mutate(item.id) }} aria-label={`Xóa ${title}`} disabled={remove.isPending}><Trash2 size={16} /></button></div></article>)}</div>}
    </section>
  )
}

function EditorActions({ saving, onCancel }: { saving: boolean; onCancel: () => void }) {
  return <div className="profile-form-actions"><Button type="button" variant="ghost" onClick={onCancel}><X size={16} /> Hủy</Button><Button type="submit" disabled={saving}>{saving ? 'Đang lưu...' : 'Lưu thông tin'}</Button></div>
}

function EducationEditor({ item, saving, error, onSave, onCancel }: { item: Education | null; saving: boolean; error: unknown; onSave: (payload: EducationRequest) => void; onCancel: () => void }) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<EducationForm>({ resolver: zodResolver(educationSchema) })
  useEffect(() => { reset(item ? { institutionName: item.institutionName, qualification: item.qualification, fieldOfStudy: item.fieldOfStudy ?? '', startDate: item.startDate ?? '', endDate: item.endDate ?? '', grade: item.grade ?? '', description: item.description ?? '' } : { institutionName: '', qualification: '', startDate: '' }) }, [item, reset])
  return <form className="profile-form profile-inline-editor" onSubmit={handleSubmit((values) => onSave({ ...cleanRecord(values), version: item?.version }))}><div className="profile-form-grid"><label>Trường/Cơ sở đào tạo<span>*</span><input {...register('institutionName')} />{errors.institutionName && <small>Vui lòng nhập tên trường.</small>}</label><label>Bằng cấp<span>*</span><input {...register('qualification')} />{errors.qualification && <small>Vui lòng nhập bằng cấp.</small>}</label><label>Chuyên ngành<input {...register('fieldOfStudy')} /></label><label>Điểm/Xếp loại<input {...register('grade')} /></label><label>Ngày bắt đầu<span>*</span><input type="date" {...register('startDate')} />{errors.startDate && <small>{errors.startDate.message}</small>}</label><label>Ngày kết thúc<input type="date" {...register('endDate')} /></label><label className="profile-form-wide">Mô tả<textarea rows={3} {...register('description')} /></label></div>{Boolean(error) && <p className="profile-form-error">{getErrorMessage(error)}</p>}<EditorActions saving={saving} onCancel={onCancel} /></form>
}

function ExperienceEditor({ item, saving, error, onSave, onCancel }: { item: Experience | null; saving: boolean; error: unknown; onSave: (payload: ExperienceRequest) => void; onCancel: () => void }) {
  const { register, handleSubmit, reset, watch, formState: { errors } } = useForm<ExperienceForm>({ resolver: zodResolver(experienceSchema) })
  useEffect(() => { reset(item ? { employerName: item.employerName, jobTitle: item.jobTitle, employmentType: item.employmentType ?? 'FULL_TIME', location: item.location ?? '', startDate: item.startDate ?? '', endDate: item.endDate ?? '', current: item.current ?? false, description: item.description ?? '', achievements: item.achievements ?? '' } : { employerName: '', jobTitle: '', employmentType: 'FULL_TIME', startDate: '', current: false }) }, [item, reset])
  return <form className="profile-form profile-inline-editor" onSubmit={handleSubmit((values) => onSave({ ...cleanRecord(values), endDate: values.current ? null : values.endDate ?? null, version: item?.version }))}><div className="profile-form-grid"><label>Công ty<span>*</span><input {...register('employerName')} />{errors.employerName && <small>Vui lòng nhập tên công ty.</small>}</label><label>Chức danh<span>*</span><input {...register('jobTitle')} />{errors.jobTitle && <small>Vui lòng nhập chức danh.</small>}</label><label>Loại hình<span>*</span><select {...register('employmentType')}>{['FULL_TIME','PART_TIME','CONTRACT','INTERNSHIP','FREELANCE','TEMPORARY'].map((value) => <option key={value}>{value}</option>)}</select></label><label>Địa điểm<input {...register('location')} /></label><label>Ngày bắt đầu<span>*</span><input type="date" {...register('startDate')} />{errors.startDate && <small>{errors.startDate.message}</small>}</label><label>Ngày kết thúc<input type="date" {...register('endDate')} disabled={watch('current')} /></label><label className="profile-checkbox"><input type="checkbox" {...register('current')} /> Tôi đang làm việc tại đây</label><label className="profile-form-wide">Mô tả<textarea rows={3} {...register('description')} /></label><label className="profile-form-wide">Thành tựu<textarea rows={3} {...register('achievements')} /></label></div>{Boolean(error) && <p className="profile-form-error">{getErrorMessage(error)}</p>}<EditorActions saving={saving} onCancel={onCancel} /></form>
}

function SkillEditor({ item, saving, error, onSave, onCancel }: { item: CandidateSkill | null; saving: boolean; error: unknown; onSave: (payload: CandidateSkillRequest) => void; onCancel: () => void }) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<SkillForm>({ resolver: zodResolver(skillSchema) })
  useEffect(() => { reset(item ? { skillName: item.skillName, skillLevel: item.skillLevel ?? undefined, yearsExperience: item.yearsExperience ?? undefined } : { skillName: '' }) }, [item, reset])
  return <form className="profile-form profile-inline-editor" onSubmit={handleSubmit((values) => onSave({ ...values, version: item?.version }))}><div className="profile-form-grid"><label>Kỹ năng<span>*</span><input {...register('skillName')} />{errors.skillName && <small>Vui lòng nhập kỹ năng.</small>}</label><label>Trình độ<select {...register('skillLevel')}><option value="">Chưa chọn</option>{['BEGINNER','INTERMEDIATE','ADVANCED','EXPERT'].map((value) => <option key={value}>{value}</option>)}</select></label><label>Số năm kinh nghiệm<input type="number" min="0" max="99.9" step="0.1" {...register('yearsExperience', { setValueAs: (value) => value === '' ? undefined : Number(value) })} />{errors.yearsExperience && <small>{String(errors.yearsExperience.message)}</small>}</label></div>{Boolean(error) && <p className="profile-form-error">{getErrorMessage(error)}</p>}<EditorActions saving={saving} onCancel={onCancel} /></form>
}

function LanguageEditor({ item, saving, error, onSave, onCancel }: { item: CandidateLanguage | null; saving: boolean; error: unknown; onSave: (payload: CandidateLanguageRequest) => void; onCancel: () => void }) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<LanguageForm>({ resolver: zodResolver(languageSchema) })
  useEffect(() => { reset(item ? { languageCode: item.languageCode, languageLevel: item.languageLevel ?? undefined } : { languageCode: '' }) }, [item, reset])
  return <form className="profile-form profile-inline-editor" onSubmit={handleSubmit((values) => onSave({ ...values, languageCode: values.languageCode.toLowerCase(), version: item?.version }))}><div className="profile-form-grid"><label>Mã ngôn ngữ<span>*</span><input {...register('languageCode')} placeholder="vi, en..." />{errors.languageCode && <small>Vui lòng nhập mã ngôn ngữ.</small>}</label><label>Trình độ<select {...register('languageLevel')}><option value="">Chưa chọn</option>{['BASIC','CONVERSATIONAL','PROFESSIONAL','NATIVE_OR_BILINGUAL'].map((value) => <option key={value}>{value}</option>)}</select></label></div>{Boolean(error) && <p className="profile-form-error">{getErrorMessage(error)}</p>}<EditorActions saving={saving} onCancel={onCancel} /></form>
}

function CertificateEditor({ item, saving, error, onSave, onCancel }: { item: Certificate | null; saving: boolean; error: unknown; onSave: (payload: CertificateRequest) => void; onCancel: () => void }) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<CertificateForm>({ resolver: zodResolver(certificateSchema) })
  useEffect(() => { reset(item ? { certificateName: item.certificateName, issuerName: item.issuerName, credentialId: item.credentialId ?? '', issueDate: item.issueDate ?? '', expiryDate: item.expiryDate ?? '', verificationUrl: item.verificationUrl ?? '' } : { certificateName: '', issuerName: '', issueDate: '' }) }, [item, reset])
  return <form className="profile-form profile-inline-editor" onSubmit={handleSubmit((values) => onSave({ ...cleanRecord(values), version: item?.version }))}><div className="profile-form-grid"><label>Tên chứng chỉ<span>*</span><input {...register('certificateName')} />{errors.certificateName && <small>Vui lòng nhập tên chứng chỉ.</small>}</label><label>Đơn vị cấp<span>*</span><input {...register('issuerName')} />{errors.issuerName && <small>Vui lòng nhập đơn vị cấp.</small>}</label><label>Mã chứng chỉ<input {...register('credentialId')} /></label><label>Ngày cấp<span>*</span><input type="date" {...register('issueDate')} />{errors.issueDate && <small>{errors.issueDate.message}</small>}</label><label>Ngày hết hạn<input type="date" {...register('expiryDate')} /></label><label>URL xác minh<input type="url" {...register('verificationUrl')} />{errors.verificationUrl && <small>{errors.verificationUrl.message}</small>}</label></div>{Boolean(error) && <p className="profile-form-error">{getErrorMessage(error)}</p>}<EditorActions saving={saving} onCancel={onCancel} /></form>
}

function SocialEditor({ item, saving, error, onSave, onCancel }: { item: SocialLink | null; saving: boolean; error: unknown; onSave: (payload: SocialLinkRequest) => void; onCancel: () => void }) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<SocialLinkForm>({ resolver: zodResolver(socialLinkSchema) })
  useEffect(() => { reset(item ? { linkType: item.linkType ?? undefined, url: item.url, label: item.label ?? '' } : { url: '' }) }, [item, reset])
  return <form className="profile-form profile-inline-editor" onSubmit={handleSubmit((values) => onSave({ ...cleanRecord(values), version: item?.version }))}><div className="profile-form-grid"><label>Loại liên kết<select {...register('linkType')}><option value="">Chưa chọn</option>{['LINKEDIN','GITHUB','GITLAB','PORTFOLIO','WEBSITE','OTHER'].map((value) => <option key={value}>{value}</option>)}</select></label><label>Nhãn<input {...register('label')} /></label><label className="profile-form-wide">URL<span>*</span><input type="url" {...register('url')} placeholder="https://..." />{errors.url && <small>{errors.url.message}</small>}</label></div>{Boolean(error) && <p className="profile-form-error">{getErrorMessage(error)}</p>}<EditorActions saving={saving} onCancel={onCancel} /></form>
}

export function ProfileCollectionSections({ userId }: { userId: string }) {
  return <>
    <CollectionCard<Education, EducationRequest> title="Học vấn" eyebrow="Nền tảng chuyên môn" icon={<BookOpen />} emptyText="Chưa có thông tin học vấn." userId={userId} queryKey="educations" api={educationApi} renderItem={(item) => <><h3>{item.qualification}</h3><p>{item.institutionName}{item.fieldOfStudy ? ` · ${item.fieldOfStudy}` : ''}</p><small>{item.startDate || '—'} – {item.endDate || 'Hiện tại'}</small></>} renderEditor={(props) => <EducationEditor {...props} />} />
    <CollectionCard<Experience, ExperienceRequest> title="Kinh nghiệm" eyebrow="Hành trình làm việc" icon={<Briefcase />} emptyText="Chưa có kinh nghiệm làm việc." userId={userId} queryKey="experiences" api={experienceApi} renderItem={(item) => <><h3>{item.jobTitle}</h3><p>{item.employerName}{item.location ? ` · ${item.location}` : ''}</p><small>{item.startDate || '—'} – {item.current ? 'Hiện tại' : item.endDate || '—'}</small></>} renderEditor={(props) => <ExperienceEditor {...props} />} />
    <CollectionCard<CandidateSkill, CandidateSkillRequest> title="Kỹ năng" eyebrow="Năng lực nghề nghiệp" icon={<Code2 />} emptyText="Chưa có kỹ năng." userId={userId} queryKey="skills" api={skillApi} renderItem={(item) => <><h3>{item.skillName}</h3><p>{item.skillLevel || 'Chưa chọn trình độ'}{item.yearsExperience != null ? ` · ${item.yearsExperience} năm` : ''}</p></>} renderEditor={(props) => <SkillEditor {...props} />} />
    <CollectionCard<CandidateLanguage, CandidateLanguageRequest> title="Ngôn ngữ" eyebrow="Khả năng giao tiếp" icon={<Languages />} emptyText="Chưa có ngôn ngữ." userId={userId} queryKey="languages" api={languageApi} renderItem={(item) => <><h3>{item.displayName || item.languageCode}</h3><p>{item.languageLevel || 'Chưa chọn trình độ'}</p></>} renderEditor={(props) => <LanguageEditor {...props} />} />
    <CollectionCard<Certificate, CertificateRequest> title="Chứng chỉ" eyebrow="Thành tựu chuyên môn" icon={<Award />} emptyText="Chưa có chứng chỉ." userId={userId} queryKey="certificates" api={certificateApi} renderItem={(item) => <><h3>{item.certificateName}</h3><p>{item.issuerName}</p><small>{item.issueDate || 'Chưa cập nhật ngày cấp'}</small></>} renderEditor={(props) => <CertificateEditor {...props} />} />
    <CollectionCard<SocialLink, SocialLinkRequest> title="Liên kết nghề nghiệp" eyebrow="Hiện diện trực tuyến" icon={<Globe2 />} emptyText="Chưa có liên kết nghề nghiệp." userId={userId} queryKey="social-links" api={socialLinkApi} renderItem={(item) => <><h3>{item.label || item.linkType || 'Liên kết'}</h3><a href={item.url} target="_blank" rel="noreferrer">{item.url}</a></>} renderEditor={(props) => <SocialEditor {...props} />} />
  </>
}
