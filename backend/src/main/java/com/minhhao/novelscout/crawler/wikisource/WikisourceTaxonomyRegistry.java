package com.minhhao.novelscout.crawler.wikisource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

@Component
public class WikisourceTaxonomyRegistry {
    private final Map<String, Set<String>> categoriesByPage;

    public WikisourceTaxonomyRegistry(
            @Value("${app.crawler.wikisource.taxonomy:classpath:crawler/wikisource-vi-taxonomy.json}")
            Resource taxonomyResource) {
        this.categoriesByPage = load(taxonomyResource);
    }

    public Set<String> categorySlugsFor(String sourcePage) {
        Set<String> categories = categoriesByPage.get(sourcePage);
        if (categories == null) {
            throw new WikisourceException("Chưa biên tập category cho tác phẩm Wikisource: " + sourcePage);
        }
        return categories;
    }

    public Set<String> sourcePages() {
        return categoriesByPage.keySet();
    }

    private Map<String, Set<String>> load(Resource resource) {
        try (var input = resource.getInputStream()) {
            TaxonomyFile file = new ObjectMapper().readValue(input, TaxonomyFile.class);
            Map<String, Set<String>> mapping = new LinkedHashMap<>();
            for (TaxonomyEntry work : file.works()) {
                String page = work.page().trim();
                Set<String> categories = Collections.unmodifiableSet(new LinkedHashSet<>(work.categories()));
                if (page.isBlank() || categories.isEmpty() || mapping.putIfAbsent(page, categories) != null) {
                    throw new WikisourceException("Taxonomy Wikisource chứa mục trống hoặc trùng lặp: " + page);
                }
            }
            return Collections.unmodifiableMap(mapping);
        } catch (IOException exception) {
            throw new WikisourceException("Không đọc được taxonomy Wikisource", exception);
        }
    }

    private record TaxonomyFile(List<TaxonomyEntry> works) {}
    private record TaxonomyEntry(String page, List<String> categories) {}
}
