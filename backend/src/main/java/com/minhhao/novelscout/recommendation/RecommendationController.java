package com.minhhao.novelscout.recommendation;

import com.minhhao.novelscout.recommendation.dto.RecommendationResponse;
import com.minhhao.novelscout.user.User;
import com.minhhao.novelscout.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/recommendations")
public class RecommendationController {

    private final HybridRecommendationService hybridRecommendationService;
    private final UserRepository userRepository;

    public RecommendationController(
            HybridRecommendationService hybridRecommendationService,
            UserRepository userRepository
    ) {
        this.hybridRecommendationService = hybridRecommendationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/personalized")
    public ResponseEntity<List<RecommendationResponse>> getPersonalizedRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "12") int limit
    ) {
        Long userId = null;
        if (userDetails != null) {
            userId = userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(userDetails.getUsername(), userDetails.getUsername())
                    .map(User::getId)
                    .orElse(null);
        }

        List<RecommendationResponse> recommendations = hybridRecommendationService.getPersonalizedRecommendations(userId, limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/similar/{slug}")
    public ResponseEntity<List<RecommendationResponse>> getSimilarNovels(
            @PathVariable String slug,
            @RequestParam(defaultValue = "6") int limit
    ) {
        List<RecommendationResponse> similar = hybridRecommendationService.getSimilarNovels(slug, limit);
        return ResponseEntity.ok(similar);
    }
}
