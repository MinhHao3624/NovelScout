import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/authContext.js'

const navigation = [
  { to: '/', label: 'Trang chủ' },
  { to: '/tim-kiem', label: 'Khám phá' },
]

export default function PublicLayout() {
  const { user, loading, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/')
  }

  return (
    <div className="app-shell">
      <header className="site-header">
        <NavLink className="brand" to="/" aria-label="NovelScout - Trang chủ">
          <span className="brand-mark">N</span><span>NovelScout</span>
        </NavLink>
        <nav className="main-nav" aria-label="Điều hướng chính">
          {navigation.map((item) => <NavLink key={item.to} to={item.to} end={item.to === '/'}>{item.label}</NavLink>)}
        </nav>
        <div className="header-actions">
          {!loading && user ? (
            <>
              <NavLink className="profile-link" to="/ho-so">
                {user.avatarUrl
                  ? <img className="avatar-small avatar-image" src={user.avatarUrl} alt="" />
                  : <span className="avatar-small">{(user.displayName || user.username).charAt(0).toUpperCase()}</span>}
                <span>{user.displayName || user.username}</span>
              </NavLink>
              <button className="text-button" type="button" onClick={handleLogout}>Đăng xuất</button>
            </>
          ) : !loading ? (
            <>
              <NavLink className="text-link" to="/dang-nhap">Đăng nhập</NavLink>
              <NavLink className="button button-small" to="/dang-ky">Đăng ký</NavLink>
            </>
          ) : null}
        </div>
      </header>
      <main><Outlet /></main>
      <footer className="site-footer"><span>NovelScout</span><span>Đọc sâu hơn. Tìm đúng truyện hơn.</span></footer>
    </div>
  )
}
