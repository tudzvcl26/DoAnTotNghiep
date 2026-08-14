import { z } from 'zod'

const optionalUuid = z.string().trim().refine((value) => value === '' || z.uuid().safeParse(value).success, 'UUID không hợp lệ')

export const catalogSchema = z.object({
  name: z.string().trim().min(1, 'Tên là bắt buộc').max(100),
  slug: z.string().trim().min(1, 'Slug là bắt buộc').max(120).regex(/^[a-z0-9]+(?:-[a-z0-9]+)*$/, 'Slug chỉ gồm chữ thường, số và dấu gạch ngang'),
  description: z.string().trim().max(500), icon: z.string().trim().max(255), active: z.boolean(),
  displayOrder: z.number().int().min(0), parentId: optionalUuid,
})

export const personalNotificationSchema = z.object({
  recipientUserId: z.uuid('Recipient user ID phải là UUID hợp lệ'),
  eventType: z.enum(['APPLICATION_SUBMITTED','APPLICATION_WITHDRAWN','APPLICATION_STATUS_CHANGED','JOB_APPROVED','JOB_REJECTED','COMPANY_VERIFIED','COMPANY_REJECTED','PASSWORD_CHANGED','WELCOME','SYSTEM_ANNOUNCEMENT']),
  title: z.string().trim().min(1, 'Tiêu đề là bắt buộc').max(200), content: z.string().trim().min(1, 'Nội dung là bắt buộc').max(4000),
  relatedResourceType: z.string().trim().max(50), relatedResourceId: optionalUuid,
})

export const broadcastSchema = z.object({ title: z.string().trim().min(1, 'Tiêu đề là bắt buộc').max(200), content: z.string().trim().min(1, 'Nội dung là bắt buộc').max(4000) })

export const templateSchema = z.object({
  code: z.string().trim().min(1, 'Code là bắt buộc').max(100),
  eventType: personalNotificationSchema.shape.eventType,
  channel: z.enum(['IN_APP','EMAIL','PUSH']),
  titleTemplate: z.string().trim().min(1, 'Title template là bắt buộc').max(200),
  contentTemplate: z.string().trim().min(1, 'Content template là bắt buộc'),
})
