package com.minhhao.novelscout.catalog;

import com.minhhao.novelscout.catalog.dto.CategoryResponse;
import com.minhhao.novelscout.catalog.dto.ChapterDetailResponse;
import com.minhhao.novelscout.catalog.dto.ChapterSummaryResponse;
import com.minhhao.novelscout.catalog.dto.NovelDetailResponse;
import com.minhhao.novelscout.catalog.dto.NovelSummaryResponse;
import com.minhhao.novelscout.catalog.dto.PageResponse;
import com.minhhao.novelscout.common.api.ApiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class CatalogService {
    private final NovelRepository novelRepository;
    private final CategoryRepository categoryRepository;
    private final ChapterRepository chapterRepository;

    public CatalogService(NovelRepository novelRepository, CategoryRepository categoryRepository, ChapterRepository chapterRepository) {
        this.novelRepository = novelRepository;
        this.categoryRepository = categoryRepository;
        this.chapterRepository = chapterRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<NovelSummaryResponse> getNovels(String query, String category, String status,
                                                        String sort, int page, int size) {
        NovelStatus novelStatus = parseStatus(status);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 24), resolveSort(sort));
        var novels = novelRepository.findAll(
                NovelSpecifications.publishedCatalog(query, category, novelStatus), pageable);
        return PageResponse.from(novels, NovelSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public List<NovelSummaryResponse> getFeatured(int limit) {
        int safeLimit = Math.clamp(limit, 1, 8);
        var page = novelRepository.findAll(NovelSpecifications.publishedCatalog(null, null, null),
                PageRequest.of(0, safeLimit, Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("averageRating"))));
        return page.getContent().stream().map(NovelSummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public NovelDetailResponse getNovel(String slug) {
        return novelRepository.findBySlugAndPublicationStatus(slug, PublicationStatus.PUBLISHED)
                .map(NovelDetailResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Không tìm thấy truyện"));
    }

    @Transactional(readOnly = true)
    public List<ChapterSummaryResponse> getNovelChapters(String slug) {
        return chapterRepository.findByNovelSlugAndPublicationStatusOrderByChapterNumberAsc(slug, PublicationStatus.PUBLISHED)
                .stream()
                .map(ChapterSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChapterDetailResponse getChapterDetail(String slug, BigDecimal chapterNumber) {
        Chapter chapter = chapterRepository.findByNovelSlugAndChapterNumberAndPublicationStatus(slug, chapterNumber, PublicationStatus.PUBLISHED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CHAPTER_NOT_FOUND", "Không tìm thấy chương truyện"));

        BigDecimal prevChapterNum = chapterRepository.findFirstByNovelSlugAndChapterNumberLessThanAndPublicationStatusOrderByChapterNumberDesc(slug, chapterNumber, PublicationStatus.PUBLISHED)
                .map(Chapter::getChapterNumber)
                .orElse(null);

        BigDecimal nextChapterNum = chapterRepository.findFirstByNovelSlugAndChapterNumberGreaterThanAndPublicationStatusOrderByChapterNumberAsc(slug, chapterNumber, PublicationStatus.PUBLISHED)
                .map(Chapter::getChapterNumber)
                .orElse(null);

        long totalChapters = chapterRepository.countByNovelSlugAndPublicationStatus(slug, PublicationStatus.PUBLISHED);

        return ChapterDetailResponse.of(chapter, prevChapterNum, nextChapterNum, totalChapters);
    }

    @Transactional
    public void incrementViewCount(String slug) {
        novelRepository.incrementViewCount(slug);
    }

    private NovelStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return NovelStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_NOVEL_STATUS", "Trạng thái truyện không hợp lệ");
        }
    }

    private Sort resolveSort(String sort) {
        return switch (sort == null ? "latest" : sort) {
            case "popular" -> Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("updatedAt"));
            case "rating" -> Sort.by(Sort.Order.desc("averageRating"), Sort.Order.desc("ratingCount"));
            case "title" -> Sort.by(Sort.Order.asc("title"));
            default -> Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id"));
        };
    }
}

