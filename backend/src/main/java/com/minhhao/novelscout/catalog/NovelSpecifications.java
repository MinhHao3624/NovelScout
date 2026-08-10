package com.minhhao.novelscout.catalog;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class NovelSpecifications {
    private NovelSpecifications() {}

    static Specification<Novel> publishedCatalog(String query, String categorySlug, NovelStatus status) {
        List<Specification<Novel>> specifications = new ArrayList<>();
        specifications.add((root, criteriaQuery, builder) ->
                builder.equal(root.get("publicationStatus"), PublicationStatus.PUBLISHED));

        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            specifications.add((root, criteriaQuery, builder) -> builder.or(
                    builder.like(builder.lower(root.get("title")), pattern),
                    builder.like(builder.lower(root.join("author", JoinType.LEFT).get("name")), pattern)));
        }
        if (categorySlug != null && !categorySlug.isBlank()) {
            specifications.add((root, criteriaQuery, builder) -> {
                criteriaQuery.distinct(true);
                return builder.equal(root.join("categories").get("slug"), categorySlug.trim());
            });
        }
        if (status != null) {
            specifications.add((root, criteriaQuery, builder) -> builder.equal(root.get("novelStatus"), status));
        }
        return Specification.allOf(specifications);
    }
}
