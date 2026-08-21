package com.minhhao.novelscout.catalog;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

public interface NovelRepository extends JpaRepository<Novel, Long>, JpaSpecificationExecutor<Novel> {
    boolean existsBySlug(String slug);

    Optional<Novel> findBySourceUrl(String sourceUrl);

    long countBySourceName(String sourceName);

    List<Novel> findAllBySlugInAndSourceUrlIsNull(Collection<String> slugs);

    @EntityGraph(attributePaths = {"author", "categories"})
    Optional<Novel> findBySlugAndPublicationStatus(String slug, PublicationStatus publicationStatus);

    @Override
    @EntityGraph(attributePaths = {"author"})
    Page<Novel> findAll(Specification<Novel> specification, Pageable pageable);

    @Modifying
    @Query("UPDATE Novel n SET n.viewCount = n.viewCount + 1 WHERE n.slug = :slug")
    void incrementViewCount(@Param("slug") String slug);
}

