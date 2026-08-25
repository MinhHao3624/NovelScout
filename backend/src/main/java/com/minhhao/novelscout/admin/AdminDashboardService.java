package com.minhhao.novelscout.admin;

import com.minhhao.novelscout.admin.dto.AdminDashboardStatsResponse;
import com.minhhao.novelscout.catalog.ChapterRepository;
import com.minhhao.novelscout.catalog.NovelRepository;
import com.minhhao.novelscout.interaction.NovelFavoriteRepository;
import com.minhhao.novelscout.interaction.NovelRatingRepository;
import com.minhhao.novelscout.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final NovelRatingRepository ratingRepository;
    private final NovelFavoriteRepository favoriteRepository;

    public AdminDashboardService(
            NovelRepository novelRepository,
            ChapterRepository chapterRepository,
            UserRepository userRepository,
            NovelRatingRepository ratingRepository,
            NovelFavoriteRepository favoriteRepository
    ) {
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public AdminDashboardStatsResponse getStats() {
        long totalNovels = novelRepository.count();
        long totalChapters = chapterRepository.count();
        long totalUsers = userRepository.count();

        long totalViews = novelRepository.findAll().stream()
                .mapToLong(n -> n.getViewCount())
                .sum();


        long totalRatings = ratingRepository.count();
        long totalFavorites = favoriteRepository.count();

        Double avg = ratingRepository.findOverallAverageRating();
        double averageRating = (avg != null) ? Math.round(avg * 100.0) / 100.0 : 0.0;

        return new AdminDashboardStatsResponse(
                totalNovels,
                totalChapters,
                totalUsers,
                totalViews,
                totalRatings,
                totalFavorites,
                averageRating
        );
    }
}
