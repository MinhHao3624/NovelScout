import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/authContext.js'

export default function ProfilePage() {
  const { user, updateProfile, changePassword } = useAuth()
  const navigate = useNavigate()
  const [profile, setProfile] = useState({ displayName: user.displayName ?? '', avatarUrl: user.avatarUrl ?? '' })
  const [passwords, setPasswords] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [profileStatus, setProfileStatus] = useState(null)
  const [passwordStatus, setPasswordStatus] = useState(null)

  async function saveProfile(event) {
    event.preventDefault()
    setProfileStatus(null)
    try {
      await updateProfile(profile)
      setProfileStatus({ type: 'success', message: 'Đã lưu thay đổi hồ sơ.' })
    } catch (error) {
      setProfileStatus({ type: 'error', message: error.message })
    }
  }

  async function savePassword(event) {
    event.preventDefault()
    setPasswordStatus(null)
    if (passwords.newPassword !== passwords.confirmPassword) {
      setPasswordStatus({ type: 'error', message: 'Mật khẩu xác nhận chưa trùng khớp.' })
      return
    }
    try {
      await changePassword({ currentPassword: passwords.currentPassword, newPassword: passwords.newPassword })
      navigate('/dang-nhap', { replace: true, state: { passwordChanged: true } })
    } catch (error) {
      setPasswordStatus({ type: 'error', message: error.message })
    }
  }

  return (
    <section className="profile-page">
      <header className="profile-heading">
        {user.avatarUrl
          ? <img className="profile-avatar profile-avatar-image" src={user.avatarUrl} alt="Ảnh đại diện" />
          : <div className="profile-avatar">{(user.displayName || user.username).charAt(0).toUpperCase()}</div>}
        <div><p className="eyebrow">Hồ sơ độc giả</p><h1>{user.displayName || user.username}</h1><p>@{user.username} · {user.roles.join(' / ')}</p></div>
      </header>
      <div className="profile-grid">
        <article className="settings-card">
          <span className="card-number">01</span><h2>Thông tin cá nhân</h2><p>Tên hiển thị và ảnh đại diện xuất hiện trong không gian đọc của bạn.</p>
          {profileStatus && <div className={`form-notice ${profileStatus.type}`}>{profileStatus.message}</div>}
          <form className="auth-form" onSubmit={saveProfile}>
            <label>Tên hiển thị<input required maxLength="150" value={profile.displayName} onChange={(e) => setProfile({ ...profile, displayName: e.target.value })} /></label>
            <label>Địa chỉ ảnh đại diện<input type="url" maxLength="500" value={profile.avatarUrl} onChange={(e) => setProfile({ ...profile, avatarUrl: e.target.value })} placeholder="https://..." /></label>
            <label>Email<input disabled value={user.email} /><small>Email hiện chưa thể thay đổi.</small></label>
            <button className="button button-small" type="submit">Lưu hồ sơ</button>
          </form>
        </article>
        <article className="settings-card">
          <span className="card-number">02</span><h2>Đổi mật khẩu</h2><p>Sau khi đổi mật khẩu, bạn sẽ được đăng xuất khỏi phiên hiện tại.</p>
          {passwordStatus && <div className={`form-notice ${passwordStatus.type}`}>{passwordStatus.message}</div>}
          <form className="auth-form" onSubmit={savePassword}>
            <label>Mật khẩu hiện tại<input autoComplete="current-password" required type="password" value={passwords.currentPassword} onChange={(e) => setPasswords({ ...passwords, currentPassword: e.target.value })} /></label>
            <label>Mật khẩu mới<input autoComplete="new-password" required minLength="8" maxLength="72" type="password" value={passwords.newPassword} onChange={(e) => setPasswords({ ...passwords, newPassword: e.target.value })} /></label>
            <label>Nhập lại mật khẩu mới<input autoComplete="new-password" required type="password" value={passwords.confirmPassword} onChange={(e) => setPasswords({ ...passwords, confirmPassword: e.target.value })} /></label>
            <button className="button button-small" type="submit">Đổi mật khẩu</button>
          </form>
        </article>
      </div>
    </section>
  )
}
