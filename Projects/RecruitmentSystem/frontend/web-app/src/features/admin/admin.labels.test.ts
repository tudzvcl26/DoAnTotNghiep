import { describe, expect, it } from 'vitest'
import { activeStateLabels, notificationChannelLabels, notificationEventLabels, notificationResourceLabel } from './admin.labels'

describe('Vietnamese presentation labels', () => {
  it('does not present notification enums, channels or active states as raw codes', () => {
    expect(notificationEventLabels.APPLICATION_SUBMITTED).toBe('Đã nộp đơn ứng tuyển')
    expect(notificationEventLabels.JOB_PUBLISHED).toBe('Việc làm đã đăng')
    expect(notificationChannelLabels.IN_APP).toBe('Trong ứng dụng')
    expect(activeStateLabels.ACTIVE).toBe('Đang hoạt động')
    expect(activeStateLabels.INACTIVE).toBe('Ngừng hoạt động')
    expect(activeStateLabels.DISABLED).toBe('Đã vô hiệu hóa')
  })

  it('localizes known notification resources and hides unknown backend codes', () => {
    expect(notificationResourceLabel('APPLICATION')).toBe('Đơn ứng tuyển')
    expect(notificationResourceLabel('UNRECOGNIZED_INTERNAL_CODE')).toBe('Tài nguyên liên quan')
  })
})
