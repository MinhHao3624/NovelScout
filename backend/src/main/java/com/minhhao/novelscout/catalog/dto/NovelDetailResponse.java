package com.minhhao.novelscout.catalog.dto;

import com.minhhao.novelscout.catalog.Novel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record NovelDetailResponse(
        Long id,
        String title,
        String slug,
        String authorName,
        String authorSlug,
        String description,
        String coverUrl,
        String status,
        List<CategoryResponse> categories,
        long viewCount,
        BigDecimal averageRating,
        long ratingCount,
        Instant publishedAt,
        Instant updatedAt
) {
    public static NovelDetailResponse from(Novel novel) {
        return new NovelDetailResponse(
                novel.getId(), novel.getTitle(), novel.getSlug(),
                novel.getAuthor() == null ? "Khuyết danh" : novel.getAuthor().getName(),
                novel.getAuthor() == null ? null : novel.getAuthor().getSlug(),
                novel.getDescription(), novel.getCoverUrl(), novel.getNovelStatus().name(),
                novel.getCategories().stream().map(CategoryResponse::from).toList(),
                novel.getViewCount(), novel.getAverageRating(), novel.getRatingCount(),
                novel.getPublishedAt(), novel.getUpdatedAt());
    }
}
