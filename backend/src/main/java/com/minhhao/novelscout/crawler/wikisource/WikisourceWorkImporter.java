package com.minhhao.novelscout.crawler.wikisource;

import com.minhhao.novelscout.catalog.Author;
import com.minhhao.novelscout.catalog.AuthorRepository;
import com.minhhao.novelscout.catalog.Category;
import com.minhhao.novelscout.catalog.CategoryRepository;
import com.minhhao.novelscout.catalog.Chapter;
import com.minhhao.novelscout.catalog.ChapterRepository;
import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.catalog.NovelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class WikisourceWorkImporter {
    private static final String SOURCE_NAME = "WIKISOURCE_VI";

    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final WikisourceTaxonomyRegistry taxonomyRegistry;

    public WikisourceWorkImporter(AuthorRepository authorRepository, CategoryRepository categoryRepository,
                                  NovelRepository novelRepository, ChapterRepository chapterRepository,
                                  WikisourceTaxonomyRegistry taxonomyRegistry) {
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.taxonomyRegistry = taxonomyRegistry;
    }

    @Transactional
    public PersistedWork persist(WikisourceWorkData data) {
        Author author = findOrCreateAuthor(data.author());
        Set<Category> categories = resolveCategories(data.sourcePage());
        Novel novel = novelRepository.findBySourceUrl(data.sourceUrl()).orElse(null);
        boolean created = novel == null;

        if (created) {
            novel = Novel.imported(data.title(), uniqueNovelSlug(data.title()), author, data.description(),
                    data.sourceUrl(), SOURCE_NAME, data.license(), categories);
        } else {
            novel.updateImportedMetadata(data.title(), author, data.description(), SOURCE_NAME,
                    data.license(), categories);
        }
        novel = novelRepository.saveAndFlush(novel);

        for (int index = 0; index < data.chapters().size(); index++) {
            WikisourceChapterData chapterData = data.chapters().get(index);
            Chapter chapter = chapterRepository.findBySourceUrl(chapterData.sourceUrl()).orElse(null);
            if (chapter == null) {
                chapter = Chapter.imported(novel, chapterData.title(), BigDecimal.valueOf(index + 1L),
                        chapterData.sanitizedHtml(), chapterData.sourceUrl());
            } else {
                chapter.updateImportedContent(chapterData.title(), chapterData.sanitizedHtml());
            }
            chapterRepository.save(chapter);
        }
        return new PersistedWork(novel, created, data.chapters().size());
    }

    private Author findOrCreateAuthor(String name) {
        String slug = Slugifier.slugify(name);
        return authorRepository.findBySlug(slug)
                .orElseGet(() -> authorRepository.save(new Author(name, slug,
                        "Tác giả của các tác phẩm được lưu trữ trên Wikisource tiếng Việt.")));
    }

    private Set<Category> resolveCategories(String sourcePage) {
        Set<Category> categories = new LinkedHashSet<>();
        taxonomyRegistry.categorySlugsFor(sourcePage).stream()
                .map(this::requiredCategory)
                .forEach(categories::add);
        return categories;
    }

    private Category requiredCategory(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalStateException("Thiếu thể loại hệ thống: " + slug));
    }

    private String uniqueNovelSlug(String title) {
        String base = Slugifier.slugify(title);
        if (!novelRepository.existsBySlug(base)) return base;
        String candidate = base + "-wikisource";
        int suffix = 2;
        while (novelRepository.existsBySlug(candidate)) candidate = base + "-wikisource-" + suffix++;
        return candidate;
    }
}
