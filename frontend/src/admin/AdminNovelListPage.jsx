import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAdminNovels, createAdminNovel, updateAdminNovel, uploadAdminNovelCover, deleteAdminNovel } from '../api/admin';
import NovelCover from '../components/NovelCover';

export function AdminNovelListPage() {
  const [novels, setNovels] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  // Modals state
  const [editNovel, setEditNovel] = useState(null); // null = modal closed, {} = create new, {id...} = edit
  const [coverModalNovel, setCoverModalNovel] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);
  const [customUrl, setCustomUrl] = useState('');
  const [uploading, setUploading] = useState(false);

  // Novel Form State
  const [formData, setFormData] = useState({ title: '', authorName: '', description: '', coverUrl: '', status: 'COMPLETED' });

  const fetchNovels = (p = page, q = search) => {
    setLoading(true);
    getAdminNovels(q, p, 15)
      .then((data) => {
        setNovels(data.content || []);
        setTotalPages(data.totalPages || 1);
        setLoading(false);
      })
      .catch((err) => {
        alert('Lỗi tải danh sách truyện: ' + err.message);
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchNovels(page, search);
  }, [page]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchNovels(0, search);
  };

  const handleOpenCreate = () => {
    setFormData({ title: '', authorName: '', description: '', coverUrl: '', status: 'COMPLETED' });
    setEditNovel({});
  };

  const handleOpenEdit = (novel) => {
    setFormData({
      title: novel.title || '',
      authorName: novel.authorName || '',
      description: novel.description || '',
      coverUrl: novel.coverUrl || '',
      status: novel.status || 'COMPLETED',
    });
    setEditNovel(novel);
  };

  const handleSaveNovel = async (e) => {
    e.preventDefault();
    try {
      if (editNovel.id) {
        await updateAdminNovel(editNovel.id, formData);
        alert('Đã cập nhật tác phẩm thành công!');
      } else {
        await createAdminNovel(formData);
        alert('Đã tạo tác phẩm mới thành công!');
      }
      setEditNovel(null);
      fetchNovels(page, search);
    } catch (err) {
      alert('Lỗi: ' + err.message);
    }
  };

  const handleOpenCoverModal = (novel) => {
    setCoverModalNovel(novel);
    setCustomUrl(novel.coverUrl || '');
    setSelectedFile(null);
  };

  const handleUploadCoverSubmit = async (e) => {
    e.preventDefault();
    if (!coverModalNovel) return;
    setUploading(true);

    try {
      if (selectedFile) {
        // Upload file image directly
        await uploadAdminNovelCover(coverModalNovel.id, selectedFile);
        alert('Đã tải ảnh bìa lên thành công!');
      } else if (customUrl !== coverModalNovel.coverUrl) {
        // Update URL directly
        await updateAdminNovel(coverModalNovel.id, {
          title: coverModalNovel.title,
          authorName: coverModalNovel.authorName,
          description: coverModalNovel.description,
          coverUrl: customUrl,
          status: coverModalNovel.status,
        });
        alert('Đã cập nhật đường dẫn ảnh bìa thành công!');
      }
      setCoverModalNovel(null);
      fetchNovels(page, search);
    } catch (err) {
      alert('Lỗi cập nhật ảnh bìa: ' + err.message);
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (novel) => {
    if (!window.confirm(`Bro có chắc muốn xóa tác phẩm "${novel.title}" không?`)) return;
    try {
      await deleteAdminNovel(novel.id);
      alert('Đã xóa tác phẩm.');
      fetchNovels(page, search);
    } catch (err) {
      alert('Lỗi xóa: ' + err.message);
    }
  };

  return (
    <div className="admin-novels-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Quản Lý Kho Truyện & Ảnh Bìa</h1>
          <p className="page-subtitle">Thêm mới, sửa thông tin, tải ảnh bìa từ máy tính hoặc quản lý chương</p>
        </div>
        <button className="admin-btn primary" onClick={handleOpenCreate}>
          ➕ Thêm Truyện Mới
        </button>
      </div>

      <form className="admin-search-form" onSubmit={handleSearchSubmit}>
        <input
          type="text"
          placeholder="Tìm kiếm tên tác phẩm..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="admin-search-input"
        />
        <button type="submit" className="admin-btn outline">🔍 Tìm kiếm</button>
      </form>

      {loading ? (
        <div className="admin-loading">Đang tải danh sách kho truyện...</div>
      ) : (
        <div className="table-responsive">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Ảnh Bìa</th>
                <th>Tên Tác Phẩm</th>
                <th>Tác Giả</th>
                <th>Lượt Đọc</th>
                <th>Đánh Giá</th>
                <th>Thao Tác Quản Lý</th>
              </tr>
            </thead>
            <tbody>
              {novels.map((novel) => (
                <tr key={novel.id}>
                  <td>{novel.id}</td>
                  <td>
                    <div className="table-cover-preview" onClick={() => handleOpenCoverModal(novel)} title="Bấm để đổi ảnh bìa">
                      <NovelCover novel={novel} />
                      <span className="cover-edit-overlay">📷 Sửa</span>
                    </div>
                  </td>
                  <td>
                    <strong className="novel-table-title">{novel.title}</strong>
                    <div className="novel-table-slug">{novel.slug}</div>
                  </td>
                  <td>{novel.authorName}</td>
                  <td>{novel.viewCount?.toLocaleString('vi-VN') || 0}</td>
                  <td>⭐ {novel.averageRating || '4.0'} ({novel.ratingCount || 0})</td>
                  <td className="table-actions">
                    <button className="btn-action edit" onClick={() => handleOpenEdit(novel)} title="Sửa thông tin">
                      ✏️ Sửa
                    </button>
                    <button className="btn-action cover" onClick={() => handleOpenCoverModal(novel)} title="Thay đổi ảnh bìa">
                      🖼️ Đổi bìa
                    </button>
                    <Link to={`/admin/novels/${novel.id}/chapters`} className="btn-action chapters" title="Quản lý danh sách chương">
                      📖 Chương
                    </Link>
                    <button className="btn-action delete" onClick={() => handleDelete(novel)} title="Xóa tác phẩm">
                      🗑️ Xóa
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      <div className="admin-pagination">
        <button disabled={page === 0} onClick={() => setPage(page - 1)} className="admin-btn outline">
          ◀ Trang trước
        </button>
        <span className="page-info">Trang {page + 1} / {totalPages}</span>
        <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)} className="admin-btn outline">
          Trang sau ▶
        </button>
      </div>

      {/* Novel Edit/Create Modal */}
      {editNovel && (
        <div className="admin-modal-backdrop">
          <div className="admin-modal">
            <h2>{editNovel.id ? 'Chỉnh Sửa Tác Phẩm' : 'Thêm Tác Phẩm Mới'}</h2>
            <form onSubmit={handleSaveNovel}>
              <div className="form-group">
                <label>Tên tác phẩm *</label>
                <input
                  type="text"
                  required
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Tác giả</label>
                <input
                  type="text"
                  value={formData.authorName}
                  onChange={(e) => setFormData({ ...formData, authorName: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Mô tả tác phẩm</label>
                <textarea
                  rows="4"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                ></textarea>
              </div>

              <div className="form-group">
                <label>Đường dẫn URL ảnh bìa (hoặc để trống)</label>
                <input
                  type="text"
                  placeholder="/covers/ten-truyen.png"
                  value={formData.coverUrl}
                  onChange={(e) => setFormData({ ...formData, coverUrl: e.target.value })}
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="admin-btn outline" onClick={() => setEditNovel(null)}>Hủy</button>
                <button type="submit" className="admin-btn primary">Lưu Tác Phẩm</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Cover Image Upload Modal */}
      {coverModalNovel && (
        <div className="admin-modal-backdrop">
          <div className="admin-modal cover-modal">
            <h2>🖼️ Quản Lý Ảnh Bìa: {coverModalNovel.title}</h2>
            <form onSubmit={handleUploadCoverSubmit}>
              <div className="cover-modal-body">
                <div className="current-preview">
                  <label>Ảnh bìa hiện tại:</label>
                  <div className="preview-box">
                    <NovelCover novel={coverModalNovel} />
                  </div>
                </div>

                <div className="upload-options">
                  <div className="form-group">
                    <label>Cách 1: Chọn file ảnh từ máy tính (PNG/JPG/WEBP)</label>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={(e) => setSelectedFile(e.target.files[0])}
                    />
                    {selectedFile && <div className="file-name-tag">Đã chọn: {selectedFile.name}</div>}
                  </div>

                  <div className="divider-line"><span>HOẶC</span></div>

                  <div className="form-group">
                    <label>Cách 2: Nhập đường dẫn URL ảnh bìa trực tiếp</label>
                    <input
                      type="text"
                      placeholder="VD: /covers/leu-chong.png hoặc URL web"
                      value={customUrl}
                      onChange={(e) => setCustomUrl(e.target.value)}
                    />
                  </div>
                </div>
              </div>

              <div className="modal-actions">
                <button type="button" className="admin-btn outline" onClick={() => setCoverModalNovel(null)} disabled={uploading}>
                  Hủy
                </button>
                <button type="submit" className="admin-btn primary" disabled={uploading}>
                  {uploading ? 'Đang tải lên...' : 'Cập Nhật Ảnh Bìa'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminNovelListPage;
