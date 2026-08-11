import { z } from 'zod'

export const loginSchema = z.object({
  email: z.email('Email không đúng định dạng.'),
  password: z.string().min(1, 'Vui lòng nhập mật khẩu.'),
})

export const registerSchema = z.object({
  fullName: z.string().trim().min(2, 'Họ tên cần ít nhất 2 ký tự.').max(150, 'Họ tên quá dài.'),
  email: z.email('Email không đúng định dạng.'),
  phone: z.string().trim().max(30, 'Số điện thoại quá dài.').optional(),
  password: z.string().min(8, 'Mật khẩu cần ít nhất 8 ký tự.').max(100, 'Mật khẩu quá dài.'),
  confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Mật khẩu xác nhận không khớp.',
  path: ['confirmPassword'],
})

export type LoginFormValues = z.infer<typeof loginSchema>
export type RegisterFormValues = z.infer<typeof registerSchema>
