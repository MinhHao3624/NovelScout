package com.minhhao.novelscout.recommendation;

import com.minhhao.novelscout.interaction.UserInteraction;
import com.minhhao.novelscout.interaction.UserInteractionRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CollaborativeFilteringService {

    private final UserInteractionRepository userInteractionRepository;

    public CollaborativeFilteringService(UserInteractionRepository userInteractionRepository) {
        this.userInteractionRepository = userInteractionRepository;
    }

    /**
     * Xây dựng ma trận tương tác Người dùng - Tác phẩm (User-Item Interaction Matrix).
     */
    public Map<Long, Map<Long, Double>> buildUserItemMatrix() {
        List<UserInteraction> allInteractions = userInteractionRepository.findAll();
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();

        for (UserInteraction ui : allInteractions) {
            Long userId = ui.getUser().getId();
            Long novelId = ui.getNovel().getId();
            double weight = ui.getWeight() != null ? ui.getWeight().doubleValue() : 1.0;

            matrix.computeIfAbsent(userId, k -> new HashMap<>()).put(novelId, weight);
        }

        return matrix;
    }

    /**
     * Tính độ tương đồng hành vi đọc giữa 2 độc giả (Cosine / Pearson Correlation).
     */
    public double calculateUserSimilarity(Map<Long, Double> userA, Map<Long, Double> userB) {
        if (userA == null || userB == null || userA.isEmpty() || userB.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (Map.Entry<Long, Double> entry : userA.entrySet()) {
            Long novelId = entry.getKey();
            double valA = entry.getValue();
            normA += valA * valA;

            if (userB.containsKey(novelId)) {
                double valB = userB.get(novelId);
                dotProduct += valA * valB;
            }
        }

        for (double valB : userB.values()) {
            normB += valB * valB;
        }

        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Tính điểm gợi ý Collaborative Filtering cho độc giả target.
     */
    public Map<Long, Double> calculateUserCFScores(Long targetUserId, Map<Long, Map<Long, Double>> matrix) {
        Map<Long, Double> cfScores = new HashMap<>();
        Map<Long, Double> targetUserRatings = matrix.get(targetUserId);

        if (targetUserRatings == null || targetUserRatings.isEmpty()) {
            return cfScores; // target user chưa tương tác -> Trả về rỗng (để Hybrid xử lý Cold Start)
        }

        Map<Long, Double> simSumMap = new HashMap<>();
        Map<Long, Double> weightedScoreMap = new HashMap<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : matrix.entrySet()) {
            Long peerUserId = entry.getKey();
            if (peerUserId.equals(targetUserId)) continue;

            Map<Long, Double> peerUserRatings = entry.getValue();
            double sim = calculateUserSimilarity(targetUserRatings, peerUserRatings);

            if (sim > 0.05) { // Chỉ xét các độc giả có độ tương đồng dương đáng kể
                for (Map.Entry<Long, Double> ratingEntry : peerUserRatings.entrySet()) {
                    Long novelId = ratingEntry.getKey();
                    double rating = ratingEntry.getValue();

                    if (!targetUserRatings.containsKey(novelId)) { // Truyện target user chưa đọc
                        weightedScoreMap.put(novelId, weightedScoreMap.getOrDefault(novelId, 0.0) + sim * rating);
                        simSumMap.put(novelId, simSumMap.getOrDefault(novelId, 0.0) + sim);
                    }
                }
            }
        }

        for (Map.Entry<Long, Double> entry : weightedScoreMap.entrySet()) {
            Long novelId = entry.getKey();
            double totalWeightedScore = entry.getValue();
            double totalSim = simSumMap.get(novelId);

            if (totalSim > 0) {
                // Điểm chuẩn hóa về khoảng [0, 1] (Do max weight khoảng 10.0)
                double rawPredictedScore = totalWeightedScore / totalSim;
                double normalizedScore = Math.min(1.0, rawPredictedScore / 10.0);
                cfScores.put(novelId, normalizedScore);
            }
        }

        return cfScores;
    }
}
