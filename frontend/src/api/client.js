const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'

export class ApiError extends Error {
  constructor(message, status, details) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

async function getCsrfToken() {
  const response = await fetch(`${API_BASE_URL}/auth/csrf`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
  })
  if (!response.ok) throw new ApiError('Không thể thiết lập phiên bảo mật', response.status)

  const csrf = await response.json()
  const cookie = document.cookie
    .split('; ')
    .find((item) => item.startsWith('XSRF-TOKEN='))
  const token = cookie ? decodeURIComponent(cookie.substring('XSRF-TOKEN='.length)) : null

  if (!token) throw new ApiError('Không thể đọc phiên bảo mật', 0)
  return { headerName: csrf.headerName, token }
}

export async function apiRequest(path, options = {}) {
  const method = (options.method ?? 'GET').toUpperCase()
  const headers = {
    Accept: 'application/json',
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...options.headers,
  }

  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const csrf = await getCsrfToken()
    headers[csrf.headerName] = csrf.token
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
    ...options,
    method,
    headers,
  })

  if (response.status === 204) return null
  const contentType = response.headers.get('content-type') ?? ''
  const payload = contentType.includes('application/json') ? await response.json() : await response.text()

  if (!response.ok) {
    throw new ApiError(payload?.message ?? 'Không thể kết nối đến hệ thống', response.status, payload)
  }
  return payload
}
