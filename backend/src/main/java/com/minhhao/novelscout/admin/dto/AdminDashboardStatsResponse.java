package com.minhhao.novelscout.admin.dto;

public record AdminDashboardStatsResponse(
        long totalNovels,
        long totalChapters,
        long totalUsers,
        long totalViews,
        long totalRatings,
        long totalFavorites,
        double averageRating
) {}
