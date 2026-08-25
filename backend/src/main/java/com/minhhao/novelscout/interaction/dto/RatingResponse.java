package com.minhhao.novelscout.interaction.dto;

import com.minhhao.novelscout.interaction.NovelRating;

import java.time.Instant;

public record RatingResponse(
        Long id,
        Long userId,
        String userDisplayName,
        String userAvatarUrl,
        int score,
        String reviewText,
        Instant createdAt
) {
    public static RatingResponse from(NovelRating rating) {
        String name = rating.getUser().getDisplayName();
        if (name == null || name.isBlank()) name = rating.getUser().getUsername();
        return new RatingResponse(
                rating.getId(),
                rating.getUser().getId(),
                name,
                rating.getUser().getAvatarUrl(),
                rating.getScore(),
                rating.getReviewText(),
                rating.getCreatedAt()
        );
    }
}
