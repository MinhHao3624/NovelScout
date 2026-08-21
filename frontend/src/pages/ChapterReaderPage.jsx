import { useEffect, useState, useCallback } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { catalogApi } from '../api/catalog.js'

const THEMES = {
  light: { name: 'Trắng', bg: '#ffffff', text: '#1a202c', border: '#e2e8f0', toolbarBg: '#f8fafc' },
  sepia: { name: 'Vàng nhạt', bg: '#fbf0d9', text: '#5f4b32', border: '#e6d7b8', toolbarBg: '#f4e6c9' },
  dark: { name: 'Tối', bg: '#1a202c', text: '#e2e8f0', border: '#2d3748', toolbarBg: '#2d3748' },
  oled: { name: 'Đen (OLED)', bg: '#000000', text: '#d1d5db', border: '#1f2937', toolbarBg: '#111827' }
}

const FONTS = {
  sans: { name: 'Không chân (Sans)', family: 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif' },
  serif: { name: 'Có chân (Serif)', family: 'Georgia, Cambria, "Times New Roman", Times, serif' },
  mono: { name: 'Đơn cách (Mono)', family: 'Consolas, "Liberation Mono", Courier, monospace' }
}

export default function ChapterReaderPage() {
  const { slug, chapterNumber: paramChapterNumber, chapterPath } = useParams()
  const rawNum = paramChapterNumber || chapterPath || ''
  const chapterNumber = rawNum.replace(/^chuong-?/, '')
  const navigate = useNavigate()


  const [chapter, setChapter] = useState(null)
  const [chaptersList, setChaptersList] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showSettings, setShowSettings] = useState(false)

  // Reading Preferences saved in localStorage
  const [theme, setTheme] = useState(() => localStorage.getItem('novelscout_reader_theme') || 'light')
  const [font, setFont] = useState(() => localStorage.getItem('novelscout_reader_font') || 'serif')
  const [fontSize, setFontSize] = useState(() => Number(localStorage.getItem('novelscout_reader_fontsize')) || 19)

  // Save preferences
  useEffect(() => {
    localStorage.setItem('novelscout_reader_theme', theme)
    localStorage.setItem('novelscout_reader_font', font)
    localStorage.setItem('novelscout_reader_fontsize', fontSize.toString())
  }, [theme, font, fontSize])

  // Fetch chapter detail and chapters list
  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')

    Promise.all([
      catalogApi.chapter(slug, chapterNumber),
      catalogApi.chapters(slug)
    ])
      .then(([chapterData, listData]) => {
        if (active) {
          setChapter(chapterData)
          setChaptersList(listData)
          // Increment view count
          catalogApi.incrementView(slug).catch(() => {})
          // Save reading history to localStorage
          const historyKey = `novelscout_history_${slug}`
          localStorage.setItem(historyKey, JSON.stringify({
            chapterNumber: chapterData.chapterNumber,
            title: chapterData.title,
            updatedAt: new Date().toISOString()
          }))
        }
      })
      .catch((err) => {
        if (active) setError(err.message || 'Không thể tải chương truyện này')
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => { active = false }
  }, [slug, chapterNumber])

  // Navigation handlers
  const goToPrevChapter = useCallback(() => {
    if (chapter?.prevChapterNumber != null) {
      navigate(`/truyen/${slug}/chuong-${chapter.prevChapterNumber}`)
    }
  }, [chapter, navigate, slug])

  const goToNextChapter = useCallback(() => {
    if (chapter?.nextChapterNumber != null) {
      navigate(`/truyen/${slug}/chuong-${chapter.nextChapterNumber}`)
    }
  }, [chapter, navigate, slug])

  // Keyboard navigation (Left/Right Arrow keys)
  useEffect(() => {
    function handleKeyDown(e) {
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') {
        return
      }
      if (e.key === 'ArrowLeft') {
        goToPrevChapter()
      } else if (e.key === 'ArrowRight') {
        goToNextChapter()
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [goToPrevChapter, goToNextChapter])

  const currentTheme = THEMES[theme] || THEMES.light
  const currentFont = FONTS[font] || FONTS.serif

  if (loading) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh' }}>
        <div className="novel-skeleton" style={{ width: '80%', height: '400px', borderRadius: '12px' }} />
        <p style={{ marginTop: '1rem', color: '#718096' }}>Đang tải nội dung chương...</p>
      </div>
    )
  }

  if (error || !chapter) {
    return (
      <div style={{ maxWidth: '600px', margin: '4rem auto', textAlign: 'center', padding: '2rem', borderRadius: '12px', border: '1px solid #feb2b2', backgroundColor: '#fff5f5' }}>
        <h2 style={{ color: '#e53e3e', marginBottom: '1rem' }}>Không thể đọc chương này</h2>
        <p style={{ color: '#4a5568', marginBottom: '1.5rem' }}>{error || 'Chương truyện không tồn tại hoặc đã bị ẩn.'}</p>
        <Link className="button" to={`/truyen/${slug}`}>Về trang chi tiết truyện</Link>
      </div>
    )
  }

  const renderChapterNav = () => (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '1rem'
      }}
    >
      <button
        onClick={goToPrevChapter}
        disabled={chapter.prevChapterNumber == null}
        style={{
          padding: '0.6rem 1.2rem',
          borderRadius: '8px',
          border: `1px solid ${currentTheme.border}`,
          backgroundColor: currentTheme.bg,
          color: currentTheme.text,
          cursor: chapter.prevChapterNumber != null ? 'pointer' : 'not-allowed',
          opacity: chapter.prevChapterNumber != null ? 1 : 0.4
        }}
      >
        ← Chương trước (←)
      </button>

      <select
        value={chapter.chapterNumber}
        onChange={(e) => navigate(`/truyen/${slug}/chuong-${e.target.value}`)}
        style={{
          padding: '0.6rem 1rem',
          borderRadius: '8px',
          border: `1px solid ${currentTheme.border}`,
          backgroundColor: currentTheme.bg,
          color: currentTheme.text,
          cursor: 'pointer',
          maxWidth: '220px'
        }}
      >
        {chaptersList.map((ch) => (
          <option key={ch.id} value={ch.chapterNumber}>
            Chương {ch.chapterNumber}: {ch.title}
          </option>
        ))}
      </select>

      <button
        onClick={goToNextChapter}
        disabled={chapter.nextChapterNumber == null}
        style={{
          padding: '0.6rem 1.2rem',
          borderRadius: '8px',
          border: `1px solid ${currentTheme.border}`,
          backgroundColor: currentTheme.bg,
          color: currentTheme.text,
          cursor: chapter.nextChapterNumber != null ? 'pointer' : 'not-allowed',
          opacity: chapter.nextChapterNumber != null ? 1 : 0.4
        }}
      >
        Chương sau (→) →
      </button>
    </div>
  )


  return (
    <div
      style={{
        backgroundColor: currentTheme.bg,
        color: currentTheme.text,
        minHeight: '100vh',
        transition: 'background-color 0.3s ease, color 0.3s ease',
        paddingBottom: '4rem'
      }}
    >
      {/* Top sticky navigation toolbar */}
      <header
        style={{
          position: 'sticky',
          top: 0,
          zIndex: 100,
          backgroundColor: currentTheme.toolbarBg,
          borderBottom: `1px solid ${currentTheme.border}`,
          padding: '0.75rem 1.5rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', overflow: 'hidden' }}>
          <Link
            to={`/truyen/${slug}`}
            style={{ color: currentTheme.text, textDecoration: 'none', fontWeight: 600, fontSize: '0.9rem', whiteSpace: 'nowrap' }}
          >
            ← {chapter.novelTitle}
          </Link>
          <span style={{ opacity: 0.5 }}>|</span>
          <span style={{ fontSize: '0.9rem', opacity: 0.8, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            Chương {chapter.chapterNumber}: {chapter.title}
          </span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <button
            onClick={() => setShowSettings(!showSettings)}
            style={{
              padding: '0.4rem 0.8rem',
              borderRadius: '6px',
              border: `1px solid ${currentTheme.border}`,
              backgroundColor: currentTheme.bg,
              color: currentTheme.text,
              cursor: 'pointer',
              fontSize: '0.85rem'
            }}
          >
            ⚙️ Cài đặt đọc
          </button>
        </div>
      </header>

      {/* Reader Settings Drawer/Bar */}
      {showSettings && (
        <div
          style={{
            backgroundColor: currentTheme.toolbarBg,
            borderBottom: `1px solid ${currentTheme.border}`,
            padding: '1rem 1.5rem',
            display: 'flex',
            flexWrap: 'wrap',
            gap: '1.5rem',
            alignItems: 'center',
            fontSize: '0.85rem'
          }}
        >
          {/* Theme selector */}
          <div>
            <strong style={{ display: 'block', marginBottom: '0.4rem' }}>Giao diện màu:</strong>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              {Object.entries(THEMES).map(([key, t]) => (
                <button
                  key={key}
                  onClick={() => setTheme(key)}
                  style={{
                    padding: '0.3rem 0.6rem',
                    borderRadius: '4px',
                    border: theme === key ? '2px solid #3182ce' : `1px solid ${t.border}`,
                    backgroundColor: t.bg,
                    color: t.text,
                    cursor: 'pointer',
                    fontSize: '0.8rem'
                  }}
                >
                  {t.name}
                </button>
              ))}
            </div>
          </div>

          {/* Font Family selector */}
          <div>
            <strong style={{ display: 'block', marginBottom: '0.4rem' }}>Kiểu chữ:</strong>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              {Object.entries(FONTS).map(([key, f]) => (
                <button
                  key={key}
                  onClick={() => setFont(key)}
                  style={{
                    padding: '0.3rem 0.6rem',
                    borderRadius: '4px',
                    border: font === key ? '2px solid #3182ce' : `1px solid ${currentTheme.border}`,
                    backgroundColor: currentTheme.bg,
                    color: currentTheme.text,
                    cursor: 'pointer',
                    fontSize: '0.8rem',
                    fontFamily: f.family
                  }}
                >
                  {f.name}
                </button>
              ))}
            </div>
          </div>

          {/* Font Size control */}
          <div>
            <strong style={{ display: 'block', marginBottom: '0.4rem' }}>Cỡ chữ ({fontSize}px):</strong>
            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
              <button
                disabled={fontSize <= 14}
                onClick={() => setFontSize((s) => Math.max(14, s - 1))}
                style={{ padding: '0.2rem 0.6rem', cursor: 'pointer' }}
              >
                A-
              </button>
              <button
                disabled={fontSize >= 32}
                onClick={() => setFontSize((s) => Math.min(32, s + 1))}
                style={{ padding: '0.2rem 0.6rem', cursor: 'pointer' }}
              >
                A+
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Main Chapter Content Container */}
      <main
        style={{
          maxWidth: '800px',
          margin: '2rem auto',
          padding: '0 1.5rem',
          fontFamily: currentFont.family,
          fontSize: `${fontSize}px`,
          lineHeight: '1.8'
        }}
      >
        {/* Top Navigation Bar */}
        <div style={{ marginBottom: '2rem', paddingBottom: '1.5rem', borderBottom: `1px solid ${currentTheme.border}` }}>
          {renderChapterNav()}
        </div>

        <div style={{ textAlign: 'center', marginBottom: '2.5rem' }}>
          <h2 style={{ fontSize: '1.2em', opacity: 0.85, fontWeight: 500, marginBottom: '0.5rem' }}>
            {chapter.novelTitle}
          </h2>
          <h1 style={{ fontSize: '1.6em', fontWeight: 700 }}>
            Chương {chapter.chapterNumber}: {chapter.title}
          </h1>
        </div>

        {/* Render HTML content securely */}
        <div
          className="chapter-content-body"
          dangerouslySetInnerHTML={{ __html: chapter.content }}
          style={{ wordBreak: 'break-word' }}
        />

        {/* Chapter Bottom Navigation */}
        <div
          style={{
            marginTop: '3.5rem',
            paddingTop: '1.5rem',
            borderTop: `1px solid ${currentTheme.border}`
          }}
        >
          {renderChapterNav()}
          <p style={{ textAlign: 'center', fontSize: '0.75rem', opacity: 0.6, marginTop: '1rem' }}>
            💡 Mẹo: Bạn có thể dùng phím <strong>←</strong> và <strong>→</strong> trên bàn phím để chuyển chương nhanh.
          </p>
        </div>
      </main>
    </div>
  )
}

