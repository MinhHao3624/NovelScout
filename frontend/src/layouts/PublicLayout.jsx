import { useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/authContext.js'

export default function PublicLayout() {
  const { user, loading, logout } = useAuth()
  const [headerQuery, setHeaderQuery] = useState('')
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/')
  }

  function search(event) {
    event.preventDefault()
    const value = headerQuery.trim()
    navigate(value ? `/?query=${encodeURIComponent(value)}` : '/')
    setTimeout(() => document.getElementById('kho-truyen')?.scrollIntoView({ behavior: 'smooth' }), 0)
  }

  return (
    <div className="app-shell">
      <header className="site-header">
        <NavLink className="brand" to="/" aria-label="NovelScout - Trang chủ"><span className="brand-mark">N</span><span>NovelScout</span></NavLink>
        <nav className="main-nav" aria-label="Điều hướng chính">
          <NavLink to="/" end>Trang chủ</NavLink>
          <Link to="/#the-loai">Thể loại</Link>
          <NavLink to="/tu-sach">Tủ sách</NavLink>
          <NavLink to="/goi-y">Gợi ý</NavLink>
        </nav>
        <form className="header-search" onSubmit={search}>
          <input value={headerQuery} onChange={(event) => setHeaderQuery(event.target.value)} placeholder="Tìm truyện…" aria-label="Tìm truyện" />
          <button type="submit" aria-label="Bắt đầu tìm kiếm">⌕</button>
        </form>
        <div className="header-actions">
          {!loading && user ? (
            <>
              <NavLink className="profile-link" to="/ho-so">
                {user.avatarUrl ? <img className="avatar-small avatar-image" src={user.avatarUrl} alt="" /> : <span className="avatar-small">{(user.displayName || user.username).charAt(0).toUpperCase()}</span>}
                <span>{user.displayName || user.username}</span>
              </NavLink>
              <button className="text-button" type="button" onClick={handleLogout}>Đăng xuất</button>
            </>
          ) : !loading ? <><NavLink className="text-link" to="/dang-nhap">Đăng nhập</NavLink><NavLink className="button button-small" to="/dang-ky">Đăng ký</NavLink></> : null}
        </div>
      </header>
      <main><Outlet /></main>
      <footer className="site-footer"><span>NovelScout</span><span>Đọc sâu hơn. Tìm đúng truyện hơn.</span></footer>
    </div>
  )
}
