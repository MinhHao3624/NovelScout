import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getPersonalizedRecommendations } from '../api/recommendation.js';
import NovelCover from '../components/NovelCover.jsx';

export function RecommendationPage() {
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getPersonalizedRecommendations(12)
      .then((data) => {
        setRecommendations(data || []);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message || 'Không thể tải danh sách gợi ý');
        setLoading(false);
      });
  }, []);

  return (
    <div className="page-shell recommendation-page">
      <header className="page-intro">
        <span className="eyebrow">Mô-đun Gợi Ý Nội Dung Lai (Hybrid Filtering)</span>
        <h1>Gợi Ý Dành Riêng Cho Bạn</h1>
        <p className="intro-text">
          Hệ thống kết hợp thuật toán <strong>Lọc dựa trên Nội dung (Content-Based)</strong> và <strong>Lọc Cộng Tác (Collaborative Filtering)</strong> từ lịch sử tương tác độc giả để tìm ra những tác phẩm phù hợp nhất với gu của bạn.
        </p>
      </header>

      {loading ? (
        <div className="empty-state">Đang phân tích ma trận gu đọc và tính toán gợi ý...</div>
      ) : error ? (
        <div className="form-notice error">{error}</div>
      ) : recommendations.length === 0 ? (
        <div className="empty-state">Chưa có tác phẩm gợi ý phù hợp. Hãy đọc và đánh giá thêm các tác phẩm nhé!</div>
      ) : (
        <div className="recommendation-grid">
          {recommendations.map((rec) => (
            <div key={rec.novel.id} className="rec-card">
              <div className="rec-badge-overlay">
                <span className="match-badge">⚡ {rec.matchPercentage}% Phù hợp</span>
              </div>
              <Link to={`/truyen/${rec.novel.slug}`} className="rec-cover-link">
                <NovelCover novel={rec.novel} />
              </Link>
              <div className="rec-card-body">
                <div className="rec-reason-tag">
                  💡 {rec.reason}
                </div>
                <Link to={`/truyen/${rec.novel.slug}`} className="rec-title">
                  {rec.novel.title}
                </Link>
                <div className="rec-author">{rec.novel.authorName}</div>
                <div className="rec-meta">
                  <span>★ {Number(rec.novel.averageRating || 4.0).toFixed(1)}</span>
                  <span>👁️ {rec.novel.viewCount?.toLocaleString('vi-VN') || 0}</span>
                </div>
                <div className="category-list">
                  {rec.novel.categories?.slice(0, 2).map((cat) => (
                    <span key={cat.id}>{cat.name}</span>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default RecommendationPage;
