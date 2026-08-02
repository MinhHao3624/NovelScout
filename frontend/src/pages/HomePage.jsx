import { Link } from 'react-router-dom'

const foundationItems = [
  ['Kho truyện chữ', 'Tìm kiếm, lọc và đọc truyện theo chương trên mọi thiết bị.'],
  ['Thu thập tự động', 'Crawler đồng bộ dữ liệu có kiểm soát từ các nguồn được cấu hình.'],
  ['Gợi ý cá nhân', 'Kết hợp nội dung và hành vi đọc để tìm tác phẩm phù hợp.'],
]

export default function HomePage() {
  return (
    <>
      <section className="hero-section">
        <div className="hero-copy">
          <p className="eyebrow">Kho truyện dành riêng cho bạn</p>
          <h1>Đừng tìm một truyện hay. Hãy tìm truyện hợp với bạn.</h1>
          <p className="hero-description">
            NovelScout giúp bạn khám phá truyện chữ, tiếp tục hành trình đang đọc
            và nhận gợi ý ngày càng chính xác theo sở thích.
          </p>
          <div className="hero-actions">
            <Link className="button" to="/tim-kiem">Khám phá truyện</Link>
            <Link className="secondary-link" to="/dang-ky">Tạo tài khoản →</Link>
          </div>
        </div>

        <div className="hero-visual" aria-hidden="true">
          <div className="book book-back" />
          <div className="book book-middle" />
          <div className="book book-front">
            <span>NovelScout</span>
            <strong>Stories find their readers.</strong>
          </div>
        </div>
      </section>

      <section className="foundation-section" aria-labelledby="foundation-title">
        <div>
          <p className="eyebrow">Đang được xây dựng</p>
          <h2 id="foundation-title">Một nền tảng đọc truyện có dữ liệu và cá tính.</h2>
        </div>
        <div className="feature-grid">
          {foundationItems.map(([title, description], index) => (
            <article className="feature-card" key={title}>
              <span>0{index + 1}</span>
              <h3>{title}</h3>
              <p>{description}</p>
            </article>
          ))}
        </div>
      </section>
    </>
  )
}
