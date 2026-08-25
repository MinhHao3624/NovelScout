package com.minhhao.novelscout.interaction.dto;

public record InteractionStatusResponse(
        boolean isFavorite,
        long favoriteCount,
        Integer userRating,
        String userReview
) {}
