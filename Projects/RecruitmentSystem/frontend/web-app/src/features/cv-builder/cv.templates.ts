import { defaultCvDesignConfig, type CvContent, type CvTemplateId } from './cv.types'
import { cvPresetDefinitions, cvTemplateCategories } from './cv.presets'

export type CvTemplate = {
  id: CvTemplateId
  name: string
  style: typeof cvTemplateCategories[number]
  description: string
  bestFor: string
  highlights: string[]
}

export const cvTemplates: CvTemplate[] = Object.entries(cvPresetDefinitions).map(([id, preset]) => ({
  id: id as CvTemplateId, name: preset.name, style: preset.category, description: preset.description, bestFor: preset.bestFor,
  highlights: [preset.category, preset.layout.startsWith('sidebar') ? 'Hai cột thông tin' : 'Một cột nội dung', 'Giữ nguyên nội dung khi đổi mẫu'],
}))

export const sampleCv: CvContent = {
  personalInfo: { fullName: 'Nguyễn Minh Anh', headline: 'Chuyên viên phát triển sản phẩm', email: 'minhanh@example.com', phone: '090 123 4567', location: 'Thành phố Hồ Chí Minh', website: 'portfolio.example.com' },
  summary: 'Tôi mong muốn tạo ra những sản phẩm hữu ích bằng tư duy lấy người dùng làm trung tâm.',
  experiences: [{ position: 'Product Executive', company: 'Công ty Công nghệ Việt', startDate: '2023', endDate: 'Hiện tại', description: 'Phối hợp cùng đội ngũ kỹ thuật để cải thiện trải nghiệm sản phẩm.' }],
  education: [{ school: 'Đại học Kinh tế', degree: 'Cử nhân Quản trị kinh doanh', startDate: '2019', endDate: '2023', description: '' }],
  skills: ['Phân tích dữ liệu', 'Quản lý dự án', 'Giao tiếp'],
  projects: [], certifications: [], awards: [], activities: [], customSections: [], designConfig: defaultCvDesignConfig('modern'),
}
