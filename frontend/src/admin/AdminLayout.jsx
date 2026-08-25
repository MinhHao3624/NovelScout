import React from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authContext.js';

import './Admin.css';

export function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="admin-container">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <Link to="/admin" className="admin-logo">
            <span className="logo-icon">⚡</span> NovelScout <span className="admin-badge">ADMIN</span>
          </Link>
        </div>

        <nav className="admin-nav">
          <NavLink to="/admin" end className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
            <span className="nav-icon">📊</span> Tổng quan Dashboard
          </NavLink>
          <NavLink to="/admin/novels" className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
            <span className="nav-icon">📚</span> Quản lý Kho truyện & Bìa
          </NavLink>
          <Link to="/" className="nav-item reader-link">
            <span className="nav-icon">📖</span> Về giao diện Độc giả
          </Link>
        </nav>

        <div className="admin-sidebar-footer">
          <div className="admin-user-info">
            <div className="admin-avatar">{user?.displayName?.[0] || 'A'}</div>
            <div className="admin-user-details">
              <span className="user-name">{user?.displayName || 'Admin'}</span>
              <span className="user-role">Quản trị viên</span>
            </div>
          </div>
          <button className="admin-logout-btn" onClick={handleLogout} title="Đăng xuất">
            🚪
          </button>
        </div>
      </aside>

      <main className="admin-main">
        <header className="admin-header">
          <div className="header-title">Hệ Thống Quản Trị NovelScout</div>
          <div className="header-actions">
            <span className="current-date">{new Date().toLocaleDateString('vi-VN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</span>
          </div>
        </header>

        <div className="admin-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
}

export default AdminLayout;
