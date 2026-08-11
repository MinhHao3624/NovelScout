package com.minhhao.novelscout.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    Optional<Chapter> findBySourceUrl(String sourceUrl);
    long countByNovelId(Long novelId);
}
