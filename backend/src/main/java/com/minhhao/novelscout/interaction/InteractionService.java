package com.minhhao.novelscout.interaction;

import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.catalog.NovelRepository;
import com.minhhao.novelscout.catalog.dto.NovelSummaryResponse;
import com.minhhao.novelscout.common.api.ApiException;
import com.minhhao.novelscout.interaction.dto.InteractionStatusResponse;
import com.minhhao.novelscout.interaction.dto.RatingRequest;
import com.minhhao.novelscout.interaction.dto.RatingResponse;
import com.minhhao.novelscout.user.User;
import com.minhhao.novelscout.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class InteractionService {

    private final NovelRepository novelRepository;
    private final UserRepository userRepository;
    private final NovelFavoriteRepository favoriteRepository;
    private final NovelRatingRepository ratingRepository;
    private final UserInteractionRepository interactionRepository;

    public InteractionService(NovelRepository novelRepository,
                              UserRepository userRepository,
                              NovelFavoriteRepository favoriteRepository,
                              NovelRatingRepository ratingRepository,
                              UserInteractionRepository interactionRepository) {
        this.novelRepository = novelRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.ratingRepository = ratingRepository;
        this.interactionRepository = interactionRepository;
    }

    @Transactional
    public boolean toggleFavorite(Long userId, String novelSlug) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Tài khoản không tồn tại"));
        Novel novel = novelRepository.findBySlugAndPublicationStatus(novelSlug, com.minhhao.novelscout.catalog.PublicationStatus.PUBLISHED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Truyện không tồn tại"));

        var existing = favoriteRepository.findByUserIdAndNovelId(userId, novel.getId());
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        } else {
            favoriteRepository.save(new NovelFavorite(user, novel));
            interactionRepository.save(new UserInteraction(user, novel, "FAVORITE", BigDecimal.valueOf(3.0)));
            return true;
        }
    }

    @Transactional
    public RatingResponse rateNovel(Long userId, String novelSlug, RatingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Tài khoản không tồn tại"));
        Novel novel = novelRepository.findBySlugAndPublicationStatus(novelSlug, com.minhhao.novelscout.catalog.PublicationStatus.PUBLISHED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Truyện không tồn tại"));

        NovelRating rating = ratingRepository.findByUserIdAndNovelId(userId, novel.getId())
                .orElseGet(() -> new NovelRating(user, novel, request.score(), request.reviewText()));

        rating.updateRating(request.score(), request.reviewText());
        NovelRating saved = ratingRepository.save(rating);

        // Recalculate average rating & rating count
        Double avgScore = ratingRepository.calculateAverageScoreByNovelId(novel.getId());
        long count = ratingRepository.countByNovelId(novel.getId());

        BigDecimal roundedAvg = (avgScore == null) ? BigDecimal.ZERO : BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP);
        updateNovelRatingFields(novel, roundedAvg, count);

        // Record interaction log
        BigDecimal weight = BigDecimal.valueOf(request.score());
        interactionRepository.save(new UserInteraction(user, novel, "RATE", weight));

        return RatingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> getNovelRatings(String novelSlug) {
        Novel novel = novelRepository.findBySlugAndPublicationStatus(novelSlug, com.minhhao.novelscout.catalog.PublicationStatus.PUBLISHED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Truyện không tồn tại"));

        return ratingRepository.findByNovelIdOrderByCreatedAtDesc(novel.getId())
                .stream()
                .map(RatingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public InteractionStatusResponse getInteractionStatus(Long userId, String novelSlug) {
        Novel novel = novelRepository.findBySlugAndPublicationStatus(novelSlug, com.minhhao.novelscout.catalog.PublicationStatus.PUBLISHED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Truyện không tồn tại"));

        long favCount = favoriteRepository.countByNovelId(novel.getId());
        if (userId == null) {
            return new InteractionStatusResponse(false, favCount, null, null);
        }

        boolean isFav = favoriteRepository.existsByUserIdAndNovelId(userId, novel.getId());
        var userRating = ratingRepository.findByUserIdAndNovelId(userId, novel.getId());

        return new InteractionStatusResponse(
                isFav,
                favCount,
                userRating.map(NovelRating::getScore).orElse(null),
                userRating.map(NovelRating::getReviewText).orElse(null)
        );
    }

    @Transactional(readOnly = true)
    public List<NovelSummaryResponse> getUserFavoriteNovels(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(fav -> NovelSummaryResponse.from(fav.getNovel()))
                .toList();
    }

    private void updateNovelRatingFields(Novel novel, BigDecimal avgRating, long ratingCount) {
        try {
            var fieldAvg = Novel.class.getDeclaredField("averageRating");
            fieldAvg.setAccessible(true);
            fieldAvg.set(novel, avgRating);

            var fieldCount = Novel.class.getDeclaredField("ratingCount");
            fieldCount.setAccessible(true);
            fieldCount.set(novel, ratingCount);

            novelRepository.save(novel);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật rating truyện: " + e.getMessage(), e);
        }
    }
}
