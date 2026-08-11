import axios from 'axios'
import type { GatewayErrorResponse, ServiceErrorResponse } from '../../types/api/errors'

const friendlyMessages: Record<number, string> = {
  400: 'Thông tin gửi lên chưa hợp lệ. Vui lòng kiểm tra lại.',
  401: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
  403: 'Bạn không có quyền thực hiện thao tác này.',
  404: 'Không tìm thấy nội dung bạn yêu cầu.',
  409: 'Dữ liệu đã thay đổi hoặc bị trùng. Vui lòng tải lại và thử tiếp.',
  413: 'Tệp tải lên vượt quá dung lượng cho phép.',
  500: 'Hệ thống đang gặp sự cố. Vui lòng thử lại sau.',
  502: 'Dịch vụ tạm thời chưa sẵn sàng. Vui lòng thử lại sau.',
  503: 'Dịch vụ tạm thời chưa sẵn sàng. Vui lòng thử lại sau.',
  504: 'Yêu cầu mất quá nhiều thời gian. Vui lòng thử lại.',
}

export class AppError extends Error {
  readonly status?: number
  readonly code?: string
  readonly correlationId?: string
  readonly fieldErrors?: Record<string, string>
  readonly retryable?: boolean

  constructor(message: string, options: {
    status?: number
    code?: string
    correlationId?: string
    fieldErrors?: Record<string, string>
    retryable?: boolean
  } = {}) {
    super(message)
    this.name = 'AppError'
    Object.assign(this, options)
  }
}

export function normalizeApiError(error: unknown): AppError {
  if (error instanceof AppError) return error

  if (!axios.isAxiosError(error)) {
    return new AppError('Đã có lỗi không mong muốn xảy ra. Vui lòng thử lại.')
  }

  const status = error.response?.status
  const data = error.response?.data as (ServiceErrorResponse & GatewayErrorResponse) | undefined
  const responseCorrelation = error.response?.headers?.['x-correlation-id'] as string | undefined
  const correlationId = data?.traceId ?? data?.correlationId ?? responseCorrelation
  const backendMessage = data?.message
  const canUseBackendMessage = backendMessage && backendMessage.length <= 240 && !backendMessage.toLowerCase().includes('exception')
  const message = canUseBackendMessage ? backendMessage : friendlyMessages[status ?? 0]

  return new AppError(message ?? 'Không thể kết nối đến hệ thống. Vui lòng kiểm tra mạng và thử lại.', {
    status,
    code: data?.code ?? data?.error,
    correlationId,
    fieldErrors: data?.errors ?? data?.details,
    retryable: data?.retryable,
  })
}

export function getErrorMessage(error: unknown): string {
  return normalizeApiError(error).message
}
