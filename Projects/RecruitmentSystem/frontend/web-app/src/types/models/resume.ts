export type ResumeAsset = {
  id: string
  assetKind: 'RESUME'
  storageKey: string
  originalFilename: string
  contentType: string
  sizeBytes: number
  checksum: string
  publicUrl: string | null
  assetStatus: 'ACTIVE' | 'ARCHIVED' | 'DELETED'
  assetVersion: number
  current: boolean
  createdAt: string
  version: number
}
