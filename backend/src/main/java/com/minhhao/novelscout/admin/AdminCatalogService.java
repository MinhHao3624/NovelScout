package com.minhhao.novelscout.admin;

import com.minhhao.novelscout.admin.dto.AdminChapterRequest;
import com.minhhao.novelscout.admin.dto.AdminNovelRequest;
import com.minhhao.novelscout.catalog.*;
import com.minhhao.novelscout.catalog.dto.ChapterDetailResponse;
import com.minhhao.novelscout.catalog.dto.NovelDetailResponse;
import com.minhhao.novelscout.catalog.dto.NovelSummaryResponse;
import com.minhhao.novelscout.common.api.ApiException;
import com.minhhao.novelscout.common.util.SlugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AdminCatalogService {

    private static final Logger log = LoggerFactory.getLogger(AdminCatalogService.class);

    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public AdminCatalogService(
            NovelRepository novelRepository,
            ChapterRepository chapterRepository,
            AuthorRepository authorRepository,
            CategoryRepository categoryRepository
    ) {
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<NovelSummaryResponse> getNovels(String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return novelRepository.findByTitleContainingIgnoreCase(query, pageable)
                    .map(NovelSummaryResponse::from);
        }
        return novelRepository.findAll(pageable).map(NovelSummaryResponse::from);
    }

    public NovelDetailResponse createNovel(AdminNovelRequest request) {
        String slug = SlugUtils.slugify(request.title());
        Author author = resolveAuthor(request.authorName());
        Set<Category> categories = resolveCategories(request.categoryIds());

        Novel novel = Novel.published(
                request.title(),
                slug,
                author,
                request.description(),
                request.status() != null ? request.status() : NovelStatus.COMPLETED,
                0,
                BigDecimal.ZERO,
                0,
                categories
        );

        if (request.coverUrl() != null && !request.coverUrl().isBlank()) {
            novel.setCoverUrl(request.coverUrl());
        }

        Novel saved = novelRepository.save(novel);
        log.info("Admin vừa tạo truyện mới ID={}: {}", saved.getId(), saved.getTitle());
        return NovelDetailResponse.from(saved);
    }

    public NovelDetailResponse updateNovel(Long id, AdminNovelRequest request) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Không tìm thấy truyện ID=" + id));

        Author author = resolveAuthor(request.authorName());
        Set<Category> categories = resolveCategories(request.categoryIds());

        novel.updateAdminInfo(
                request.title(),
                author,
                request.description(),
                request.status(),
                request.coverUrl(),
                categories
        );

        log.info("Admin vừa cập nhật truyện ID={}: {}", id, novel.getTitle());
        return NovelDetailResponse.from(novel);
    }

    public String uploadCoverImage(Long id, MultipartFile file) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Không tìm thấy truyện ID=" + id));

        if (file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "File tải lên không được để trống");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = ".png";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = novel.getSlug() + extension;
        Path frontendCoversDir = Paths.get("frontend/public/covers").toAbsolutePath().normalize();

        try {
            Files.createDirectories(frontendCoversDir);
            Path targetPath = frontendCoversDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String coverUrl = "/covers/" + filename;
            novel.setCoverUrl(coverUrl);
            novelRepository.save(novel);

            log.info("Admin đã upload ảnh bìa cho truyện ID={}: {}", id, coverUrl);
            return coverUrl;
        } catch (IOException e) {
            log.error("Lỗi khi lưu file ảnh bìa cho novel ID={}", id, e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_SAVE_ERROR", "Không thể lưu file ảnh bìa: " + e.getMessage());
        }
    }

    public void deleteNovel(Long id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Không tìm thấy truyện ID=" + id));
        novelRepository.delete(novel);
        log.info("Admin vừa xóa truyện ID={}: {}", id, novel.getTitle());
    }

    @Transactional(readOnly = true)
    public List<ChapterDetailResponse> getChaptersForNovel(Long novelId) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Không tìm thấy truyện ID=" + novelId));
        List<Chapter> chapters = chapterRepository.findByNovelSlugAndPublicationStatusOrderByChapterNumberAsc(novel.getSlug(), PublicationStatus.PUBLISHED);
        long total = chapters.size();
        return chapters.stream()
                .map(ch -> ChapterDetailResponse.of(ch, null, null, total))
                .toList();
    }

    public ChapterDetailResponse createChapter(Long novelId, AdminChapterRequest request) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Không tìm thấy truyện ID=" + novelId));

        BigDecimal chapterNum = BigDecimal.valueOf(request.chapterNumber());
        Chapter chapter = Chapter.imported(
                novel,
                request.title(),
                chapterNum,
                request.content(),
                null
        );

        Chapter saved = chapterRepository.save(chapter);
        log.info("Admin vừa thêm chương {} cho truyện ID={}", request.chapterNumber(), novelId);
        long total = chapterRepository.countByNovelId(novelId);
        return ChapterDetailResponse.of(saved, null, null, total);
    }

    public ChapterDetailResponse updateChapter(Long chapterId, AdminChapterRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CHAPTER_NOT_FOUND", "Không tìm thấy chương ID=" + chapterId));

        chapter.updateInfo(request.title(), BigDecimal.valueOf(request.chapterNumber()), request.content());
        log.info("Admin vừa sửa chương ID={}", chapterId);
        long total = chapterRepository.countByNovelId(chapter.getNovel().getId());
        return ChapterDetailResponse.of(chapter, null, null, total);
    }

    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CHAPTER_NOT_FOUND", "Không tìm thấy chương ID=" + chapterId));
        chapterRepository.delete(chapter);
        log.info("Admin vừa xóa chương ID={}", chapterId);
    }

    private Author resolveAuthor(String authorName) {
        if (authorName == null || authorName.isBlank()) {
            authorName = "không rõ";
        }
        String slug = SlugUtils.slugify(authorName);
        String finalName = authorName;
        return authorRepository.findBySlug(slug)
                .orElseGet(() -> authorRepository.save(new Author(finalName, slug, null)));
    }

    private Set<Category> resolveCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(categoryRepository.findAllById(categoryIds));
    }
}
