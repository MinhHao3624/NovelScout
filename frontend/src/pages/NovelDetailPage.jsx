import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { catalogApi, getNovelCoverUrl } from '../api/catalog.js'
import { interactionApi } from '../api/interaction.js'


const statusLabels = { ONGOING: 'Đang ra', COMPLETED: 'Hoàn thành', HIATUS: 'Tạm dừng' }

export default function NovelDetailPage() {
  const { slug } = useParams()
  const [novel, setNovel] = useState(null)
  const [chapters, setChapters] = useState([])
  const [error, setError] = useState('')
  const [lastRead, setLastRead] = useState(null)

  // Interactions State
  const [interactionStatus, setInteractionStatus] = useState({ isFavorite: false, favoriteCount: 0, userRating: null, userReview: '' })
  const [ratingsList, setRatingsList] = useState([])
  const [selectedScore, setSelectedScore] = useState(5)
  const [hoverScore, setHoverScore] = useState(0)
  const [reviewInput, setReviewInput] = useState('')
  const [isSubmittingRating, setIsSubmittingRating] = useState(false)
  const [ratingSuccessMsg, setRatingSuccessMsg] = useState('')

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
          .then((statusData) => {
            if (statusData) {
              setInteractionStatus(statusData)
              if (statusData.userRating) setSelectedScore(statusData.userRating)
              if (statusData.userReview) setReviewInput(statusData.userReview)
            }
          })
          .catch((err) => console.error('Lỗi tải trạng thái tương tác:', err))

        interactionApi.getRatings(slug)
          .then((ratingsData) => {
            if (ratingsData) setRatingsList(ratingsData)
          })
          .catch((err) => console.error('Lỗi tải danh sách nhận xét:', err))
      })
      .catch((requestError) => setError(requestError.message))
  }, [slug])


  const handleToggleFavorite = () => {
    interactionApi.toggleFavorite(slug)
      .then((res) => {
        setInteractionStatus((prev) => ({
          ...prev,
          isFavorite: res.isFavorite,
          favoriteCount: res.isFavorite ? prev.favoriteCount + 1 : Math.max(0, prev.favoriteCount - 1)
        }))
      })
      .catch((err) => {
        alert('Vui lòng đăng nhập để lưu tác phẩm vào Tủ sách yêu thích!')
      })
  }

  const handleSubmitRating = (e) => {
    e.preventDefault()
    setIsSubmittingRating(true)
    setRatingSuccessMsg('')

    interactionApi.rateNovel(slug, selectedScore, reviewInput.trim())
      .then((newRating) => {
        setRatingSuccessMsg('Cảm ơn bạn đã gửi đánh giá!')
        setInteractionStatus((prev) => ({ ...prev, userRating: selectedScore, userReview: reviewInput.trim() }))
        // Refresh novel detail & ratings list
        catalogApi.novel(slug).then(setNovel).catch(() => {})
        interactionApi.getRatings(slug).then(setRatingsList).catch(() => {})
      })
      .catch((err) => {
        alert(err.message || 'Vui lòng đăng nhập để đánh giá tác phẩm này!')
      })
      .finally(() => setIsSubmittingRating(false))
  }

  const [imgError, setImgError] = useState(false)

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
              <button className="button" disabled>Chưa có chương nào</button>
            )}

            {/* Favorite Toggle Button */}
            <button
              onClick={handleToggleFavorite}
              style={{
                padding: '0.65rem 1.2rem',
                borderRadius: '999px',
                border: interactionStatus.isFavorite ? '2px solid #e53e3e' : '1px solid #cbd5e0',
                backgroundColor: interactionStatus.isFavorite ? '#fff5f5' : '#ffffff',
                color: interactionStatus.isFavorite ? '#e53e3e' : '#4a5568',
                cursor: 'pointer',
                fontWeight: 600,
                fontSize: '0.9rem',
                display: 'flex',
                alignItems: 'center',
                gap: '0.4rem',
                transition: 'all 0.2s ease'
              }}
            >
              {interactionStatus.isFavorite ? '❤️ Đã yêu thích' : '🤍 Yêu thích'} ({interactionStatus.favoriteCount})
            </button>

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

      {/* User Reviews & Rating Section */}
      <div className="novel-reviews-section" style={{ marginTop: '3.5rem', paddingTop: '2rem', borderTop: '1px solid #e2e8f0' }}>
        <h2 style={{ fontSize: '1.5rem', marginBottom: '1.5rem' }}>
          Đánh giá & Nhận xét độc giả ({ratingsList.length})
        </h2>

        {/* Rating Submission Form */}
        <form
          onSubmit={handleSubmitRating}
          style={{
            backgroundColor: '#f8fafc',
            border: '1px solid #e2e8f0',
            borderRadius: '12px',
            padding: '1.5rem',
            marginBottom: '2.5rem'
          }}
        >
          <h3 style={{ fontSize: '1.1rem', marginBottom: '0.75rem' }}>
            {interactionStatus.userRating ? 'Cập nhật đánh giá của bạn:' : 'Viết đánh giá của bạn:'}
          </h3>

          {/* Interactive Star Picker */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', marginBottom: '1rem' }}>
            {[1, 2, 3, 4, 5].map((star) => (
              <span
                key={star}
                onMouseEnter={() => setHoverScore(star)}
                onMouseLeave={() => setHoverScore(0)}
                onClick={() => setSelectedScore(star)}
                style={{
                  fontSize: '1.8rem',
                  cursor: 'pointer',
                  color: star <= (hoverScore || selectedScore) ? '#ecc94b' : '#cbd5e0',
                  transition: 'color 0.15s ease'
                }}
              >
                ★
              </span>
            ))}
            <span style={{ marginLeft: '0.75rem', fontWeight: 600, color: '#4a5568' }}>
              {hoverScore || selectedScore} / 5 sao
            </span>
          </div>

          {/* Optional Review Text Input */}
          <textarea
            rows="3"
            value={reviewInput}
            onChange={(e) => setReviewInput(e.target.value)}
            placeholder="Viết nhận xét của bạn về tác phẩm này (không bắt buộc)..."
            style={{
              width: '100%',
              padding: '0.75rem',
              borderRadius: '8px',
              border: '1px solid #cbd5e0',
              fontSize: '0.95rem',
              fontFamily: 'inherit',
              resize: 'vertical',
              marginBottom: '1rem'
            }}
          />

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <button className="button" type="submit" disabled={isSubmittingRating}>
              {isSubmittingRating ? 'Đang gửi...' : 'Gửi đánh giá'}
            </button>
            {ratingSuccessMsg && <span style={{ color: '#38a169', fontWeight: 600, fontSize: '0.9rem' }}>{ratingSuccessMsg}</span>}
          </div>
        </form>

        {/* Reviews List */}
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
    </section>
  )
}
