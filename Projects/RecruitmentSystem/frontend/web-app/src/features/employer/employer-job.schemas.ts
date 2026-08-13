import { z } from 'zod'
import { EMPLOYMENT_TYPES, EXPERIENCE_LEVELS } from '../../types/models/job'

const optionalMoney = z.union([z.literal(''), z.string().regex(/^\d+(\.\d{1,2})?$/, 'Giá trị tiền không hợp lệ.')])

export const employerJobSchema = z.object({
  title: z.string().trim().min(1, 'Tiêu đề là bắt buộc.').max(255, 'Tiêu đề không được vượt quá 255 ký tự.'),
  jobCode: z.string().trim().min(1, 'Mã công việc là bắt buộc.').max(50, 'Mã công việc không được vượt quá 50 ký tự.'),
  description: z.string(),
  requirements: z.string(),
  responsibilities: z.string(),
  salaryMin: optionalMoney,
  salaryMax: optionalMoney,
  currency: z.string().max(10, 'Đơn vị tiền tệ không được vượt quá 10 ký tự.'),
  employmentType: z.enum(EMPLOYMENT_TYPES),
  experienceLevel: z.enum(EXPERIENCE_LEVELS),
  quantity: z.number().int('Số lượng phải là số nguyên.').min(1, 'Số lượng tối thiểu là 1.'),
  applicationDeadline: z.string(),
  remoteAllowed: z.boolean(),
  categoryId: z.string().uuid('Vui lòng chọn danh mục công việc.'),
}).superRefine((value, context) => {
  if (value.salaryMin && value.salaryMax && Number(value.salaryMin) > Number(value.salaryMax)) {
    context.addIssue({ code: 'custom', path: ['salaryMax'], message: 'Lương tối đa phải lớn hơn hoặc bằng lương tối thiểu.' })
  }
})

export type EmployerJobFormValues = z.infer<typeof employerJobSchema>
