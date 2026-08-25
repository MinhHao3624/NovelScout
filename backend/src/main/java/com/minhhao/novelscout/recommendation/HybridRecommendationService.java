package com.minhhao.novelscout.recommendation;

import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.catalog.NovelRepository;
import com.minhhao.novelscout.catalog.dto.NovelSummaryResponse;
import com.minhhao.novelscout.common.api.ApiException;
import com.minhhao.novelscout.interaction.UserInteraction;
import com.minhhao.novelscout.interaction.UserInteractionRepository;
import com.minhhao.novelscout.recommendation.dto.RecommendationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HybridRecommendationService {

    private final ContentBasedFilteringService cbfService;
    private final CollaborativeFilteringService cfService;
    private final NovelRepository novelRepository;
    private final UserInteractionRepository userInteractionRepository;

    @Value("${app.recommendation.content-weight:0.45}")
    private double contentWeight;

    @Value("${app.recommendation.collaborative-weight:0.45}")
    private double collaborativeWeight;

    @Value("${app.recommendation.popularity-weight:0.10}")
    private double popularityWeight;

    public HybridRecommendationService(
            ContentBasedFilteringService cbfService,
            CollaborativeFilteringService cfService,
            NovelRepository novelRepository,
            UserInteractionRepository userInteractionRepository
    ) {
        this.cbfService = cbfService;
        this.cfService = cfService;
        this.novelRepository = novelRepository;
        this.userInteractionRepository = userInteractionRepository;
    }

    /**
     * Thuật toán Gợi ý cá nhân hóa Hybrid (Content-Based + Collaborative + Popularity)
     */
    public List<RecommendationResponse> getPersonalizedRecommendations(Long userId, int limit) {
        List<Novel> allNovels = novelRepository.findAll();
        Map<Long, Novel> novelMap = allNovels.stream().collect(Collectors.toMap(Novel::getId, n -> n));

        if (userId == null) {
            return getPopularityFallbackRecommendations(allNovels, limit);
        }

        // Lấy lịch sử tương tác của độc giả target
        List<UserInteraction> userInteractionsList = userInteractionRepository.findByUserId(userId);
        if (userInteractionsList.isEmpty()) {
            // Cold-Start: Độc giả chưa có lịch sử tương tác -> Fallback sang Popularity + Top Rated
            return getPopularityFallbackRecommendations(allNovels, limit);
        }

        Map<Long, Double> targetUserInteractions = userInteractionsList.stream()
                .collect(Collectors.toMap(
                        ui -> ui.getNovel().getId(),
                        ui -> ui.getWeight() != null ? ui.getWeight().doubleValue() : 1.0,
                        (a, b) -> Math.max(a, b)
                ));

        // 1. Tính điểm Content-Based Filtering
        Map<Long, Double> cbfScores = cbfService.calculateUserCBFScores(targetUserInteractions, novelMap);

        // 2. Tính điểm Collaborative Filtering
        Map<Long, Map<Long, Double>> userItemMatrix = cfService.buildUserItemMatrix();
        Map<Long, Double> cfScores = cfService.calculateUserCFScores(userId, userItemMatrix);

        // 3. Tính Popularity Score cho toàn bộ truyện
        long maxViews = allNovels.stream().mapToLong(Novel::getViewCount).max().orElse(1L);
        if (maxViews == 0) maxViews = 1L;

        Map<Long, Double> popScores = new HashMap<>();
        for (Novel novel : allNovels) {
            double normView = (double) novel.getViewCount() / maxViews;
            double normRating = novel.getAverageRating() != null ? novel.getAverageRating().doubleValue() / 5.0 : 0.8;
            popScores.put(novel.getId(), 0.6 * normView + 0.4 * normRating);
        }

        // 4. Kết hợp Ma trận lai (Hybrid Aggregation)
        List<RecommendationResponse> recommendations = new ArrayList<>();

        for (Novel candidate : allNovels) {
            if (targetUserInteractions.containsKey(candidate.getId())) {
                continue; // Bỏ qua truyện độc giả đã đọc
            }

            double cbf = cbfScores.getOrDefault(candidate.getId(), 0.0);
            double cf = cfScores.getOrDefault(candidate.getId(), 0.0);
            double pop = popScores.getOrDefault(candidate.getId(), 0.0);

            // Công thức Hybrid
            double hybridScore = contentWeight * cbf + collaborativeWeight * cf + popularityWeight * pop;

            int matchPercentage = Math.min(99, Math.max(60, (int) Math.round(hybridScore * 100)));
            String reason = generateReason(cbf, cf, pop, matchPercentage);

            recommendations.add(new RecommendationResponse(
                    NovelSummaryResponse.from(candidate),
                    hybridScore,
                    matchPercentage,
                    reason
            ));
        }

        // Sắp xếp điểm Hybrid giảm dần và giới hạn số lượng kết quả
        recommendations.sort(Comparator.comparingDouble(RecommendationResponse::score).reversed());
        return recommendations.stream().limit(limit).toList();
    }

    /**
     * Lấy danh sách Truyện Tương Tự (Similar Novels) dựa trên thuật toán Content-Based Filtering
     */
    public List<RecommendationResponse> getSimilarNovels(String slug, int limit) {
        Novel target = novelRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Không tìm thấy truyện slug=" + slug));

        List<Novel> allNovels = novelRepository.findAll();
        List<RecommendationResponse> result = new ArrayList<>();

        for (Novel candidate : allNovels) {
            if (candidate.getId().equals(target.getId())) continue;

            double sim = cbfService.calculateSimilarity(target, candidate);
            int matchPercentage = Math.min(99, Math.max(65, (int) Math.round(sim * 100)));

            String reason = String.format("%d%% Tương đồng về thể loại & tác giả %s",
                    matchPercentage,
                    candidate.getAuthor() != null ? candidate.getAuthor().getName() : "");

            result.add(new RecommendationResponse(
                    NovelSummaryResponse.from(candidate),
                    sim,
                    matchPercentage,
                    reason
            ));
        }

        result.sort(Comparator.comparingDouble(RecommendationResponse::score).reversed());
        return result.stream().limit(limit).toList();
    }

    private List<RecommendationResponse> getPopularityFallbackRecommendations(List<Novel> allNovels, int limit) {
        return allNovels.stream()
                .map(novel -> {
                    double normRating = novel.getAverageRating() != null ? novel.getAverageRating().doubleValue() / 5.0 : 0.8;
                    int matchPercentage = Math.min(98, Math.max(80, (int) Math.round(normRating * 95)));
                    return new RecommendationResponse(
                            NovelSummaryResponse.from(novel),
                            normRating,
                            matchPercentage,
                            "Tác phẩm nổi bật được cộng đồng đánh giá cao nhất"
                    );
                })
                .sorted(Comparator.comparingDouble(RecommendationResponse::score).reversed())
                .limit(limit)
                .toList();
    }

    private String generateReason(double cbf, double cf, double pop, int matchPercentage) {
        if (cf > 0.4 && cbf > 0.4) {
            return String.format("%d%% Phù hợp với gu đọc & được độc giả cùng sở thích đánh giá cao", matchPercentage);
        } else if (cbf >= cf && cbf > 0.2) {
            return String.format("%d%% Tương đồng về thể loại & tác giả bạn thường đọc", matchPercentage);
        } else if (cf > 0.2) {
            return String.format("%d%% Độc giả có lịch sử đọc giống bạn cũng rất yêu thích", matchPercentage);
        } else {
            return String.format("%d%% Tác phẩm nổi bật phù hợp với xu hướng đọc", matchPercentage);
        }
    }
}
