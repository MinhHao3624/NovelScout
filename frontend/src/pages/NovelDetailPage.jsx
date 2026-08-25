import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { catalogApi, getNovelCoverUrl } from '../api/catalog.js'
import { interactionApi } from '../api/interaction.js'
import { getSimilarNovels } from '../api/recommendation.js'
import NovelCover from '../components/NovelCover.jsx'

const statusLabels = { ONGOING: 'Đang ra', COMPLETED: 'Hoàn thành', HIATUS: 'Tạm dừng' }

export default function NovelDetailPage() {
  const { slug } = useParams()
  const [novel, setNovel] = useState(null)
  const [chapters, setChapters] = useState([])
  const [error, setError] = useState('')
  const [lastRead, setLastRead] = useState(null)
  const [imgError, setImgError] = useState(false)

  // Interactions State
  const [interactionStatus, setInteractionStatus] = useState({ isFavorite: false, favoriteCount: 0, userRating: null, userReview: '' })
  const [ratingsList, setRatingsList] = useState([])
  const [selectedScore, setSelectedScore] = useState(5)
  const [hoverScore, setHoverScore] = useState(0)
  const [reviewInput, setReviewInput] = useState('')
  const [isSubmittingRating, setIsSubmittingRating] = useState(false)
  const [ratingSuccessMsg, setRatingSuccessMsg] = useState('')

  // Similar Novels State
  const [similarNovels, setSimilarNovels] = useState([])

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

    catalogApi.novel(slug)
      .then((novelData) => {
        setNovel(novelData)
        catalogApi.chapters(slug).then(setChapters).catch((err) => console.error('Lỗi tải chương:', err))
        interactionApi.getStatus(slug)
          .then((status) => {
            if (status) {
              setInteractionStatus(status)
              if (status.userRating) {
                setSelectedScore(status.userRating)
              }
              if (status.userReview) {
                setReviewInput(status.userReview)
              }
            }
          })
          .catch((err) => console.error('Lỗi trạng thái tương tác:', err))

        interactionApi.getRatings(slug)
          .then((ratings) => setRatingsList(ratings || []))
          .catch((err) => console.error('Lỗi danh sách đánh giá:', err))
      })
      .catch((requestError) => setError(requestError.message))

    // Fetch Similar Novels (Content-Based Filtering)
    getSimilarNovels(slug, 6)
      .then((data) => setSimilarNovels(data || []))
      .catch((err) => console.error('Lỗi tải truyện tương tự:', err))
  }, [slug])

  const handleToggleFavorite = async () => {
    try {
      const res = await interactionApi.toggleFavorite(slug)
      setInteractionStatus((prev) => ({
        ...prev,
        isFavorite: res.isFavorite,
        favoriteCount: res.favoriteCount,
      }))
    } catch (err) {
      alert(err.message || 'Vui lòng đăng nhập để lưu tủ sách!')
    }
  }

  const handleSubmitRating = async (e) => {
    e.preventDefault()
    setIsSubmittingRating(true)
    try {
      const res = await interactionApi.rateNovel(slug, selectedScore, reviewInput)
      setRatingSuccessMsg('Cảm ơn bạn đã gửi đánh giá!')
      setInteractionStatus((prev) => ({
        ...prev,
        userRating: res.score,
        userReview: res.reviewText,
      }))
      interactionApi.getRatings(slug).then((ratings) => setRatingsList(ratings || []))
      catalogApi.novel(slug).then(setNovel)
      setTimeout(() => setRatingSuccessMsg(''), 3000)
    } catch (err) {
      alert(err.message || 'Vui lòng đăng nhập để đánh giá tác phẩm!')
    } finally {
      setIsSubmittingRating(false)
    }
  }

  if (error) {
    return (
      <section className="empty-state">
        <h2>Chưa tải được thông tin tác phẩm.</h2>
        <p>{error}</p>
        <Link className="button" to="/">Về Trang chủ</Link>
      </section>
    )
  }

  if (!novel) return <div className="route-loader" aria-label="Đang tải" />

  const firstChapter = chapters.length > 0 ? chapters[0] : null
  const coverUrl = getNovelCoverUrl(novel)

  return (
    <section className="novel-detail-page">
      <Link className="back-link" to="/tim-kiem">← Trở lại kho truyện</Link>
      <div className="novel-detail-hero">
        {coverUrl && !imgError ? (
          <img
            className="novel-detail-cover novel-cover-image"
            src={coverUrl}
            alt={`Bìa ${novel.title}`}
            onError={() => setImgError(true)}
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
          <p className="eyebrow">{statusLabels[novel.status] || novel.status} · ★ {Number(novel.averageRating).toFixed(1)} ({novel.ratingCount} lượt đánh giá)</p>
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
              <span className="button disabled">Truyện đang cập nhật</span>
            )}

            <button
              className={`button secondary favorite-toggle ${interactionStatus.isFavorite ? 'is-favorite' : ''}`}
              onClick={handleToggleFavorite}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.5rem',
                backgroundColor: interactionStatus.isFavorite ? '#fff5f5' : undefined,
                borderColor: interactionStatus.isFavorite ? '#e53e3e' : undefined,
                color: interactionStatus.isFavorite ? '#e53e3e' : undefined,
              }}
            >
              {interactionStatus.isFavorite ? '❤️ Đã lưu Tủ sách' : '🤍 Lưu vào Tủ sách'}
              <span className="fav-count">({interactionStatus.favoriteCount})</span>
            </button>
          </div>
        </div>
      </div>

      {/* Interactive Rating & Review Section */}
      <div className="novel-rating-section" style={{ marginTop: '3rem', padding: '2rem', backgroundColor: '#fcfbf7', borderRadius: '16px', border: '1px solid #eae7dc' }}>
        <h2 style={{ fontSize: '1.5rem', marginBottom: '1rem', color: '#102a20' }}>Đánh giá & Nhận xét từ độc giả</h2>
        
        {/* Rating Form */}
        <form onSubmit={handleSubmitRating} style={{ marginBottom: '2.5rem', paddingBottom: '2rem', borderBottom: '1px solid #eae7dc' }}>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', fontWeight: 600, marginBottom: '0.5rem' }}>Đánh giá của bạn về tác phẩm:</label>
            <div style={{ display: 'flex', gap: '0.5rem', fontSize: '1.8rem', cursor: 'pointer' }}>
              {[1, 2, 3, 4, 5].map((star) => (
                <span
                  key={star}
                  onClick={() => setSelectedScore(star)}
                  onMouseEnter={() => setHoverScore(star)}
                  onMouseLeave={() => setHoverScore(0)}
                  style={{ color: star <= (hoverScore || selectedScore) ? '#f59e0b' : '#cbd5e1', transition: 'color 0.2s' }}
                >
                  ★
                </span>
              ))}
              <span style={{ fontSize: '1rem', alignSelf: 'center', color: '#64748b', marginLeft: '0.5rem' }}>
                ({hoverScore || selectedScore}/5 sao)
              </span>
            </div>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', fontWeight: 600, marginBottom: '0.5rem' }}>Nhận xét (không bắt buộc):</label>
            <textarea
              rows="3"
              value={reviewInput}
              onChange={(e) => setReviewInput(e.target.value)}
              placeholder="Chia sẻ cảm nghĩ của bạn về tác phẩm này với cộng đồng độc giả..."
              style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '0.95rem' }}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <button className="button" type="submit" disabled={isSubmittingRating}>
              {isSubmittingRating ? 'Đang gửi...' : 'Gửi đánh giá'}
            </button>
            {ratingSuccessMsg && <span style={{ color: '#059669', fontWeight: 600 }}>{ratingSuccessMsg}</span>}
          </div>
        </form>

        {/* Reviews List */}
        <h3 style={{ fontSize: '1.2rem', marginBottom: '1rem' }}>Tất cả đánh giá ({ratingsList.length})</h3>
        {ratingsList.length === 0 ? (
          <p style={{ color: '#718096', textAlign: 'center', padding: '2rem 0' }}>
            Chưa có đánh giá nào. Hãy là người đầu tiên đánh giá tác phẩm này!
          </p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {ratingsList.map((r) => (
              <div
                key={r.id}
                style={{
                  padding: '1.25rem',
                  borderRadius: '10px',
                  border: '1px solid #edf2f7',
                  backgroundColor: '#ffffff'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <div
                      style={{
                        width: '36px',
                        height: '36px',
                        borderRadius: '50%',
                        backgroundColor: '#1d5b43',
                        color: '#d7ed74',
                        display: 'grid',
                        placeItems: 'center',
                        fontWeight: 700,
                        fontSize: '0.9rem'
                      }}
                    >
                      {r.userDisplayName ? r.userDisplayName.charAt(0).toUpperCase() : 'U'}
                    </div>
                    <div>
                      <strong style={{ display: 'block', fontSize: '0.95rem' }}>{r.userDisplayName}</strong>
                      <span style={{ color: '#ecc94b', fontSize: '0.9rem' }}>
                        {'★'.repeat(r.score)}{'☆'.repeat(5 - r.score)} ({r.score}/5)
                      </span>
                    </div>
                  </div>
                  <span style={{ fontSize: '0.8rem', color: '#a0aec0' }}>
                    {new Date(r.createdAt).toLocaleDateString('vi-VN')}
                  </span>
                </div>

                {r.reviewText && (
                  <p style={{ margin: '0.5rem 0 0 0', color: '#4a5568', fontSize: '0.95rem', lineHeight: '1.6' }}>
                    {r.reviewText}
                  </p>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Similar Novels Section (Content-Based Recommendations) */}
      {similarNovels.length > 0 && (
        <div className="similar-novels-section" style={{ marginTop: '3.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '1.5rem' }}>
            <h2 style={{ fontSize: '1.5rem', color: '#102a20', margin: 0 }}>📚 Tác phẩm tương tự có thể bạn thích</h2>
            <span style={{ fontSize: '0.875rem', color: '#64748b' }}>Dựa trên thuật toán Lọc theo nội dung (Content-Based)</span>
          </div>

          <div className="recommendation-grid">
            {similarNovels.map((rec) => (
              <div key={rec.novel.id} className="rec-card">
                <div className="rec-badge-overlay">
                  <span className="match-badge">⚡ {rec.matchPercentage}% Tương đồng</span>
                </div>
                <Link to={`/truyen/${rec.novel.slug}`} className="rec-cover-link">
                  <NovelCover novel={rec.novel} />
                </Link>
                <div className="rec-card-body">
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
        </div>
      )}

      <div className="chapter-list-section" style={{ marginTop: '3rem' }}>
        <h2 style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>Danh sách chương ({chapters.length})</h2>
        <div className="chapter-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '0.75rem' }}>
          {chapters.map((ch) => (
            <Link
              key={ch.id}
              to={`/truyen/${slug}/chuong-${ch.chapterNumber}`}
              className="chapter-item"
              style={{
                padding: '0.75rem 1rem',
                borderRadius: '8px',
                border: '1px solid #e2e8f0',
                textDecoration: 'none',
                color: '#2d3748',
                display: 'flex',
                justify: 'space-between',
                alignItems: 'center',
                backgroundColor: '#ffffff'
              }}
            >
              <span style={{ fontWeight: 500 }}>Chương {ch.chapterNumber}: {ch.title}</span>
              <span style={{ fontSize: '0.8rem', color: '#a0aec0' }}>→</span>
            </Link>
          ))}
        </div>
      </div>
    </section>
  )
}
