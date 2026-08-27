package com.minhhao.novelscout.interaction;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NovelCommentRepository extends JpaRepository<NovelComment, Long> {

    @EntityGraph(attributePaths = {"user", "parent", "chapter"})
    List<NovelComment> findByNovelIdOrderByCreatedAtAsc(Long novelId);

    @EntityGraph(attributePaths = {"user", "parent", "chapter"})
    List<NovelComment> findByChapterIdOrderByCreatedAtAsc(Long chapterId);

    long countByNovelId(Long novelId);

    long countByChapterId(Long chapterId);
}
