package com.minhhao.novelscout.recommendation;

import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.catalog.NovelRepository;
import com.minhhao.novelscout.common.api.ApiException;
import com.minhhao.novelscout.interaction.UserInteraction;
import com.minhhao.novelscout.interaction.UserInteractionRepository;
import com.minhhao.novelscout.recommendation.dto.AdminRecommendationDto.*;
import com.minhhao.novelscout.user.User;
import com.minhhao.novelscout.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminRecommendationService {

    private final ContentBasedFilteringService cbfService;
    private final CollaborativeFilteringService cfService;
    private final NovelRepository novelRepository;
    private final UserRepository userRepository;
    private final UserInteractionRepository userInteractionRepository;

    // Trọng số động thời gian thực (Thread-safe)
    private final AtomicReference<RecommendationConfigDto> currentConfig =
            new AtomicReference<>(new RecommendationConfigDto(0.45, 0.45, 0.10));

    public AdminRecommendationService(
            ContentBasedFilteringService cbfService,
            CollaborativeFilteringService cfService,
            NovelRepository novelRepository,
            UserRepository userRepository,
            UserInteractionRepository userInteractionRepository
    ) {
        this.cbfService = cbfService;
        this.cfService = cfService;
        this.novelRepository = novelRepository;
        this.userRepository = userRepository;
        this.userInteractionRepository = userInteractionRepository;
    }

    public RecommendationConfigDto getConfig() {
        return currentConfig.get();
    }

    public RecommendationConfigDto updateConfig(RecommendationConfigDto newConfig) {
        double total = newConfig.contentWeight() + newConfig.collaborativeWeight() + newConfig.popularityWeight();
        if (Math.abs(total - 1.0) > 0.05) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_WEIGHTS", "Tổng các trọng số phải bằng 1.0 (100%)");
        }
        currentConfig.set(newConfig);
        return newConfig;
    }

    public RecommendationMetricsDto getMetrics() {
        long totalUsers = userRepository.count();
        long totalNovels = novelRepository.count();
        long totalInteractions = userInteractionRepository.count();

        Set<Long> activeUserIds = userInteractionRepository.findAll().stream()
                .map(ui -> ui.getUser().getId())
                .collect(Collectors.toSet());

        double totalPossibleCells = (double) (totalUsers > 0 ? totalUsers : 1) * (totalNovels > 0 ? totalNovels : 1);
        double sparsity = 100.0 * (1.0 - ((double) totalInteractions / totalPossibleCells));
        double roundedSparsity = Math.round(sparsity * 100.0) / 100.0;

        return new RecommendationMetricsDto(
                totalUsers,
                totalNovels,
                totalInteractions,
                roundedSparsity,
                activeUserIds.size()
        );
    }

    /**
     * Kịch bản Mô phỏng Thuật toán cho một độc giả cụ thể dành cho Admin Debugger
     */
    public SimulationResultDto simulateForUser(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Không tìm thấy độc giả ID=" + userId));

        List<Novel> allNovels = novelRepository.findAll();
        Map<Long, Novel> novelMap = allNovels.stream().collect(Collectors.toMap(Novel::getId, n -> n));

        List<UserInteraction> userInteractionsList = userInteractionRepository.findByUserId(userId);
        Map<Long, Double> targetUserInteractions = userInteractionsList.stream()
                .collect(Collectors.toMap(
                        ui -> ui.getNovel().getId(),
                        ui -> ui.getWeight() != null ? ui.getWeight().doubleValue() : 1.0,
                        (a, b) -> Math.max(a, b)
                ));

        Map<Long, Double> cbfScores = cbfService.calculateUserCBFScores(targetUserInteractions, novelMap);
        Map<Long, Map<Long, Double>> userItemMatrix = cfService.buildUserItemMatrix();
        Map<Long, Double> cfScores = cfService.calculateUserCFScores(userId, userItemMatrix);

        long maxViews = allNovels.stream().mapToLong(Novel::getViewCount).max().orElse(1L);
        if (maxViews == 0) maxViews = 1L;

        RecommendationConfigDto config = getConfig();
        List<SimulationItemDto> items = new ArrayList<>();

        for (Novel candidate : allNovels) {
            if (targetUserInteractions.containsKey(candidate.getId())) {
                continue; // Đã đọc
            }

            double cbf = cbfScores.getOrDefault(candidate.getId(), 0.0);
            double cf = cfScores.getOrDefault(candidate.getId(), 0.0);

            double normView = (double) candidate.getViewCount() / maxViews;
            double normRating = candidate.getAverageRating() != null ? candidate.getAverageRating().doubleValue() / 5.0 : 0.8;
            double pop = 0.6 * normView + 0.4 * normRating;

            double hybridScore = config.contentWeight() * cbf + config.collaborativeWeight() * cf + config.popularityWeight() * pop;
            int matchPercentage = Math.min(99, Math.max(60, (int) Math.round(hybridScore * 100)));

            String reason = String.format("CBF=%.2f, CF=%.2f, Pop=%.2f", cbf, cf, pop);

            items.add(new SimulationItemDto(
                    candidate.getId(),
                    candidate.getTitle(),
                    candidate.getSlug(),
                    candidate.getCoverUrl(),
                    candidate.getAuthor() != null ? candidate.getAuthor().getName() : "Không rõ",
                    Math.round(cbf * 1000.0) / 1000.0,
                    Math.round(cf * 1000.0) / 1000.0,
                    Math.round(pop * 1000.0) / 1000.0,
                    Math.round(hybridScore * 1000.0) / 1000.0,
                    matchPercentage,
                    reason
            ));
        }

        items.sort(Comparator.comparingDouble(SimulationItemDto::hybridScore).reversed());
        List<SimulationItemDto> topItems = items.stream().limit(limit).toList();

        return new SimulationResultDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                topItems
        );
    }
}
