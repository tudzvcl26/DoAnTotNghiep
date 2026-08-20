export type MenuLink = { label: string; to: string }
export type MenuSection = { title: string; links: MenuLink[] }
export type MegaMenuDefinition = { label: string; sections: MenuSection[] }

export const megaMenus: MegaMenuDefinition[] = [
  {
    label: 'Việc làm',
    sections: [
      { title: 'Khám phá', links: [
        { label: 'Tìm việc làm', to: '/jobs' },
        { label: 'Việc làm mới nhất', to: '/jobs?sort=publishedAt,desc' },
        { label: 'Đơn ứng tuyển của tôi', to: '/candidate/applications' },
      ] },
      { title: 'Theo ngành', links: ['IT', 'Marketing', 'Kinh doanh', 'Tài chính', 'Nhân sự', 'Logistics'].map((label) => ({ label, to: `/jobs?keyword=${encodeURIComponent(label)}` })) },
      { title: 'Theo địa điểm', links: ['Hồ Chí Minh', 'Hà Nội', 'Đà Nẵng', 'Bình Dương', 'Đồng Nai'].map((label) => ({ label, to: `/jobs?location=${encodeURIComponent(label)}` })) },
    ],
  },
  {
    label: 'Tạo CV',
    sections: [
      { title: 'CV của bạn', links: [
        { label: 'Quản lý CV', to: '/candidate/resumes' },
        { label: 'Hồ sơ nghề nghiệp', to: '/candidate/profile' },
      ] },
      { title: 'Trợ lý thông minh', links: [
        { label: 'AI Resume Analysis', to: '/candidate/ai-career' },
        { label: 'Job Recommendations', to: '/candidate/ai-career' },
      ] },
    ],
  },
  {
    label: 'Công cụ',
    sections: [
      { title: 'AI nghề nghiệp', links: [
        { label: 'AI Resume Analysis', to: '/candidate/ai-career' },
        { label: 'CV Matching', to: '/candidate/ai-career' },
      ] },
    ],
  },
  {
    label: 'Cẩm nang',
    sections: [
      { title: 'Phát triển sự nghiệp', links: [
        { label: 'Hồ sơ nghề nghiệp', to: '/candidate/profile' },
        { label: 'Quản lý CV', to: '/candidate/resumes' },
        { label: 'Theo dõi ứng tuyển', to: '/candidate/applications' },
      ] },
    ],
  },
  {
    label: 'Công ty',
    sections: [
      { title: 'Khám phá công ty', links: [
        { label: 'Danh sách công ty', to: '/companies' },
        { label: 'Tìm công ty', to: '/companies' },
      ] },
    ],
  },
]
