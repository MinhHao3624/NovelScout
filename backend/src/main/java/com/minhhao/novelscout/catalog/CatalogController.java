package com.minhhao.novelscout.catalog;

import com.minhhao.novelscout.catalog.dto.CategoryResponse;
import com.minhhao.novelscout.catalog.dto.ChapterDetailResponse;
import com.minhhao.novelscout.catalog.dto.ChapterSummaryResponse;
import com.minhhao.novelscout.catalog.dto.NovelDetailResponse;
import com.minhhao.novelscout.catalog.dto.NovelSummaryResponse;
import com.minhhao.novelscout.catalog.dto.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/public/catalog")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) { this.catalogService = catalogService; }

    @GetMapping("/categories")
    List<CategoryResponse> categories() { return catalogService.getCategories(); }

    @GetMapping("/novels")
    PageResponse<NovelSummaryResponse> novels(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return catalogService.getNovels(query, category, status, sort, page, size);
    }

    @GetMapping("/featured")
    List<NovelSummaryResponse> featured(@RequestParam(defaultValue = "4") int limit) {
        return catalogService.getFeatured(limit);
    }

    @GetMapping("/novels/{slug}")
    NovelDetailResponse novel(@PathVariable String slug) { return catalogService.getNovel(slug); }

    @GetMapping("/novels/{slug}/chapters")
    List<ChapterSummaryResponse> chapters(@PathVariable String slug) {
        return catalogService.getNovelChapters(slug);
    }

    @GetMapping("/novels/{slug}/chapters/{chapterNumber}")
    ChapterDetailResponse chapter(@PathVariable String slug, @PathVariable BigDecimal chapterNumber) {
        return catalogService.getChapterDetail(slug, chapterNumber);
    }

    @PostMapping("/novels/{slug}/view")
    ResponseEntity<Void> incrementView(@PathVariable String slug) {
        catalogService.incrementViewCount(slug);
        return ResponseEntity.noContent().build();
    }
}

