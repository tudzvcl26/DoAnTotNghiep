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
        { label: 'Việc làm phù hợp', to: '/candidate' },
        { label: 'Việc làm lương cao', to: '/jobs?focus=high-salary' },
        { label: 'Việc làm Remote', to: '/jobs?remote=true' },
      ] },
      { title: 'Theo ngành', links: ['IT', 'Marketing', 'Kinh doanh', 'Tài chính', 'Nhân sự', 'Logistics'].map((label) => ({ label, to: `/jobs?keyword=${encodeURIComponent(label)}` })) },
      { title: 'Theo địa điểm', links: ['Hồ Chí Minh', 'Hà Nội', 'Đà Nẵng', 'Bình Dương', 'Đồng Nai'].map((label) => ({ label, to: `/jobs?location=${encodeURIComponent(label)}` })) },
    ],
  },
  {
    label: 'Tạo CV',
    sections: [
      { title: 'CV của bạn', links: [
        { label: 'Tạo CV', to: '/candidate' },
        { label: 'Mẫu CV', to: '/candidate' },
        { label: 'Quản lý CV', to: '/candidate' },
      ] },
      { title: 'Trợ lý thông minh', links: [
        { label: 'AI Resume Analysis', to: '/candidate' },
        { label: 'AI CV Improvement', to: '/candidate' },
        { label: 'Cover Letter', to: '/candidate' },
      ] },
    ],
  },
  {
    label: 'Công cụ',
    sections: [
      { title: 'AI nghề nghiệp', links: [
        { label: 'AI Resume Analysis', to: '/candidate' },
        { label: 'CV Matching', to: '/candidate' },
        { label: 'AI Cover Letter', to: '/candidate' },
        { label: 'Công cụ nghề nghiệp', to: '/' },
      ] },
    ],
  },
  {
    label: 'Cẩm nang',
    sections: [
      { title: 'Phát triển sự nghiệp', links: ['Viết CV', 'Phỏng vấn', 'Tìm việc', 'Phát triển nghề nghiệp', 'Kỹ năng'].map((label) => ({ label, to: '/' })) },
    ],
  },
  {
    label: 'Công ty',
    sections: [
      { title: 'Khám phá công ty', links: [
        { label: 'Danh sách công ty', to: '/companies' },
        { label: 'Công ty nổi bật', to: '/companies?featured=true' },
        { label: 'Công ty đang tuyển', to: '/companies?hiring=true' },
      ] },
    ],
  },
]
