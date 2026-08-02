import { NavLink, Outlet } from 'react-router-dom'

const navigation = [
  { to: '/', label: 'Trang chủ' },
  { to: '/tim-kiem', label: 'Khám phá' },
]

export default function PublicLayout() {
  return (
    <div className="app-shell">
      <header className="site-header">
        <NavLink className="brand" to="/" aria-label="NovelScout - Trang chủ">
          <span className="brand-mark">N</span>
          <span>NovelScout</span>
        </NavLink>

        <nav className="main-nav" aria-label="Điều hướng chính">
          {navigation.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === '/'}>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="header-actions">
          <NavLink className="text-link" to="/dang-nhap">Đăng nhập</NavLink>
          <NavLink className="button button-small" to="/dang-ky">Đăng ký</NavLink>
        </div>
      </header>

      <main>
        <Outlet />
      </main>

      <footer className="site-footer">
        <span>NovelScout</span>
        <span>Đọc sâu hơn. Tìm đúng truyện hơn.</span>
      </footer>
    </div>
  )
}
