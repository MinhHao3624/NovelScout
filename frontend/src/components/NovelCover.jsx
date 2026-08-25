import React, { useState } from 'react';
import { getNovelCoverUrl } from '../api/catalog.js';

export function NovelCover({ novel, featured = false }) {
  const [imgError, setImgError] = useState(false);
  const coverUrl = novel ? getNovelCoverUrl(novel) : null;

  if (coverUrl && !imgError) {
    return (
      <img
        className={`novel-cover novel-cover-image ${featured ? 'featured-cover' : ''}`}
        src={coverUrl}
        alt={`Bìa ${novel?.title || ''}`}
        onError={() => setImgError(true)}
      />
    );
  }

  const toneClass = `cover-tone-${((novel?.id || 0) % 4) + 1}`;
  return (
    <div className={`novel-cover ${toneClass} ${featured ? 'featured-cover' : ''}`}>
      <span>NovelScout Selection</span>
      <strong>{novel?.title}</strong>
      <small>{novel?.authorName}</small>
    </div>
  );
}

export default NovelCover;
