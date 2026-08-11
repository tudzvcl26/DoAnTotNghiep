export type GatewayErrorResponse = {
  timestamp?: string
  status: number
  code: string
  message: string
  path?: string
  traceId?: string
}

export type ServiceErrorResponse = {
  success?: boolean
  status?: number
  error?: string
  code?: string
  message?: string
  errors?: Record<string, string>
  details?: Record<string, string>
  path?: string
  retryable?: boolean
  correlationId?: string
  timestamp?: string
}
