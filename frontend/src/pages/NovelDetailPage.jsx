import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { catalogApi } from '../api/catalog.js'

const statusLabels = { ONGOING: 'Đang ra', COMPLETED: 'Hoàn thành', HIATUS: 'Tạm dừng' }

export default function NovelDetailPage() {
  const { slug } = useParams()
  const [novel, setNovel] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    catalogApi.novel(slug).then(setNovel).catch((requestError) => setError(requestError.message))
  }, [slug])

  if (error) return <section className="simple-page"><p className="eyebrow">Không tìm thấy</p><h1>{error}</h1><Link className="button" to="/">Về Trang chủ</Link></section>
  if (!novel) return <div className="route-loader" aria-label="Đang tải" />

  return (
    <section className="novel-detail-page">
      <Link className="back-link" to="/#kho-truyen">← Trở lại kho truyện</Link>
      <div className="novel-detail-hero">
        <div className={`novel-detail-cover cover-tone-${(novel.id % 4) + 1}`}>
          <span>NovelScout Selection</span><strong>{novel.title}</strong><small>{novel.authorName}</small>
        </div>
        <div className="novel-detail-copy">
          <p className="eyebrow">{statusLabels[novel.status]} · ★ {Number(novel.averageRating).toFixed(1)}</p>
          <h1>{novel.title}</h1>
          <p className="detail-author">bởi {novel.authorName}</p>
          <div className="category-list">{novel.categories.map((category) => <span key={category.id}>{category.name}</span>)}</div>
          <p className="detail-description">{novel.description}</p>
          {novel.sourceAttributionUrl && (
            <p className="source-attribution">Nguồn: <a href={novel.sourceAttributionUrl} target="_blank" rel="noreferrer">Wikisource tiếng Việt ↗</a> · {novel.sourceLicense === 'PUBLIC_DOMAIN' ? 'Phạm vi công cộng' : 'CC BY-SA'}</p>
          )}
          <div className="detail-actions"><button className="button" disabled>Chương truyện sắp ra mắt</button><span>{novel.viewCount.toLocaleString('vi-VN')} lượt đọc</span></div>
        </div>
      </div>
    </section>
  )
}
