package com.minhhao.novelscout.recommendation.dto;

import java.util.List;

public class AdminRecommendationDto {

    public record RecommendationConfigDto(
            double contentWeight,
            double collaborativeWeight,
            double popularityWeight
    ) {}

    public record RecommendationMetricsDto(
            long totalUsers,
            long totalNovels,
            long totalInteractions,
            double matrixSparsityPercentage,
            int activeReadersCount
    ) {}

    public record SimulationItemDto(
            Long novelId,
            String title,
            String slug,
            String coverUrl,
            String authorName,
            double cbfScore,
            double cfScore,
            double popScore,
            double hybridScore,
            int matchPercentage,
            String reason
    ) {}

    public record SimulationResultDto(
            Long userId,
            String username,
            String displayName,
            List<SimulationItemDto> recommendations
    ) {}
}
