import { useState, useEffect } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { authApi } from '../api/auth.js'
import { useAuth } from '../auth/authContext.js'

const initialForm = { displayName: '', username: '', email: '', password: '', confirmPassword: '', otpCode: '' }

export default function RegisterPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState('')
  const [successMsg, setSuccessMsg] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)

  // OTP State
  const [sendingOtp, setSendingOtp] = useState(false)
  const [otpSent, setOtpSent] = useState(false)
  const [countdown, setCountdown] = useState(0)

  useEffect(() => {
    let timer
    if (countdown > 0) {
      timer = setInterval(() => setCountdown((prev) => prev - 1), 1000)
    }
    return () => clearInterval(timer)
  }, [countdown])

  if (user) return <Navigate to="/" replace />

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  const handleSendOtp = async () => {
    if (!form.email || !form.email.includes('@')) {
      setFieldErrors({ email: 'Vui lòng nhập địa chỉ email hợp lệ trước khi gửi mã' })
      return
    }

    setError('')
    setSuccessMsg('')
    setFieldErrors({})
    setSendingOtp(true)

    try {
      const res = await authApi.sendOtp(form.email)
      setOtpSent(true)
      setCountdown(60)
      setSuccessMsg(res.message || `Mã xác thực 6 chữ số đã được gửi tới hòm thư ${form.email}`)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSendingOtp(false)
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSuccessMsg('')
    setFieldErrors({})

    if (!otpSent) {
      setError('Vui lòng bấm "Gửi mã xác nhận" qua Email trước khi đăng ký!')
      return
    }

    if (form.password !== form.confirmPassword) {
      setFieldErrors({ confirmPassword: 'Mật khẩu xác nhận chưa trùng khớp' })
      return
    }

    if (!form.otpCode || form.otpCode.trim().length !== 6) {
      setFieldErrors({ otpCode: 'Vui lòng nhập đủ mã xác thực OTP 6 chữ số' })
      return
    }

    setSubmitting(true)
    try {
      const { confirmPassword, ...payload } = form
      void confirmPassword
      await authApi.registerWithOtp(payload)
      // Tự động chuyển hướng hoặc đăng nhập
      window.location.href = '/'
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
        <p className="auth-intro">Nhập email để nhận mã xác nhận OTP 6 chữ số bảo mật.</p>
        
        {successMsg && <div className="form-notice success" role="alert">{successMsg}</div>}
        {error && <div className="form-notice error" role="alert">{error}</div>}

        <form className="auth-form" onSubmit={handleSubmit}>
          {/* Email input + Send OTP Button */}
          <div className="form-group-otp" style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', fontWeight: 600, marginBottom: '0.5rem' }}>
              Email xác nhận *
            </label>
            <div style={{ display: 'flex', gap: '8px' }}>
              <input
                autoComplete="email"
                name="email"
                required
                type="email"
                value={form.email}
                onChange={updateField}
                placeholder="ban@example.com"
                style={{ flex: 1 }}
              />
              <button
                type="button"
                className="button secondary"
                onClick={handleSendOtp}
                disabled={sendingOtp || countdown > 0}
                style={{ whiteSpace: 'nowrap', padding: '0 16px', fontSize: '0.85rem' }}
              >
                {sendingOtp
                  ? 'Đang gửi...'
                  : countdown > 0
                  ? `Gửi lại (${countdown}s)`
                  : otpSent
                  ? '✉️ Gửi lại mã'
                  : '✉️ Gửi mã xác nhận'}
              </button>
            </div>
            {fieldErrors.email && <small style={{ color: '#dc2626', fontSize: '0.8rem' }}>{fieldErrors.email}</small>}
          </div>

          {/* OTP Code input field */}
          {otpSent && (
            <label style={{ display: 'block', marginBottom: '1rem' }}>
              Mã xác thực OTP (6 chữ số) *
              <input
                name="otpCode"
                required
                maxLength="6"
                value={form.otpCode}
                onChange={updateField}
                placeholder="Nhập 6 chữ số từ email"
                style={{ letterSpacing: '4px', fontWeight: 'bold', textAlign: 'center', fontSize: '1.2rem', marginTop: '0.5rem' }}
              />
              {fieldErrors.otpCode && <small style={{ color: '#dc2626' }}>{fieldErrors.otpCode}</small>}
            </label>
          )}

          <div className="form-row">
            <label>Tên hiển thị
              <input autoComplete="name" name="displayName" required maxLength="150" value={form.displayName} onChange={updateField} placeholder="Minh Hào" />
              {fieldErrors.displayName && <small>{fieldErrors.displayName}</small>}
            </label>
            <label>Tên đăng nhập
              <input autoComplete="username" name="username" required minLength="3" maxLength="30" value={form.username} onChange={updateField} placeholder="minhhao" />
              {fieldErrors.username && <small>{fieldErrors.username}</small>}
            </label>
          </div>

          <div className="form-row">
            <label>Mật khẩu
              <input autoComplete="new-password" name="password" required minLength="6" maxLength="72" type="password" value={form.password} onChange={updateField} placeholder="Tối thiểu 6 ký tự" />
              {fieldErrors.password && <small>{fieldErrors.password}</small>}
            </label>
            <label>Nhập lại mật khẩu
              <input autoComplete="new-password" name="confirmPassword" required type="password" value={form.confirmPassword} onChange={updateField} placeholder="Nhập lại mật khẩu" />
              {fieldErrors.confirmPassword && <small>{fieldErrors.confirmPassword}</small>}
            </label>
          </div>

          <button className="button auth-submit" type="submit" disabled={submitting || !otpSent}>
            {submitting ? 'Đang tạo tài khoản…' : 'Tạo tài khoản'}
          </button>
        </form>
        <p className="auth-switch">Đã có tài khoản? <Link to="/dang-nhap">Đăng nhập →</Link></p>
      </div>
    </section>
  )
}
