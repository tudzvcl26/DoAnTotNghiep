import { normalizeCvContent, type SaveCvPayload } from './cv.types'
import { cvPresetDefinitions } from './cv.presets'

export type CvDraft = { snapshot: SaveCvPayload; baseSignature: string; updatedAt: number }
const prefix = 'recruitment.cv-draft.v1:'
export const cvDraftKey = (ownerId: string | undefined, documentKey: string) => ownerId ? `${prefix}${ownerId}:${documentKey}` : null

export function readCvDraft(key: string | null): CvDraft | null {
  if (!key) return null
  try {
    const raw = sessionStorage.getItem(key)
    if (!raw) return null
    const value = JSON.parse(raw) as CvDraft
    if (!value.snapshot || typeof value.snapshot.title !== 'string' || !value.snapshot.content ||
      !Object.hasOwn(cvPresetDefinitions, value.snapshot.templateId) ||
      !['vi', 'en'].includes(value.snapshot.language) || typeof value.baseSignature !== 'string') return null
    return { ...value, snapshot: { ...value.snapshot, content: normalizeCvContent(value.snapshot.content, value.snapshot.templateId) } }
  } catch { return null }
}

export function writeCvDraft(key: string | null, draft: CvDraft): boolean {
  if (!key) return false
  try { sessionStorage.setItem(key, JSON.stringify(draft)); return true } catch { return false }
}

export function removeCvDraft(key: string | null): void {
  if (!key) return
  try { sessionStorage.removeItem(key) } catch { /* Keep server saving usable when browser storage is unavailable. */ }
}
