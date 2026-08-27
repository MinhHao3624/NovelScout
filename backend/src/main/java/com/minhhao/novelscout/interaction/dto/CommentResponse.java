package com.minhhao.novelscout.interaction.dto;

import com.minhhao.novelscout.interaction.NovelComment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record CommentResponse(
        Long id,
        Long novelId,
        Long chapterId,
        BigDecimal chapterNumber,
        String chapterTitle,
        Long userId,
        String username,
        String displayName,
        String avatarUrl,
        String content,
        Long parentId,
        Instant createdAt,
        List<CommentResponse> replies
) {
    public static CommentResponse from(NovelComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getNovel().getId(),
                comment.getChapter() != null ? comment.getChapter().getId() : null,
                comment.getChapter() != null ? comment.getChapter().getChapterNumber() : null,
                comment.getChapter() != null ? comment.getChapter().getTitle() : null,
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getUser().getDisplayName() != null ? comment.getUser().getDisplayName() : comment.getUser().getUsername(),
                comment.getUser().getAvatarUrl(),
                comment.getContent(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getCreatedAt(),
                new ArrayList<>()
        );
    }
}
