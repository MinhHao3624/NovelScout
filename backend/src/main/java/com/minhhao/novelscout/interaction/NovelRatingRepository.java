package com.minhhao.novelscout.interaction;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NovelRatingRepository extends JpaRepository<NovelRating, Long> {
    Optional<NovelRating> findByUserIdAndNovelId(Long userId, Long novelId);

    @EntityGraph(attributePaths = {"user"})
    List<NovelRating> findByNovelIdOrderByCreatedAtDesc(Long novelId);

    @Query("SELECT AVG(r.score) FROM NovelRating r WHERE r.novel.id = :novelId")
    Double calculateAverageScoreByNovelId(@Param("novelId") Long novelId);

    @Query("SELECT AVG(r.score) FROM NovelRating r")
    Double findOverallAverageRating();

    long countByNovelId(Long novelId);

}
