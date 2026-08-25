package com.minhhao.novelscout.interaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NovelFavoriteRepository extends JpaRepository<NovelFavorite, NovelFavorite.FavoriteId> {
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM NovelFavorite f WHERE f.id.userId = :userId AND f.id.novelId = :novelId")
    boolean existsByUserIdAndNovelId(@Param("userId") Long userId, @Param("novelId") Long novelId);

    @Query("SELECT f FROM NovelFavorite f WHERE f.id.userId = :userId AND f.id.novelId = :novelId")
    Optional<NovelFavorite> findByUserIdAndNovelId(@Param("userId") Long userId, @Param("novelId") Long novelId);

    @Query("SELECT f FROM NovelFavorite f JOIN FETCH f.novel n LEFT JOIN FETCH n.author WHERE f.id.userId = :userId ORDER BY f.createdAt DESC")
    List<NovelFavorite> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM NovelFavorite f WHERE f.id.novelId = :novelId")
    long countByNovelId(@Param("novelId") Long novelId);
}
