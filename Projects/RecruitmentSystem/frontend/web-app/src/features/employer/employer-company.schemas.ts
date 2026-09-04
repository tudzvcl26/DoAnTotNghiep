import { z } from 'zod'
import { COMPANY_SIZES, COMPANY_TYPES } from '../../types/models/company'

const optionalEmail = z.union([z.literal(''), z.string().email('Email không hợp lệ.').max(255, 'Email không được vượt quá 255 ký tự.')])

export const employerCompanySchema = z.object({
  name: z.string().trim().min(1, 'Tên công ty là bắt buộc.').max(255, 'Tên công ty không được vượt quá 255 ký tự.'),
  description: z.string().max(5000, 'Giới thiệu không được vượt quá 5000 ký tự.'),
  website: z.string().max(255, 'Website không được vượt quá 255 ký tự.'),
  email: optionalEmail,
  phone: z.string().max(50, 'Số điện thoại không được vượt quá 50 ký tự.'),
  taxCode: z.string().max(100, 'Mã số thuế không được vượt quá 100 ký tự.'),
  companyType: z.union([z.literal(''), z.enum(COMPANY_TYPES)]).refine((value): boolean => value !== '', 'Vui lòng chọn loại hình công ty.'),
  companySize: z.union([z.literal(''), z.enum(COMPANY_SIZES)]).refine((value): boolean => value !== '', 'Vui lòng chọn quy mô công ty.'),
  logoUrl: z.string().max(500, 'Logo URL không được vượt quá 500 ký tự.'),
  bannerUrl: z.string().max(500, 'Banner URL không được vượt quá 500 ký tự.'),
})

export type EmployerCompanyForm = z.infer<typeof employerCompanySchema>
