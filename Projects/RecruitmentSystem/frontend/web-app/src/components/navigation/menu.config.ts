export type MenuLink = { label: string; to: string }
export type MenuSection = { title: string; links: MenuLink[] }
export type MegaMenuDefinition = { label: string; activePrefixes: string[]; sections: MenuSection[] }

export const megaMenus: MegaMenuDefinition[] = [
  {
    label: 'Việc làm',
    activePrefixes: ['/jobs', '/companies'],
    sections: [
      { title: 'Khám phá', links: [
        { label: 'Tìm việc làm', to: '/jobs' },
        { label: 'Việc làm mới nhất', to: '/jobs?sort=publishedAt,desc' },
        { label: 'Đơn ứng tuyển của tôi', to: '/candidate/applications' },
        { label: 'Khám phá công ty', to: '/companies' },
      ] },
      { title: 'Theo ngành', links: ['IT', 'Marketing', 'Kinh doanh', 'Tài chính', 'Nhân sự', 'Logistics'].map((label) => ({ label, to: `/jobs?keyword=${encodeURIComponent(label)}` })) },
      { title: 'Theo địa điểm', links: ['Hồ Chí Minh', 'Hà Nội', 'Đà Nẵng', 'Bình Dương', 'Đồng Nai'].map((label) => ({ label, to: `/jobs?location=${encodeURIComponent(label)}` })) },
    ],
  },
  {
    label: 'Tạo CV',
    activePrefixes: ['/cv', '/candidate/resumes'],
    sections: [
      { title: 'Tạo và quản lý', links: [
        { label: 'Tạo CV', to: '/cv/templates' },
        { label: 'Mẫu CV', to: '/cv/templates' },
        { label: 'CV của tôi', to: '/cv' },
      ] },
      { title: 'Tài liệu của bạn', links: [
        { label: 'Tải CV lên', to: '/candidate/resumes' },
        { label: 'Tạo từ hồ sơ', to: '/cv/new' },
      ] },
      { title: 'Hướng dẫn', links: [
        { label: 'Hướng dẫn viết CV', to: '/cv/templates#guide' },
        { label: 'Hồ sơ nghề nghiệp', to: '/candidate/profile' },
      ] },
    ],
  },
  {
    label: 'Công cụ',
    activePrefixes: ['/candidate/ai-career', '/candidate/profile'],
    sections: [
      { title: 'AI nghề nghiệp', links: [
        { label: 'Phân tích CV bằng AI', to: '/candidate/ai-career' },
        { label: 'Đánh giá độ phù hợp CV', to: '/candidate/ai-career' },
      ] },
    ],
  },
  {
    label: 'Cẩm nang nghề nghiệp',
    activePrefixes: ['/candidate/applications'],
    sections: [
      { title: 'Phát triển sự nghiệp', links: [
        { label: 'Hồ sơ nghề nghiệp', to: '/candidate/profile' },
        { label: 'Quản lý CV', to: '/candidate/resumes' },
        { label: 'Theo dõi ứng tuyển', to: '/candidate/applications' },
      ] },
    ],
  },
]
