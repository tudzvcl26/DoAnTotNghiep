import { z } from 'zod'

export const applyJobSchema = z.object({
  coverLetter: z.string().max(5000, 'Thư ứng tuyển không được vượt quá 5.000 ký tự.'),
})

export type ApplyJobFormValues = z.infer<typeof applyJobSchema>
