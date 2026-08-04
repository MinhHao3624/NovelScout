import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/authContext.js'

export default function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ login: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to="/" replace />

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(form)
      navigate(location.state?.from || '/', { replace: true })
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="auth-page">
      <div className="auth-story" aria-hidden="true">
        <span className="auth-index">CHƯƠNG 01</span>
        <blockquote>“Mỗi độc giả đều có một câu chuyện đang chờ được tìm thấy.”</blockquote>
        <div className="auth-leaf">N</div>
      </div>
      <div className="auth-panel">
        <p className="eyebrow">Chào mừng trở lại</p>
        <h1>Tiếp tục hành trình đọc.</h1>
        <p className="auth-intro">Đăng nhập để giữ tiến độ và nhận những gợi ý dành riêng cho bạn.</p>
        {location.state?.registered && <div className="form-notice success">Tạo tài khoản thành công. Đăng nhập để bắt đầu nhé.</div>}
        {location.state?.passwordChanged && <div className="form-notice success">Đổi mật khẩu thành công. Hãy đăng nhập lại nhé.</div>}
        {error && <div className="form-notice error" role="alert">{error}</div>}
        <form className="auth-form" onSubmit={handleSubmit}>
          <label>Email hoặc tên đăng nhập
            <input autoFocus autoComplete="username" name="login" required value={form.login}
              onChange={(event) => setForm({ ...form, login: event.target.value })} placeholder="hao_nguyen" />
          </label>
          <label>Mật khẩu
            <input autoComplete="current-password" name="password" required type="password" value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })} placeholder="Tối thiểu 8 ký tự" />
          </label>
          <button className="button auth-submit" type="submit" disabled={submitting}>
            {submitting ? 'Đang đăng nhập…' : 'Đăng nhập'}
          </button>
        </form>
        <p className="auth-switch">Chưa có tài khoản? <Link to="/dang-ky">Tạo tài khoản →</Link></p>
      </div>
    </section>
  )
}
