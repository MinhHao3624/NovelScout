import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { catalogApi } from '../api/catalog.js'
import FilterSelect from '../components/FilterSelect.jsx'

const statusLabels = {
  ONGOING: 'Đang ra',
  COMPLETED: 'Hoàn thành',
  HIATUS: 'Tạm dừng',
}

function NovelCover({ novel, featured = false }) {
  return (
    <div className={`novel-cover cover-tone-${(novel.id % 4) + 1} ${featured ? 'featured-cover' : ''}`}>
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
        <div className="novel-meta"><span>{statusLabels[novel.status]}</span><span>★ {Number(novel.averageRating).toFixed(1)}</span></div>
        <h3>{novel.title}</h3>
        <p className="novel-author">{novel.authorName}</p>
        <p className="novel-description">{novel.description}</p>
        <div className="category-list">
          {novel.categories.slice(0, 2).map((category) => <span key={category.id}>{category.name}</span>)}
        </div>
      </div>
    </Link>
  )
}

function LoadingCards({ count = 4 }) {
  return <div className="novel-grid">{Array.from({ length: count }, (_, index) => <div className="novel-skeleton" key={index} />)}</div>
}

export default function HomePage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [searchInput, setSearchInput] = useState(searchParams.get('query') ?? '')
  const [categories, setCategories] = useState([])
  const [featured, setFeatured] = useState([])
  const [novels, setNovels] = useState(null)
  const [catalogError, setCatalogError] = useState('')
  const [featuredLoading, setFeaturedLoading] = useState(true)
  const [catalogLoading, setCatalogLoading] = useState(true)

  const query = searchParams.get('query') ?? ''
  const category = searchParams.get('category') ?? ''
  const status = searchParams.get('status') ?? ''
  const sort = searchParams.get('sort') ?? 'latest'
  const page = Number(searchParams.get('page') ?? 0)

  useEffect(() => {
    Promise.all([catalogApi.categories(), catalogApi.featured(4)])
      .then(([categoryData, featuredData]) => {
        setCategories(categoryData)
        setFeatured(featuredData)
      })
      .catch((error) => setCatalogError(error.message))
      .finally(() => setFeaturedLoading(false))
  }, [])

  useEffect(() => {
    let active = true
    catalogApi.novels({ query, category, status, sort, page, size: 8 })
      .then((data) => { if (active) setNovels(data) })
      .catch((error) => { if (active) setCatalogError(error.message) })
      .finally(() => { if (active) setCatalogLoading(false) })
    return () => { active = false }
  }, [query, category, status, sort, page])

  function updateFilters(updates) {
    setCatalogLoading(true)
    setCatalogError('')
    const next = new URLSearchParams(searchParams)
    Object.entries(updates).forEach(([key, value]) => value ? next.set(key, value) : next.delete(key))
    if (!Object.hasOwn(updates, 'page')) next.delete('page')
    setSearchParams(next)
  }

  function submitSearch(event) {
    event.preventDefault()
    updateFilters({ query: searchInput.trim() })
    document.getElementById('kho-truyen')?.scrollIntoView({ behavior: 'smooth' })
  }

  return (
    <>
      <section className="hero-section">
        <div className="hero-copy">
          <p className="eyebrow">Kho truyện dành riêng cho bạn</p>
          <h1>Đừng tìm một truyện hay. Hãy tìm truyện hợp với bạn.</h1>
          <p className="hero-description">Khám phá những thế giới mới, lọc theo gu đọc và để NovelScout hiểu bạn hơn sau mỗi câu chuyện.</p>
          <form className="hero-search" onSubmit={submitSearch}>
            <input value={searchInput} onChange={(event) => setSearchInput(event.target.value)} placeholder="Tên truyện hoặc tác giả…" aria-label="Tìm truyện" />
            <button className="button" type="submit">Tìm truyện</button>
          </form>
          <a className="secondary-link hero-browse-link" href="#kho-truyen">Hoặc xem toàn bộ kho truyện ↓</a>
        </div>
        <div className="hero-visual" aria-hidden="true">
          <div className="book book-back" /><div className="book book-middle" />
          <div className="book book-front"><span>NovelScout</span><strong>Stories find their readers.</strong></div>
        </div>
      </section>

      <section className="featured-section" aria-labelledby="featured-title">
        <div className="section-heading">
          <div><p className="eyebrow">Biên tập chọn lọc</p><h2 id="featured-title">Những câu chuyện đang được yêu thích.</h2></div>
          <span className="section-note">Dựa trên lượt đọc và đánh giá</span>
        </div>
        {featuredLoading ? <LoadingCards /> : (
          <div className="featured-grid">
            {featured.map((novel, index) => (
              <Link className={`featured-card ${index === 0 ? 'featured-primary' : ''}`} to={`/truyen/${novel.slug}`} key={novel.id}>
                {novel.coverUrl ? <img className="novel-cover-image" src={novel.coverUrl} alt="" /> : <NovelCover novel={novel} featured />}
                <div><span className="featured-number">0{index + 1}</span><h3>{novel.title}</h3><p>{novel.authorName}</p></div>
              </Link>
            ))}
          </div>
        )}
      </section>

      <section className="catalog-section" id="kho-truyen" aria-labelledby="catalog-title">
        <div className="section-heading catalog-heading">
          <div><p className="eyebrow">Kho truyện</p><h2 id="catalog-title">Chọn câu chuyện tiếp theo.</h2></div>
          {novels && <span className="section-note">{novels.totalElements} tác phẩm</span>}
        </div>

        <div className="catalog-filters" id="the-loai">
          <div className="category-filter" aria-label="Lọc theo thể loại">
            <button className={!category ? 'active' : ''} onClick={() => updateFilters({ category: '' })}>Tất cả</button>
            {categories.map((item) => <button className={category === item.slug ? 'active' : ''} onClick={() => updateFilters({ category: item.slug })} key={item.id}>{item.name}</button>)}
          </div>
          <div className="select-filters">
            <FilterSelect label="Trạng thái" value={status} onChange={(value) => updateFilters({ status: value })}
              options={[{ value: '', label: 'Mọi trạng thái' }, { value: 'ONGOING', label: 'Đang ra' }, { value: 'COMPLETED', label: 'Hoàn thành' }, { value: 'HIATUS', label: 'Tạm dừng' }]} />
            <FilterSelect label="Sắp xếp" value={sort} onChange={(value) => updateFilters({ sort: value })}
              options={[{ value: 'latest', label: 'Mới cập nhật' }, { value: 'popular', label: 'Đọc nhiều' }, { value: 'rating', label: 'Đánh giá cao' }, { value: 'title', label: 'Tên A–Z' }]} />
          </div>
        </div>

        {query && <div className="search-summary">Kết quả cho “{query}” <button onClick={() => { setSearchInput(''); updateFilters({ query: '' }) }}>Xóa tìm kiếm</button></div>}
        {catalogError ? <div className="catalog-state error"><strong>Chưa tải được kho truyện.</strong><span>{catalogError}</span></div>
          : catalogLoading ? <LoadingCards count={8} />
            : novels?.content.length ? <div className="novel-grid">{novels.content.map((novel) => <NovelCard novel={novel} key={novel.id} />)}</div>
              : <div className="catalog-state"><strong>Chưa tìm thấy truyện phù hợp.</strong><span>Thử đổi từ khóa hoặc chọn thể loại khác nhé.</span></div>}

        {novels && novels.totalPages > 1 && (
          <nav className="pagination" aria-label="Phân trang">
            <button disabled={novels.first} onClick={() => updateFilters({ page: String(page - 1) })}>← Trước</button>
            <span>Trang {page + 1} / {novels.totalPages}</span>
            <button disabled={novels.last} onClick={() => updateFilters({ page: String(page + 1) })}>Sau →</button>
          </nav>
        )}
      </section>
    </>
  )
}
