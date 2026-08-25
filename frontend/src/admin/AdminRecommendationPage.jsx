import React, { useEffect, useState } from 'react';
import {
  getAdminRecommendationConfig,
  updateAdminRecommendationConfig,
  getAdminRecommendationMetrics,
  recalculateRecommendationMatrix,
  simulateRecommendationForUser,
} from '../api/admin.js';
import NovelCover from '../components/NovelCover.jsx';

export function AdminRecommendationPage() {
  const [config, setConfig] = useState({ contentWeight: 0.45, collaborativeWeight: 0.45, popularityWeight: 0.10 });
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState('');

  // Simulator State
  const [simUserId, setSimUserId] = useState('2'); // Default reader1 or reader2
  const [simResult, setSimResult] = useState(null);
  const [simLoading, setSimLoading] = useState(false);

  useEffect(() => {
    Promise.all([getAdminRecommendationConfig(), getAdminRecommendationMetrics()])
      .then(([configData, metricsData]) => {
        setConfig(configData);
        setMetrics(metricsData);
        setLoading(false);
      })
      .catch((err) => {
        alert('Lỗi tải cấu hình gợi ý: ' + err.message);
        setLoading(false);
      });
  }, []);

  const handleSaveConfig = async (e) => {
    e.preventDefault();
    const sum = Number(config.contentWeight) + Number(config.collaborativeWeight) + Number(config.popularityWeight);
    if (Math.abs(sum - 1.0) > 0.05) {
      alert(`Tổng 3 trọng số phải bằng 1.0 (100%). Hiện tại sum = ${sum.toFixed(2)}`);
      return;
    }

    setSaving(true);
    try {
      const updated = await updateAdminRecommendationConfig(config);
      setConfig(updated);
      setMsg('✅ Đã lưu trọng số mới thành công! Thuật toán đã áp dụng cấu hình này.');
      setTimeout(() => setMsg(''), 4000);
    } catch (err) {
      alert('Lỗi lưu cấu hình: ' + err.message);
    } finally {
      setSaving(false);
    }
  };

  const handleRecalculate = async () => {
    try {
      const res = await recalculateRecommendationMatrix();
      alert(res.message || 'Đã làm mới ma trận tương đồng.');
    } catch (err) {
      alert('Lỗi làm mới ma trận: ' + err.message);
    }
  };

  const handleSimulate = async (e) => {
    if (e) e.preventDefault();
    if (!simUserId) return;
    setSimLoading(true);
    try {
      const res = await simulateRecommendationForUser(simUserId, 10);
      setSimResult(res);
    } catch (err) {
      alert('Lỗi mô phỏng cho độc giả ID=' + simUserId + ': ' + err.message);
    } finally {
      setSimLoading(false);
    }
  };

  if (loading) return <div className="admin-loading">Đang tải thông số ma trận gợi ý...</div>;

  const totalWeightSum = (Number(config.contentWeight) + Number(config.collaborativeWeight) + Number(config.popularityWeight)).toFixed(2);

  return (
    <div className="admin-recommendation-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Quản Lý & Điều Chỉnh Thuật Toán Gợi Ý Lai</h1>
          <p className="page-subtitle">Cấu hình trọng số Hybrid Filtering theo thời gian thực và mô phỏng ma trận độc giả</p>
        </div>
        <button className="admin-btn outline" onClick={handleRecalculate}>
          🔄 Tính Toán Lại Ma Trận
        </button>
      </div>

      {/* Metrics Cards */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
        <div className="stat-card">
          <div className="stat-icon novels-icon">📊</div>
          <div className="stat-info">
            <span className="stat-value">{metrics?.totalInteractions || 0}</span>
            <span className="stat-label">Tổng tương tác độc giả</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon views-icon">🌫️</div>
          <div className="stat-info">
            <span className="stat-value">{metrics?.matrixSparsityPercentage || 95.0}%</span>
            <span className="stat-label">Độ thưa ma trận (Sparsity)</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon users-icon">👥</div>
          <div className="stat-info">
            <span className="stat-value">{metrics?.activeReadersCount || 0}</span>
            <span className="stat-label">Độc giả có tương tác</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon chapters-icon">📚</div>
          <div className="stat-info">
            <span className="stat-value">{metrics?.totalNovels || 0}</span>
            <span className="stat-label">Tác phẩm trong ma trận</span>
          </div>
        </div>
      </div>

      {/* Dynamic Weight Tuning Form */}
      <div className="admin-card" style={{ background: '#fff', padding: '24px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '32px' }}>
        <h2 style={{ fontSize: '1.25rem', marginTop: 0, marginBottom: '8px' }}>⚙️ Điều Chỉnh Trọng Số Thuật Toán Động (Real-time Tuning)</h2>
        <p style={{ color: '#64748b', fontSize: '0.875rem', marginBottom: '20px' }}>
          Thay đổi tỉ lệ trọng số của 3 thuật toán thành phần. Tổng trọng số phải luôn bằng <strong>1.00 (100%)</strong>.
        </p>

        {msg && <div style={{ background: '#ecfdf5', color: '#047857', padding: '12px 16px', borderRadius: '8px', marginBottom: '20px', fontWeight: 600 }}>{msg}</div>}

        <form onSubmit={handleSaveConfig}>
          <div className="weight-sliders-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px', marginBottom: '20px' }}>
            {/* CBF Weight Slider */}
            <div className="form-group">
              <label>1. Content-Based Weight (CBF): <strong>{Number(config.contentWeight).toFixed(2)}</strong> ({(Number(config.contentWeight) * 100).toFixed(0)}%)</label>
              <input
                type="range"
                min="0.0"
                max="1.0"
                step="0.05"
                value={config.contentWeight}
                onChange={(e) => setConfig({ ...config, contentWeight: parseFloat(e.target.value) })}
              />
              <small style={{ color: '#94a3b8' }}>Dựa trên thể loại & tác giả tương đồng</small>
            </div>

            {/* CF Weight Slider */}
            <div className="form-group">
              <label>2. Collaborative Filtering Weight (CF): <strong>{Number(config.collaborativeWeight).toFixed(2)}</strong> ({(Number(config.collaborativeWeight) * 100).toFixed(0)}%)</label>
              <input
                type="range"
                min="0.0"
                max="1.0"
                step="0.05"
                value={config.collaborativeWeight}
                onChange={(e) => setConfig({ ...config, collaborativeWeight: parseFloat(e.target.value) })}
              />
              <small style={{ color: '#94a3b8' }}>Dựa trên gu đọc của độc giả có cùng sở thích</small>
            </div>

            {/* Popularity Weight Slider */}
            <div className="form-group">
              <label>3. Popularity Weight (Pop): <strong>{Number(config.popularityWeight).toFixed(2)}</strong> ({(Number(config.popularityWeight) * 100).toFixed(0)}%)</label>
              <input
                type="range"
                min="0.0"
                max="1.0"
                step="0.05"
                value={config.popularityWeight}
                onChange={(e) => setConfig({ ...config, popularityWeight: parseFloat(e.target.value) })}
              />
              <small style={{ color: '#94a3b8' }}>Dựa trên lượt đọc và rating trung bình toàn sàn</small>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderTop: '1px solid #f1f5f9', paddingTop: '16px' }}>
            <span style={{ fontSize: '0.9rem', fontWeight: 600, color: totalWeightSum === '1.00' ? '#059669' : '#dc2626' }}>
              Tổng trọng số: {totalWeightSum} / 1.00 {totalWeightSum === '1.00' ? '✓ (Hợp lệ)' : '⚠️ (Cần điều chỉnh về 1.00)'}
            </span>
            <button type="submit" className="admin-btn primary" disabled={saving}>
              {saving ? 'Đang lưu...' : '💾 Lưu Trọng Số Mới'}
            </button>
          </div>
        </form>
      </div>

      {/* User Recommendation Matrix Simulator */}
      <div className="admin-card" style={{ background: '#fff', padding: '24px', borderRadius: '12px', border: '1px solid #e2e8f0' }}>
        <h2 style={{ fontSize: '1.25rem', marginTop: 0, marginBottom: '8px' }}>🧪 Công Cụ Mô Phỏng Gợi Ý Theo Độc Giả (User Matrix Debugger)</h2>
        <p style={{ color: '#64748b', fontSize: '0.875rem', marginBottom: '20px' }}>
          Chọn hoặc nhập ID độc giả (VD: ID 2 = `reader1`, ID 3 = `reader2`) để xem trước điểm ma trận chi tiết.
        </p>

        <form onSubmit={handleSimulate} style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
          <input
            type="number"
            placeholder="Nhập User ID (VD: 2)"
            value={simUserId}
            onChange={(e) => setSimUserId(e.target.value)}
            style={{ width: '180px', padding: '10px 14px', borderRadius: '8px', border: '1px solid #cbd5e1' }}
          />
          <button type="submit" className="admin-btn primary" disabled={simLoading}>
            {simLoading ? 'Đang mô phỏng...' : '🔍 Mô Phỏng Gợi Ý'}
          </button>
        </form>

        {simResult && (
          <div>
            <div style={{ marginBottom: '16px', background: '#f8fafc', padding: '12px 16px', borderRadius: '8px', fontWeight: 600 }}>
              Kết quả mô phỏng cho: <span style={{ color: '#059669' }}>{simResult.displayName} (@{simResult.username})</span> - User ID: {simResult.userId}
            </div>

            <div className="table-responsive">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Bìa</th>
                    <th>Tên Tác Phẩm</th>
                    <th>Tác Giả</th>
                    <th>Điểm CBF</th>
                    <th>Điểm CF</th>
                    <th>Điểm Pop</th>
                    <th>Tổng Điểm Hybrid</th>
                    <th>% Phù hợp</th>
                  </tr>
                </thead>
                <tbody>
                  {simResult.recommendations.map((item) => (
                    <tr key={item.novelId}>
                      <td>
                        <div style={{ width: '36px', height: '50px' }}>
                          <NovelCover novel={{ id: item.novelId, title: item.title, coverUrl: item.coverUrl, authorName: item.authorName }} />
                        </div>
                      </td>
                      <td>
                        <strong>{item.title}</strong>
                        <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>{item.slug}</div>
                      </td>
                      <td>{item.authorName}</td>
                      <td><span style={{ background: '#eff6ff', color: '#1d4ed8', padding: '2px 8px', borderRadius: '4px', fontWeight: 600 }}>{item.cbfScore}</span></td>
                      <td><span style={{ background: '#fef3c7', color: '#b45309', padding: '2px 8px', borderRadius: '4px', fontWeight: 600 }}>{item.cfScore}</span></td>
                      <td><span style={{ background: '#f3e8ff', color: '#6b21a8', padding: '2px 8px', borderRadius: '4px', fontWeight: 600 }}>{item.popScore}</span></td>
                      <td><strong style={{ color: '#059669', fontSize: '1.05rem' }}>{item.hybridScore}</strong></td>
                      <td><span className="match-badge">⚡ {item.matchPercentage}%</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default AdminRecommendationPage;
