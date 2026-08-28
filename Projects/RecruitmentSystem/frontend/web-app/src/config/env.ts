const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim()

if (!apiBaseUrl) {
  throw new Error('VITE_API_BASE_URL is required.')
}

let parsedApiUrl: URL
try {
  parsedApiUrl = new URL(apiBaseUrl)
} catch {
  throw new Error('VITE_API_BASE_URL must be a valid URL.')
}

const forbiddenServicePorts = new Set(['8081', '8082', '8083', '8084', '8085', '8086', '8087', '11434'])
if (forbiddenServicePorts.has(parsedApiUrl.port)) {
  throw new Error('Frontend must connect through API Gateway, not a backend service port.')
}

export const env = Object.freeze({
  apiBaseUrl: parsedApiUrl.toString().replace(/\/$/, ''),
})
