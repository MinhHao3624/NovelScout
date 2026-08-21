package com.minhhao.novelscout.catalog.dto;

import com.minhhao.novelscout.catalog.Chapter;

import java.math.BigDecimal;
import java.time.Instant;

public record ChapterSummaryResponse(
        Long id,
        BigDecimal chapterNumber,
        String title,
        Instant publishedAt
) {
    public static ChapterSummaryResponse from(Chapter chapter) {
        return new ChapterSummaryResponse(
                chapter.getId(),
                chapter.getChapterNumber(),
                chapter.getTitle(),
                chapter.getPublishedAt()
        );
    }
}
