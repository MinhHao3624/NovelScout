package com.minhhao.novelscout.catalog.dto;

import com.minhhao.novelscout.catalog.Novel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record NovelSummaryResponse(
        Long id,
        String title,
        String slug,
        String authorName,
        String description,
        String coverUrl,
        String status,
        List<CategoryResponse> categories,
        long viewCount,
        BigDecimal averageRating,
        long ratingCount,
        Instant updatedAt
) {
    public static NovelSummaryResponse from(Novel novel) {
        return new NovelSummaryResponse(
                novel.getId(), novel.getTitle(), novel.getSlug(),
                novel.getAuthor() == null ? "Khuyết danh" : novel.getAuthor().getName(),
                novel.getDescription(), novel.getCoverUrl(), novel.getNovelStatus().name(),
                novel.getCategories().stream().map(CategoryResponse::from).toList(),
                novel.getViewCount(), novel.getAverageRating(), novel.getRatingCount(), novel.getUpdatedAt());
    }
}
