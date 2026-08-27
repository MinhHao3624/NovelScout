import React, { useEffect, useState } from 'react'
import { interactionApi } from '../api/interaction.js'
import { useAuth } from '../auth/authContext.js'
import { Link } from 'react-router-dom'

function formatTimeAgo(isoString) {
  if (!isoString) return ''
  const date = new Date(isoString)
  const now = new Date()
  const diffSec = Math.floor((now - date) / 1000)

  if (diffSec < 60) return 'Vừa xong'
  const diffMin = Math.floor(diffSec / 60)
  if (diffMin < 60) return `${diffMin} phút trước`
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour} giờ trước`
  const diffDay = Math.floor(diffHour / 24)
  if (diffDay < 7) return `${diffDay} ngày trước`

  return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function CommentItem({ comment, novelId, chapterId, currentUser, onRefresh }) {
  const [replying, setReplying] = useState(false)
  const [replyContent, setReplyContent] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const isOwner = currentUser && (currentUser.id === comment.userId || currentUser.username === comment.username)
  const isAdmin = currentUser && currentUser.roles && (currentUser.roles.includes('ADMIN') || currentUser.roles.includes('ROLE_ADMIN'))

  const handleSendReply = async (e) => {
    e.preventDefault()
    if (!replyContent.trim()) return
    setSubmitting(true)
    try {
      await interactionApi.postComment(novelId, {
        content: replyContent.trim(),
        parentId: comment.id,
        chapterId: chapterId || comment.chapterId || null
      })
      setReplyContent('')
      setReplying(false)
      onRefresh()
    } catch (err) {
      alert('Lỗi gửi phản hồi: ' + err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async () => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa bình luận này?')) return
    try {
      await interactionApi.deleteComment(comment.id)
      onRefresh()
    } catch (err) {
      alert('Lỗi xóa bình luận: ' + err.message)
    }
  }

  return (
    <div className="comment-item" style={{ marginBottom: '14px' }}>
      <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
        {/* Avatar */}
        {comment.avatarUrl ? (
          <img
            src={comment.avatarUrl}
            alt={comment.displayName}
            style={{ width: '38px', height: '38px', borderRadius: '50%', objectFit: 'cover' }}
          />
        ) : (
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '50%',
            backgroundColor: '#059669',
            color: '#ffffff',
            fontWeight: 'bold',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '1rem'
          }}>
            {comment.displayName ? comment.displayName.charAt(0).toUpperCase() : 'U'}
          </div>
        )}

        {/* Content Box */}
        <div style={{ flex: 1 }}>
          <div style={{
            backgroundColor: '#f8fafc',
            borderRadius: '12px',
            padding: '10px 14px',
            border: '1px solid #e2e8f0'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px', flexWrap: 'wrap', gap: '4px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
                <span style={{ fontWeight: '700', color: '#0f172a', fontSize: '0.92rem' }}>
                  {comment.displayName}
                </span>
                <span style={{ color: '#64748b', fontSize: '0.82rem' }}>
                  @{comment.username}
                </span>

                {/* Chapter Identifier Badge next to name */}
                {comment.chapterNumber && (
                  <span style={{
                    backgroundColor: '#e0f2fe',
                    color: '#0369a1',
                    fontSize: '0.75rem',
                    fontWeight: 700,
                    padding: '2px 8px',
                    borderRadius: '12px',
                    marginLeft: '4px'
                  }}>
                    Chương {comment.chapterNumber}
                  </span>
                )}
              </div>

              <span style={{ fontSize: '0.78rem', color: '#94a3b8' }}>
                {formatTimeAgo(comment.createdAt)}
              </span>
            </div>

            <p style={{ margin: 0, color: '#334155', fontSize: '0.92rem', lineHeight: '1.5', whiteSpace: 'pre-wrap' }}>
              {comment.content}
            </p>
          </div>

          {/* Action Links */}
          <div style={{ display: 'flex', gap: '16px', marginTop: '4px', fontSize: '0.82rem', paddingLeft: '4px' }}>
            {currentUser && (
              <button
                type="button"
                onClick={() => setReplying(!replying)}
                style={{ background: 'none', border: 'none', color: '#0284c7', fontWeight: 600, cursor: 'pointer', padding: 0 }}
              >
                💬 {replying ? 'Hủy' : 'Trả lời'}
              </button>
            )}
            {(isOwner || isAdmin) && (
              <button
                type="button"
                onClick={handleDelete}
                style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer', padding: 0 }}
              >
                🗑️ Xóa
              </button>
            )}
          </div>

          {/* Inline Reply Form */}
          {replying && (
            <form onSubmit={handleSendReply} style={{ marginTop: '8px', display: 'flex', gap: '8px' }}>
              <input
                autoFocus
                type="text"
                value={replyContent}
                onChange={(e) => setReplyContent(e.target.value)}
                placeholder={`Trả lời ${comment.displayName}…`}
                style={{
                  flex: 1,
                  padding: '8px 12px',
                  borderRadius: '8px',
                  border: '1px solid #cbd5e1',
                  fontSize: '0.88rem'
                }}
              />
              <button
                type="submit"
                className="button primary"
                disabled={submitting || !replyContent.trim()}
                style={{ padding: '6px 12px', fontSize: '0.82rem' }}
              >
                {submitting ? 'Gửi...' : 'Gửi'}
              </button>
            </form>
          )}

          {/* Render Nested Child Replies */}
          {comment.replies && comment.replies.length > 0 && (
            <div style={{ borderLeft: '2px solid #e2e8f0', paddingLeft: '14px', marginTop: '10px' }}>
              {comment.replies.map((reply) => (
                <CommentItem
                  key={reply.id}
                  comment={reply}
                  novelId={novelId}
                  chapterId={chapterId}
                  currentUser={currentUser}
                  onRefresh={onRefresh}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export function CommentSection({ novelId, chapterId, chapterNumber }) {
  const { user } = useAuth()
  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(true)
  const [content, setContent] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [showAllComments, setShowAllComments] = useState(false)

  const loadComments = async () => {
    try {
      let res;
      if (chapterId) {
        res = await interactionApi.getChapterComments(chapterId)
      } else {
        res = await interactionApi.getNovelComments(novelId)
      }
      setComments(res || [])
    } catch (err) {
      console.error('Lỗi tải bình luận:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (novelId || chapterId) {
      loadComments()
    }
  }, [novelId, chapterId])

  const countTotalComments = (list) => {
    let total = 0;
    for (const item of list) {
      total += 1;
      if (item.replies) {
        total += countTotalComments(item.replies);
      }
    }
    return total;
  };

  const handlePostTopComment = async (e) => {
    e.preventDefault()
    if (!content.trim()) return
    setError('')
    setSubmitting(true)
    try {
      await interactionApi.postComment(novelId, {
        content: content.trim(),
        chapterId: chapterId || null
      })
      setContent('')
      loadComments()
    } catch (err) {
      setError(err.message || 'Không thể đăng bình luận')
    } finally {
      setSubmitting(false)
    }
  }

  const totalCount = countTotalComments(comments);
  const visibleComments = showAllComments ? comments : comments.slice(0, 2);

  return (
    <section className="comments-section" style={{ marginTop: '2.5rem', paddingTop: '1.75rem', borderTop: '1px solid #e2e8f0' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0, color: '#0f172a', display: 'flex', alignItems: 'center', gap: '8px' }}>
          💬 {chapterNumber ? `Bình luận Chương ${chapterNumber}` : 'Thảo luận & Bình luận'} ({totalCount})
        </h3>
        {comments.length > 2 && (
          <button
            type="button"
            onClick={() => setShowAllComments(!showAllComments)}
            style={{
              background: 'none',
              border: 'none',
              color: '#0284c7',
              fontWeight: 600,
              cursor: 'pointer',
              fontSize: '0.88rem'
            }}
          >
            {showAllComments ? '▲ Thu gọn danh sách' : `▼ Xem tất cả (${totalCount} bình luận)`}
          </button>
        )}
      </div>

      {/* Write New Top-level Comment */}
      {user ? (
        <form onSubmit={handlePostTopComment} style={{ marginBottom: '1.75rem' }}>
          {error && <div className="form-notice error" style={{ marginBottom: '0.75rem' }}>{error}</div>}
          <div style={{ display: 'flex', gap: '12px' }}>
            <textarea
              rows="2"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder={chapterNumber ? `Viết bình luận cho Chương ${chapterNumber}…` : 'Chia sẻ cảm nhận của bạn về tác phẩm này…'}
              style={{
                flex: 1,
                padding: '10px 12px',
                borderRadius: '8px',
                border: '1px solid #cbd5e1',
                fontSize: '0.92rem',
                resize: 'vertical',
                fontFamily: 'inherit'
              }}
            />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '8px' }}>
            <button
              type="submit"
              className="button primary"
              disabled={submitting || !content.trim()}
              style={{ padding: '7px 18px', fontSize: '0.88rem' }}
            >
              {submitting ? 'Đang gửi…' : 'Đăng bình luận'}
            </button>
          </div>
        </form>
      ) : (
        <div style={{
          padding: '14px',
          backgroundColor: '#f1f5f9',
          borderRadius: '8px',
          marginBottom: '1.75rem',
          textAlign: 'center',
          color: '#475569',
          fontSize: '0.9rem'
        }}>
          Vui lòng <Link to="/dang-nhap" style={{ color: '#0284c7', fontWeight: 600 }}>Đăng nhập</Link> hoặc <Link to="/dang-ky" style={{ color: '#0284c7', fontWeight: 600 }}>Đăng ký</Link> để tham gia bình luận.
        </div>
      )}

      {/* List Comments Tree (Collapsible Sổ Dọc) */}
      {loading ? (
        <p style={{ color: '#64748b', fontSize: '0.9rem' }}>Đang tải bình luận…</p>
      ) : comments.length === 0 ? (
        <p style={{ color: '#94a3b8', fontStyle: 'italic', fontSize: '0.9rem' }}>
          {chapterNumber ? `Chương ${chapterNumber} chưa có bình luận nào. Hãy là người đầu tiên!` : 'Chưa có bình luận nào. Hãy là người đầu tiên để lại cảm nhận!'}
        </p>
      ) : (
        <div className="comments-tree">
          {visibleComments.map((item) => (
            <CommentItem
              key={item.id}
              comment={item}
              novelId={novelId}
              chapterId={chapterId}
              currentUser={user}
              onRefresh={loadComments}
            />
          ))}

          {!showAllComments && comments.length > 2 && (
            <button
              type="button"
              onClick={() => setShowAllComments(true)}
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '8px',
                border: '1px dashed #cbd5e1',
                background: '#ffffff',
                color: '#0284c7',
                fontWeight: 600,
                fontSize: '0.85rem',
                cursor: 'pointer',
                marginTop: '6px'
              }}
            >
              ▼ Xem thêm {comments.length - 2} bình luận khác...
            </button>
          )}
        </div>
      )}
    </section>
  )
}

export default CommentSection;
