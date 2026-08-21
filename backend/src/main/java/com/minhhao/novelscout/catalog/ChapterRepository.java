package com.minhhao.novelscout.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    Optional<Chapter> findBySourceUrl(String sourceUrl);
    long countByNovelId(Long novelId);
    
    long countByNovelSlugAndPublicationStatus(String novelSlug, PublicationStatus publicationStatus);
    
    List<Chapter> findByNovelSlugAndPublicationStatusOrderByChapterNumberAsc(String novelSlug, PublicationStatus publicationStatus);
    
    Optional<Chapter> findByNovelSlugAndChapterNumberAndPublicationStatus(String novelSlug, BigDecimal chapterNumber, PublicationStatus publicationStatus);
    
    Optional<Chapter> findFirstByNovelSlugAndChapterNumberLessThanAndPublicationStatusOrderByChapterNumberDesc(String novelSlug, BigDecimal chapterNumber, PublicationStatus publicationStatus);
    
    Optional<Chapter> findFirstByNovelSlugAndChapterNumberGreaterThanAndPublicationStatusOrderByChapterNumberAsc(String novelSlug, BigDecimal chapterNumber, PublicationStatus publicationStatus);
}

