package com.minhhao.novelscout.recommendation.dto;

import com.minhhao.novelscout.catalog.dto.NovelSummaryResponse;

public record RecommendationResponse(
        NovelSummaryResponse novel,
        double score,
        int matchPercentage,
        String reason
) {}
