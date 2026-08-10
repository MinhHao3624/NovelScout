package com.minhhao.novelscout.catalog;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface NovelRepository extends JpaRepository<Novel, Long>, JpaSpecificationExecutor<Novel> {
    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"author", "categories"})
    Optional<Novel> findBySlugAndPublicationStatus(String slug, PublicationStatus publicationStatus);

    @Override
    @EntityGraph(attributePaths = {"author"})
    Page<Novel> findAll(Specification<Novel> specification, Pageable pageable);
}
