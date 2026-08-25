package com.minhhao.novelscout.admin;

import com.minhhao.novelscout.admin.dto.AdminChapterRequest;
import com.minhhao.novelscout.admin.dto.AdminNovelRequest;
import com.minhhao.novelscout.catalog.dto.ChapterDetailResponse;
import com.minhhao.novelscout.catalog.dto.NovelDetailResponse;
import com.minhhao.novelscout.catalog.dto.NovelSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;

    public AdminCatalogController(AdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
    }

    @GetMapping("/novels")
    public ResponseEntity<Page<NovelSummaryResponse>> getNovels(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(adminCatalogService.getNovels(query, pageable));
    }

    @PostMapping("/novels")
    public ResponseEntity<NovelDetailResponse> createNovel(
            @Valid @RequestBody AdminNovelRequest request
    ) {
        return ResponseEntity.ok(adminCatalogService.createNovel(request));
    }

    @PutMapping("/novels/{id}")
    public ResponseEntity<NovelDetailResponse> updateNovel(
            @PathVariable Long id,
            @Valid @RequestBody AdminNovelRequest request
    ) {
        return ResponseEntity.ok(adminCatalogService.updateNovel(id, request));
    }

    @PostMapping(value = "/novels/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        String coverUrl = adminCatalogService.uploadCoverImage(id, file);
        return ResponseEntity.ok(Map.of("coverUrl", coverUrl));
    }

    @DeleteMapping("/novels/{id}")
    public ResponseEntity<Map<String, String>> deleteNovel(@PathVariable Long id) {
        adminCatalogService.deleteNovel(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa truyện thành công"));
    }

    @GetMapping("/novels/{id}/chapters")
    public ResponseEntity<List<ChapterDetailResponse>> getChapters(@PathVariable Long id) {
        return ResponseEntity.ok(adminCatalogService.getChaptersForNovel(id));
    }

    @PostMapping("/novels/{id}/chapters")
    public ResponseEntity<ChapterDetailResponse> createChapter(
            @PathVariable Long id,
            @Valid @RequestBody AdminChapterRequest request
    ) {
        return ResponseEntity.ok(adminCatalogService.createChapter(id, request));
    }

    @PutMapping("/chapters/{chapterId}")
    public ResponseEntity<ChapterDetailResponse> updateChapter(
            @PathVariable Long chapterId,
            @Valid @RequestBody AdminChapterRequest request
    ) {
        return ResponseEntity.ok(adminCatalogService.updateChapter(chapterId, request));
    }

    @DeleteMapping("/chapters/{chapterId}")
    public ResponseEntity<Map<String, String>> deleteChapter(@PathVariable Long chapterId) {
        adminCatalogService.deleteChapter(chapterId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa chương thành công"));
    }
}
