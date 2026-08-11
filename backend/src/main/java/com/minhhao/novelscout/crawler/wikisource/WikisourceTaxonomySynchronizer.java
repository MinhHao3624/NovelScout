package com.minhhao.novelscout.crawler.wikisource;

import com.minhhao.novelscout.catalog.Category;
import com.minhhao.novelscout.catalog.CategoryRepository;
import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.catalog.NovelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@Order(150)
public class WikisourceTaxonomySynchronizer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(WikisourceTaxonomySynchronizer.class);

    private final WikisourceTaxonomyRegistry taxonomyRegistry;
    private final NovelRepository novelRepository;
    private final CategoryRepository categoryRepository;

    public WikisourceTaxonomySynchronizer(WikisourceTaxonomyRegistry taxonomyRegistry,
                                          NovelRepository novelRepository,
                                          CategoryRepository categoryRepository) {
        this.taxonomyRegistry = taxonomyRegistry;
        this.novelRepository = novelRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int synchronizedWorks = 0;
        for (String sourcePage : taxonomyRegistry.sourcePages()) {
            Novel novel = novelRepository.findBySourceUrl(WikisourceParser.canonicalUrl(sourcePage)).orElse(null);
            if (novel == null) continue;
            novel.replaceCategories(resolveCategories(sourcePage));
            synchronizedWorks++;
        }
        log.info("WIKISOURCE_TAXONOMY_REPORT curatedWorks={} synchronizedWorks={}",
                taxonomyRegistry.sourcePages().size(), synchronizedWorks);
    }

    private Set<Category> resolveCategories(String sourcePage) {
        Set<Category> categories = new LinkedHashSet<>();
        for (String slug : taxonomyRegistry.categorySlugsFor(sourcePage)) {
            categories.add(categoryRepository.findBySlug(slug)
                    .orElseThrow(() -> new IllegalStateException("Thiếu category hệ thống: " + slug)));
        }
        return categories;
    }
}
