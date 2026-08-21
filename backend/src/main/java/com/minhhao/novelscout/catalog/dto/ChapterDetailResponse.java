package com.minhhao.novelscout.catalog.dto;

import com.minhhao.novelscout.catalog.Chapter;

import java.math.BigDecimal;
import java.time.Instant;

public record ChapterDetailResponse(
        Long id,
        String novelSlug,
        String novelTitle,
        BigDecimal chapterNumber,
        String title,
        String content,
        String contentFormat,
        BigDecimal prevChapterNumber,
        BigDecimal nextChapterNumber,
        long totalChapters,
        Instant publishedAt
) {
    public static ChapterDetailResponse of(Chapter chapter, BigDecimal prevChapterNumber, BigDecimal nextChapterNumber, long totalChapters) {
        return new ChapterDetailResponse(
                chapter.getId(),
                chapter.getNovel().getSlug(),
                chapter.getNovel().getTitle(),
                chapter.getChapterNumber(),
                chapter.getTitle(),
                chapter.getContent(),
                "HTML",
                prevChapterNumber,
                nextChapterNumber,
                totalChapters,
                chapter.getPublishedAt()
        );
    }
}
