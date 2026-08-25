import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getNovelCoverUrl } from '../api/catalog.js'
import { interactionApi } from '../api/interaction.js'

function NovelCover({ novel }) {
  return (
    <div className={`novel-cover cover-tone-${(novel.id % 4) + 1}`}>
      <span>NovelScout Selection</span>
      <strong>{novel.title}</strong>
      <small>{novel.authorName}</small>
    </div>
  )
}

export default function BookshelfPage() {
  const [favorites, setFavorites] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    interactionApi.getMyFavorites()
      .then(setFavorites)
      .catch((err) => setError(err.message || 'Không thể tải tủ sách'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="bookshelf-page" style={{ maxWidth: '1180px', margin: '0 auto', padding: '2rem 1rem 4rem 1rem' }}>
      <div className="section-heading">
        <div>
          <p className="eyebrow">Tủ sách cá nhân</p>
          <h1>Truyện yêu thích của bạn</h1>
        </div>
        <span className="section-note">{favorites.length} tác phẩm</span>
      </div>

      {loading ? (
        <div className="novel-grid">
          {Array.from({ length: 4 }, (_, i) => (
            <div className="novel-skeleton" key={i} />
          ))}
        </div>
      ) : error ? (
        <div className="catalog-state error">
          <strong>Chưa tải được tủ sách.</strong>
          <span>Vui lòng đăng nhập để xem danh sách truyện yêu thích của bạn.</span>
          <Link className="button" to="/dang-nhap" style={{ marginTop: '1rem' }}>Đăng nhập ngay</Link>
        </div>
      ) : favorites.length === 0 ? (
        <div className="catalog-state" style={{ padding: '4rem 1rem' }}>
          <strong>Tủ sách của bạn còn trống.</strong>
          <span>Hãy bấm nút "❤️ Yêu thích" ở các tác phẩm bạn quan tâm để lưu vào đây nhé.</span>
          <Link className="button" to="/tim-kiem" style={{ marginTop: '1.5rem' }}>Khám phá kho truyện</Link>
        </div>
      ) : (
        <div className="novel-grid">
          {favorites.map((novel) => {
            const cover = getNovelCoverUrl(novel)
            return (
              <Link className="novel-card" to={`/truyen/${novel.slug}`} key={novel.id}>
                {cover ? (
                  <img className="novel-cover novel-cover-image" src={cover} alt={`Bìa ${novel.title}`} />
                ) : (
                  <NovelCover novel={novel} />
                )}
                <div className="novel-card-copy">
                  <div className="novel-meta">
                    <span>❤️ Đã lưu</span>
                    <span>★ {Number(novel.averageRating).toFixed(1)}</span>
                  </div>
                  <h3>{novel.title}</h3>
                  <p className="novel-author">{novel.authorName}</p>
                  <p className="novel-description">{novel.description}</p>
                </div>
              </Link>
            )
          })}
        </div>
      )}
    </div>
  )
}

