import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAdminStats } from '../api/admin';

export function AdminDashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getAdminStats()
      .then((data) => {
        setStats(data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message || 'Khôn thể tải chỉ số thống kê');
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <div className="admin-loading">Đang tải chỉ số thống kê hệ thống...</div>;
  }

  if (error) {
    return <div className="admin-error">Lỗi: {error}</div>;
  }

  return (
    <div className="admin-dashboard-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Tổng quan hệ thống</h1>
          <p className="page-subtitle">Thống kê chỉ số hoạt động toàn bộ kho truyện và tương tác độc giả</p>
        </div>
        <div className="action-buttons">
          <Link to="/admin/novels" className="admin-btn primary">
            📚 Quản lý kho truyện
          </Link>
        </div>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon novels-icon">📚</div>
          <div className="stat-info">
            <span className="stat-value">{stats?.totalNovels || 0}</span>
            <span className="stat-label">Tổng số tác phẩm</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon chapters-icon">📖</div>
          <div className="stat-info">
            <span className="stat-value">{stats?.totalChapters || 0}</span>
            <span className="stat-label">Tổng số chương truyện</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon views-icon">👁️</div>
          <div className="stat-info">
            <span className="stat-value">{stats?.totalViews?.toLocaleString('vi-VN') || 0}</span>
            <span className="stat-label">Lượt đọc hệ thống</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon ratings-icon">⭐</div>
          <div className="stat-info">
            <span className="stat-value">{stats?.averageRating || '0.0'} / 5.0</span>
            <span className="stat-label">Đánh giá trung bình ({stats?.totalRatings || 0} lượt)</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon favorites-icon">❤️</div>
          <div className="stat-info">
            <span className="stat-value">{stats?.totalFavorites || 0}</span>
            <span className="stat-label">Lượt lưu Tủ sách</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon users-icon">👥</div>
          <div className="stat-info">
            <span className="stat-value">{stats?.totalUsers || 0}</span>
            <span className="stat-label">Độc giả đăng ký</span>
          </div>
        </div>
      </div>

    </div>
  );
}


export default AdminDashboardPage;
