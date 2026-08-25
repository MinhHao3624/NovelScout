import { apiRequest } from './client.js'

export const authApi = {
  me: () => apiRequest('/auth/me'),
  login: (credentials) => apiRequest('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
  register: (data) => apiRequest('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  sendOtp: (email) => apiRequest('/auth/send-otp', { method: 'POST', body: JSON.stringify({ email }) }),
  registerWithOtp: (data) => apiRequest('/auth/register-with-otp', { method: 'POST', body: JSON.stringify(data) }),
  loginWithGoogle: (credential) => apiRequest('/auth/google', { method: 'POST', body: JSON.stringify({ credential }) }),
  logout: () => apiRequest('/auth/logout', { method: 'POST' }),
  updateProfile: (data) => apiRequest('/users/me/profile', { method: 'PUT', body: JSON.stringify(data) }),
  changePassword: (data) => apiRequest('/users/me/password', { method: 'PUT', body: JSON.stringify(data) }),
}
