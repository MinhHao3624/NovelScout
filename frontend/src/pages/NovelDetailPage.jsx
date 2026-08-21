import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { catalogApi } from '../api/catalog.js'

const statusLabels = { ONGOING: 'Đang ra', COMPLETED: 'Hoàn thành', HIATUS: 'Tạm dừng' }

export default function NovelDetailPage() {
  const { slug } = useParams()
  const [novel, setNovel] = useState(null)
  const [chapters, setChapters] = useState([])
  const [error, setError] = useState('')
  const [lastRead, setLastRead] = useState(null)

  useEffect(() => {
    // Read local reading history
    const historyData = localStorage.getItem(`novelscout_history_${slug}`)
    if (historyData) {
      try {
        setLastRead(JSON.parse(historyData))
      } catch (e) {
        console.error('Lỗi đọc lịch sử:', e)
      }
    }

    Promise.all([
      catalogApi.novel(slug),
      catalogApi.chapters(slug)
    ])
      .then(([novelData, chaptersData]) => {
        setNovel(novelData)
        setChapters(chaptersData)
      })
      .catch((requestError) => setError(requestError.message))
  }, [slug])

  if (error) {
    return (
      <section className="simple-page">
        <p className="eyebrow">Không tìm thấy</p>
        <h1>{error}</h1>
        <Link className="button" to="/">Về Trang chủ</Link>
      </section>
    )
  }

  if (!novel) return <div className="route-loader" aria-label="Đang tải" />

  const firstChapter = chapters.length > 0 ? chapters[0] : null

  return (
    <section className="novel-detail-page">
      <Link className="back-link" to="/tim-kiem">← Trở lại kho truyện</Link>
      <div className="novel-detail-hero">
        {novel.coverUrl ? (
          <img
            className="novel-detail-cover novel-cover-image"
            src={novel.coverUrl}
            alt={`Bìa ${novel.title}`}
            style={{ width: '220px', height: '320px', objectFit: 'cover', borderRadius: '12px' }}
          />
        ) : (
          <div className={`novel-detail-cover cover-tone-${(novel.id % 4) + 1}`}>
            <span>NovelScout Selection</span>
            <strong>{novel.title}</strong>
            <small>{novel.authorName}</small>
          </div>
        )}

        <div className="novel-detail-copy">
          <p className="eyebrow">{statusLabels[novel.status] || novel.status} · ★ {Number(novel.averageRating).toFixed(1)}</p>
          <h1>{novel.title}</h1>
          <p className="detail-author">bởi {novel.authorName}</p>
          <div className="category-list">
            {novel.categories.map((category) => (
              <span key={category.id}>{category.name}</span>
            ))}
          </div>
          <p className="detail-description">{novel.description}</p>
          {novel.sourceAttributionUrl && (
            <p className="source-attribution">
              Nguồn: <a href={novel.sourceAttributionUrl} target="_blank" rel="noreferrer">Wikisource tiếng Việt ↗</a> · {novel.sourceLicense === 'PUBLIC_DOMAIN' ? 'Phạm vi công cộng' : 'CC BY-SA'}
            </p>
          )}

          <div className="detail-actions" style={{ marginTop: '1.5rem', display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
            {firstChapter ? (
              <>
                <Link className="button" to={`/truyen/${slug}/chuong-${firstChapter.chapterNumber}`}>
                  📖 Đọc từ đầu
                </Link>
                {lastRead && (
                  <Link className="button secondary" to={`/truyen/${slug}/chuong-${lastRead.chapterNumber}`}>
                    🔖 Đọc tiếp Chương {lastRead.chapterNumber}
                  </Link>
                )}
              </>
            ) : (
              <button className="button" disabled>Chưa có chương nào</button>
            )}
            <span style={{ color: '#718096', fontSize: '0.9rem' }}>
              👁️ {novel.viewCount.toLocaleString('vi-VN')} lượt đọc · 📚 {chapters.length} chương
            </span>
          </div>
        </div>
      </div>

      {/* Chapters List Section */}
      <div className="novel-chapters-section" style={{ marginTop: '3rem', paddingTop: '2rem', borderTop: '1px solid #e2e8f0' }}>
        <h2 style={{ fontSize: '1.5rem', marginBottom: '1.5rem' }}>Danh sách chương ({chapters.length})</h2>
        {chapters.length === 0 ? (
          <p style={{ color: '#718096' }}>Truyện hiện chưa có chương nào được cập nhật.</p>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '0.75rem' }}>
            {chapters.map((ch) => (
              <Link
                key={ch.id}
                to={`/truyen/${slug}/chuong-${ch.chapterNumber}`}
                style={{
                  padding: '0.75rem 1rem',
                  borderRadius: '8px',
                  border: '1px solid #e2e8f0',
                  textDecoration: 'none',
                  color: '#2d3748',
                  backgroundColor: lastRead?.chapterNumber === ch.chapterNumber ? '#edf2f7' : '#ffffff',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  transition: 'all 0.2s ease'
                }}
              >
                <span style={{ fontWeight: 500, fontSize: '0.95rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  Chương {ch.chapterNumber}: {ch.title}
                </span>
                {lastRead?.chapterNumber === ch.chapterNumber && (
                  <span style={{ fontSize: '0.75rem', backgroundColor: '#3182ce', color: '#fff', padding: '0.1rem 0.4rem', borderRadius: '4px' }}>Đang đọc</span>
                )}
              </Link>
            ))}
          </div>
        )}
      </div>
    </section>
  )
}
