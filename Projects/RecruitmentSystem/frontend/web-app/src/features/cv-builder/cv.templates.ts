import type { CvContent, CvTemplateId } from './cv.types'

export type CvTemplate = {
  id: CvTemplateId
  name: string
  style: 'Đơn giản' | 'Chuyên nghiệp' | 'Hiện đại' | 'ATS' | 'Sinh viên'
  description: string
  bestFor: string
}

export const cvTemplates: CvTemplate[] = [
  { id: 'classic', name: 'Classic Green', style: 'Đơn giản', description: 'Bố cục một cột rõ ràng, dễ đọc và cân bằng.', bestFor: 'Kinh doanh, vận hành' },
  { id: 'professional', name: 'Executive', style: 'Chuyên nghiệp', description: 'Tiêu đề đậm, nhịp thông tin chặt chẽ cho ứng viên giàu kinh nghiệm.', bestFor: 'Quản lý, tài chính' },
  { id: 'modern', name: 'Modern Mint', style: 'Hiện đại', description: 'Mảng màu thương hiệu và typography hiện đại.', bestFor: 'Công nghệ, sáng tạo' },
  { id: 'ats', name: 'ATS Focus', style: 'ATS', description: 'Tối giản, tuyến tính và thân thiện với hệ thống sàng lọc.', bestFor: 'Mọi ngành nghề' },
  { id: 'student', name: 'First Step', style: 'Sinh viên', description: 'Ưu tiên học vấn, dự án và hoạt động nổi bật.', bestFor: 'Sinh viên, fresher' },
]

export const sampleCv: CvContent = {
  personalInfo: { fullName: 'Nguyễn Minh Anh', headline: 'Chuyên viên phát triển sản phẩm', email: 'minhanh@example.com', phone: '090 123 4567', location: 'Thành phố Hồ Chí Minh', website: 'portfolio.example.com' },
  summary: 'Tôi mong muốn tạo ra những sản phẩm hữu ích bằng tư duy lấy người dùng làm trung tâm.',
  experiences: [{ position: 'Product Executive', company: 'Công ty Công nghệ Việt', startDate: '2023', endDate: 'Hiện tại', description: 'Phối hợp cùng đội ngũ kỹ thuật để cải thiện trải nghiệm sản phẩm.' }],
  education: [{ school: 'Đại học Kinh tế', degree: 'Cử nhân Quản trị kinh doanh', startDate: '2019', endDate: '2023', description: '' }],
  skills: ['Phân tích dữ liệu', 'Quản lý dự án', 'Giao tiếp'],
  projects: [], certifications: [], awards: [], activities: [],
}
