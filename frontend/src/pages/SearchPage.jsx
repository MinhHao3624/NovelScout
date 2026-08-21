import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { catalogApi } from '../api/catalog.js'
import FilterSelect from '../components/FilterSelect.jsx'

const statusLabels = {
  ONGOING: 'Đang ra',
  COMPLETED: 'Hoàn thành',
  HIATUS: 'Tạm dừng',
}

function NovelCover({ novel }) {
  return (
    <div className={`novel-cover cover-tone-${(novel.id % 4) + 1}`}>
      <span>NovelScout Selection</span>
      <strong>{novel.title}</strong>
      <small>{novel.authorName}</small>
    </div>
  )
}

function NovelCard({ novel }) {
  return (
    <Link className="novel-card" to={`/truyen/${novel.slug}`}>
      {novel.coverUrl
        ? <img className="novel-cover novel-cover-image" src={novel.coverUrl} alt={`Bìa ${novel.title}`} />
        : <NovelCover novel={novel} />}
      <div className="novel-card-copy">
        <div className="novel-meta">
          <span>{statusLabels[novel.status] || novel.status}</span>
          <span>★ {Number(novel.averageRating).toFixed(1)}</span>
        </div>
        <h3>{novel.title}</h3>
        <p className="novel-author">{novel.authorName}</p>
        <p className="novel-description">{novel.description}</p>
        <div className="category-list">
          {novel.categories.slice(0, 3).map((category) => (
            <span key={category.id}>{category.name}</span>
          ))}
        </div>
      </div>
    </Link>
  )
}

function LoadingCards({ count = 12 }) {
  return (
    <div className="novel-grid">
      {Array.from({ length: count }, (_, index) => (
        <div className="novel-skeleton" key={index} />
      ))}
    </div>
  )
}

export default function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [searchInput, setSearchInput] = useState(searchParams.get('query') ?? '')
  const [categories, setCategories] = useState([])
  const [novels, setNovels] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const query = searchParams.get('query') ?? ''
  const category = searchParams.get('category') ?? ''
  const status = searchParams.get('status') ?? ''
  const sort = searchParams.get('sort') ?? 'latest'
  const page = Number(searchParams.get('page') ?? 0)

  useEffect(() => {
    catalogApi.categories()
      .then(setCategories)
      .catch((err) => console.error('Lỗi lấy thể loại:', err))
  }, [])

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')

    catalogApi.novels({ query, category, status, sort, page, size: 12 })
      .then((data) => {
        if (active) setNovels(data)
      })
      .catch((err) => {
        if (active) setError(err.message || 'Không thể tải danh sách truyện')
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => { active = false }
  }, [query, category, status, sort, page])

  function updateFilters(updates) {
    const next = new URLSearchParams(searchParams)
    Object.entries(updates).forEach(([key, value]) => {
      if (value) next.set(key, value)
      else next.delete(key)
    })
    if (!Object.hasOwn(updates, 'page')) {
      next.delete('page')
    }
    setSearchParams(next)
  }

  function handleSearchSubmit(e) {
    e.preventDefault()
    updateFilters({ query: searchInput.trim() })
  }

  return (
    <div className="search-page-container">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Khám phá kho truyện</p>
          <h1>Tìm kiếm & Lọc tác phẩm</h1>
        </div>
        {novels && <span className="section-note">{novels.totalElements} tác phẩm được tìm thấy</span>}
      </div>

      <form className="search-bar-form" onSubmit={handleSearchSubmit} style={{ margin: '1.5rem 0 2rem 0', display: 'flex', gap: '0.75rem' }}>
        <input
          style={{ flex: 1, padding: '0.75rem 1rem', borderRadius: '8px', border: '1px solid var(--border-color, #e2e8f0)', fontSize: '1rem' }}
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          placeholder="Nhập tên truyện, tác giả..."
          aria-label="Từ khóa tìm kiếm"
        />
        <button className="button" type="submit">Tìm kiếm</button>
      </form>

      <div className="catalog-filters">
        <div className="category-filter" aria-label="Lọc theo thể loại">
          <button className={!category ? 'active' : ''} onClick={() => updateFilters({ category: '' })}>
            Tất cả thể loại
          </button>
          {categories.map((item) => (
            <button
              className={category === item.slug ? 'active' : ''}
              onClick={() => updateFilters({ category: item.slug })}
              key={item.id}
            >
              {item.name}
            </button>
          ))}
        </div>

        <div className="select-filters">
          <FilterSelect
            label="Trạng thái"
            value={status}
            onChange={(val) => updateFilters({ status: val })}
            options={[
              { value: '', label: 'Mọi trạng thái' },
              { value: 'ONGOING', label: 'Đang ra' },
              { value: 'COMPLETED', label: 'Hoàn thành' },
              { value: 'HIATUS', label: 'Tạm dừng' }
            ]}
          />
          <FilterSelect
            label="Sắp xếp"
            value={sort}
            onChange={(val) => updateFilters({ sort: val })}
            options={[
              { value: 'latest', label: 'Mới cập nhật' },
              { value: 'popular', label: 'Đọc nhiều' },
              { value: 'rating', label: 'Đánh giá cao' },
              { value: 'title', label: 'Tên A–Z' }
            ]}
          />
        </div>
      </div>

      {query && (
        <div className="search-summary" style={{ margin: '1rem 0' }}>
          Kết quả tìm kiếm cho: “<strong>{query}</strong>”
          <button style={{ marginLeft: '1rem', cursor: 'pointer', background: 'none', border: 'none', color: '#3182ce' }} onClick={() => { setSearchInput(''); updateFilters({ query: '' }) }}>
            Xóa tìm kiếm
          </button>
        </div>
      )}

      {error ? (
        <div className="catalog-state error">
          <strong>Đã xảy ra lỗi khi tải kho truyện.</strong>
          <span>{error}</span>
        </div>
      ) : loading ? (
        <LoadingCards count={12} />
      ) : novels?.content.length ? (
        <div className="novel-grid">
          {novels.content.map((novel) => (
            <NovelCard novel={novel} key={novel.id} />
          ))}
        </div>
      ) : (
        <div className="catalog-state" style={{ textAlign: 'center', padding: '3rem 1rem' }}>
          <strong>Chưa tìm thấy truyện phù hợp.</strong>
          <span>Vui lòng thử từ khóa khác hoặc điều chỉnh lại bộ lọc.</span>
        </div>
      )}

      {novels && novels.totalPages > 1 && (
        <nav className="pagination" aria-label="Phân trang" style={{ marginTop: '2.5rem', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem' }}>
          <button
            className="button secondary"
            disabled={novels.first}
            onClick={() => updateFilters({ page: String(page - 1) })}
          >
            ← Trang trước
          </button>
          <span>Trang <strong>{page + 1}</strong> / {novels.totalPages}</span>
          <button
            className="button secondary"
            disabled={novels.last}
            onClick={() => updateFilters({ page: String(page + 1) })}
          >
            Trang sau →
          </button>
        </nav>
      )}
    </div>
  )
}
