package com.minhhao.novelscout.recommendation;

import com.minhhao.novelscout.catalog.Category;
import com.minhhao.novelscout.catalog.Novel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContentBasedFilteringService {

    /**
     * Tính toán độ tương đồng nội dung giữa 2 tác phẩm (Cosine / Jaccard Similarity dựa trên Thể loại và Tác giả).
     */
    public double calculateSimilarity(Novel target, Novel candidate) {
        if (target.getId().equals(candidate.getId())) {
            return 1.0;
        }

        // 1. Thể loại Similarity (Jaccard / Cosine)
        Set<Long> targetCategoryIds = target.getCategories().stream().map(Category::getId).collect(Collectors.toSet());
        Set<Long> candidateCategoryIds = candidate.getCategories().stream().map(Category::getId).collect(Collectors.toSet());

        double categorySim = 0.0;
        if (!targetCategoryIds.isEmpty() && !candidateCategoryIds.isEmpty()) {
            long intersection = targetCategoryIds.stream().filter(candidateCategoryIds::contains).count();
            categorySim = (double) intersection / Math.sqrt(targetCategoryIds.size() * candidateCategoryIds.size());
        }

        // 2. Tác giả Similarity
        double authorSim = 0.0;
        if (target.getAuthor() != null && candidate.getAuthor() != null) {
            if (target.getAuthor().getId().equals(candidate.getAuthor().getId())) {
                authorSim = 1.0;
            }
        }

        // Trọng số CBF: 75% Thể loại + 25% Tác giả
        return 0.75 * categorySim + 0.25 * authorSim;
    }

    /**
     * Tính điểm CBF cho 1 danh sách tác phẩm đối với độc giả dựa trên lịch sử tương tác của họ.
     */
    public Map<Long, Double> calculateUserCBFScores(Map<Long, Double> userInteractions, Map<Long, Novel> allNovels) {
        Map<Long, Double> cbfScores = new HashMap<>();

        if (userInteractions == null || userInteractions.isEmpty()) {
            return cbfScores;
        }

        double totalUserWeight = userInteractions.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalUserWeight <= 0) return cbfScores;

        for (Novel candidate : allNovels.values()) {
            if (userInteractions.containsKey(candidate.getId())) {
                continue; // Đã đọc/tương tác -> Không gợi ý lại tác phẩm này
            }

            double weightedSimSum = 0.0;
            for (Map.Entry<Long, Double> entry : userInteractions.entrySet()) {
                Long interactedNovelId = entry.getKey();
                Double interactionWeight = entry.getValue();
                Novel interactedNovel = allNovels.get(interactedNovelId);

                if (interactedNovel != null) {
                    double sim = calculateSimilarity(interactedNovel, candidate);
                    weightedSimSum += sim * interactionWeight;
                }
            }

            double normalizedScore = weightedSimSum / totalUserWeight;
            cbfScores.put(candidate.getId(), normalizedScore);
        }

        return cbfScores;
    }
}
