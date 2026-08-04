import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { authApi } from '../api/auth.js'
import { useAuth } from '../auth/authContext.js'

const initialForm = { displayName: '', username: '', email: '', password: '', confirmPassword: '' }

export default function RegisterPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to="/" replace />

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setFieldErrors({})
    if (form.password !== form.confirmPassword) {
      setFieldErrors({ confirmPassword: 'Mật khẩu xác nhận chưa trùng khớp' })
      return
    }
    setSubmitting(true)
    try {
      const { confirmPassword, ...payload } = form
      void confirmPassword
      await authApi.register(payload)
      navigate('/dang-nhap', { replace: true, state: { registered: true } })
    } catch (requestError) {
      setError(requestError.message)
      setFieldErrors(requestError.details?.fieldErrors ?? {})
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="auth-page auth-page-register">
      <div className="auth-story" aria-hidden="true">
        <span className="auth-index">TRANG ĐẦU TIÊN</span>
        <blockquote>Hồ sơ đọc của bạn bắt đầu từ một cái tên.</blockquote>
        <div className="auth-leaf">N</div>
      </div>
      <div className="auth-panel">
        <p className="eyebrow">Gia nhập NovelScout</p>
        <h1>Tạo không gian đọc của riêng bạn.</h1>
        <p className="auth-intro">Chỉ mất một phút. Gợi ý sẽ tốt dần theo mỗi câu chuyện bạn đọc.</p>
        {error && <div className="form-notice error" role="alert">{error}</div>}
        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="form-row">
            <label>Tên hiển thị
              <input autoFocus autoComplete="name" name="displayName" required maxLength="150" value={form.displayName} onChange={updateField} placeholder="Minh Hào" />
              {fieldErrors.displayName && <small>{fieldErrors.displayName}</small>}
            </label>
            <label>Tên đăng nhập
              <input autoComplete="username" name="username" required minLength="3" maxLength="30" value={form.username} onChange={updateField} placeholder="minhhao" />
              {fieldErrors.username && <small>{fieldErrors.username}</small>}
            </label>
          </div>
          <label>Email
            <input autoComplete="email" name="email" required type="email" value={form.email} onChange={updateField} placeholder="ban@example.com" />
            {fieldErrors.email && <small>{fieldErrors.email}</small>}
          </label>
          <div className="form-row">
            <label>Mật khẩu
              <input autoComplete="new-password" name="password" required minLength="8" maxLength="72" type="password" value={form.password} onChange={updateField} placeholder="Tối thiểu 8 ký tự" />
              {fieldErrors.password && <small>{fieldErrors.password}</small>}
            </label>
            <label>Nhập lại mật khẩu
              <input autoComplete="new-password" name="confirmPassword" required type="password" value={form.confirmPassword} onChange={updateField} placeholder="Nhập lại mật khẩu" />
              {fieldErrors.confirmPassword && <small>{fieldErrors.confirmPassword}</small>}
            </label>
          </div>
          <button className="button auth-submit" type="submit" disabled={submitting}>
            {submitting ? 'Đang tạo tài khoản…' : 'Tạo tài khoản'}
          </button>
        </form>
        <p className="auth-switch">Đã có tài khoản? <Link to="/dang-nhap">Đăng nhập →</Link></p>
      </div>
    </section>
  )
}
