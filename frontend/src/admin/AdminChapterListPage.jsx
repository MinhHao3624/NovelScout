import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getAdminChapters, createAdminChapter, updateAdminChapter, deleteAdminChapter } from '../api/admin';

export function AdminChapterListPage() {
  const { novelId } = useParams();
  const [chapters, setChapters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editChapter, setEditChapter] = useState(null); // null = closed, {} = create, {id...} = edit
  const [formData, setFormData] = useState({ chapterNumber: 1, title: '', content: '' });

  const fetchChapters = () => {
    setLoading(true);
    getAdminChapters(novelId)
      .then((data) => {
        setChapters(data || []);
        setLoading(false);
      })
      .catch((err) => {
        alert('Lỗi tải danh sách chương: ' + err.message);
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchChapters();
  }, [novelId]);

  const handleOpenCreate = () => {
    const nextNum = chapters.length > 0 ? Math.max(...chapters.map((c) => c.chapterNumber || 0)) + 1 : 1;
    setFormData({ chapterNumber: nextNum, title: `Chương ${nextNum}`, content: '' });
    setEditChapter({});
  };

  const handleOpenEdit = (ch) => {
    setFormData({
      chapterNumber: ch.chapterNumber || 1,
      title: ch.title || '',
      content: ch.content || '',
    });
    setEditChapter(ch);
  };

  const handleSaveChapter = async (e) => {
    e.preventDefault();
    try {
      if (editChapter.id) {
        await updateAdminChapter(editChapter.id, formData);
        alert('Đã cập nhật chương thành công!');
      } else {
        await createAdminChapter(novelId, formData);
        alert('Đã thêm chương mới thành công!');
      }
      setEditChapter(null);
      fetchChapters();
    } catch (err) {
      alert('Lỗi: ' + err.message);
    }
  };

  const handleDelete = async (ch) => {
    if (!window.confirm(`Bro có chắc muốn xóa "${ch.title}" không?`)) return;
    try {
      await deleteAdminChapter(ch.id);
      alert('Đã xóa chương.');
      fetchChapters();
    } catch (err) {
      alert('Lỗi xóa chương: ' + err.message);
    }
  };

  return (
    <div className="admin-chapters-page">
      <div className="page-header">
        <div>
          <Link to="/admin/novels" className="back-link">
            ◀ Quay lại kho truyện
          </Link>
          <h1 className="page-title">Quản Lý Danh Sách Chương</h1>
          <p className="page-subtitle">Thêm, sửa nội dung hoặc xóa các chương truyện</p>
        </div>
        <button className="admin-btn primary" onClick={handleOpenCreate}>
          ➕ Thêm Chương Mới
        </button>
      </div>

      {loading ? (
        <div className="admin-loading">Đang tải danh sách chương...</div>
      ) : chapters.length === 0 ? (
        <div className="admin-empty">Tác phẩm này chưa có chương nào. Hãy bấm "Thêm Chương Mới" để bắt đầu!</div>
      ) : (
        <div className="table-responsive">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Số Chương</th>
                <th>Tên Chương</th>
                <th>Nội Dung (Đoạn xem trước)</th>
                <th>Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              {chapters.map((ch) => (
                <tr key={ch.id}>
                  <td>{ch.id}</td>
                  <td><strong>Chương {ch.chapterNumber}</strong></td>
                  <td>{ch.title}</td>
                  <td className="content-snippet">
                    {ch.content ? ch.content.replace(/<[^>]*>?/gm, '').substring(0, 80) + '...' : '(Nội dung rỗng)'}
                  </td>
                  <td className="table-actions">
                    <button className="btn-action edit" onClick={() => handleOpenEdit(ch)}>
                      ✏️ Sửa nội dung
                    </button>
                    <button className="btn-action delete" onClick={() => handleDelete(ch)}>
                      🗑️ Xóa
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Chapter Edit/Create Modal */}
      {editChapter && (
        <div className="admin-modal-backdrop">
          <div className="admin-modal chapter-modal">
            <h2>{editChapter.id ? 'Chỉnh Sửa Chương' : 'Thêm Chương Mới'}</h2>
            <form onSubmit={handleSaveChapter}>
              <div className="form-group-inline">
                <div className="form-group short">
                  <label>Số thứ tự chương *</label>
                  <input
                    type="number"
                    required
                    value={formData.chapterNumber}
                    onChange={(e) => setFormData({ ...formData, chapterNumber: parseInt(e.target.value) || 1 })}
                  />
                </div>
                <div className="form-group flex-1">
                  <label>Tên chương *</label>
                  <input
                    type="text"
                    required
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Nội dung chương (Hỗ trợ HTML hoặc văn bản) *</label>
                <textarea
                  rows="12"
                  required
                  value={formData.content}
                  onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                  placeholder="Nhập nội dung chương truyện tại đây..."
                ></textarea>
              </div>

              <div className="modal-actions">
                <button type="button" className="admin-btn outline" onClick={() => setEditChapter(null)}>Hủy</button>
                <button type="submit" className="admin-btn primary">Lưu Chương</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminChapterListPage;
