package com.minhhao.novelscout.interaction;

import com.minhhao.novelscout.auth.CustomUserPrincipal;
import com.minhhao.novelscout.catalog.dto.NovelSummaryResponse;
import com.minhhao.novelscout.interaction.dto.InteractionStatusResponse;
import com.minhhao.novelscout.interaction.dto.RatingRequest;
import com.minhhao.novelscout.interaction.dto.RatingResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @GetMapping("/public/interactions/novels/{slug}/status")
    public InteractionStatusResponse getStatus(@PathVariable String slug, Authentication authentication) {
        Long userId = (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal p) ? p.id() : null;
        return interactionService.getInteractionStatus(userId, slug);
    }

    @GetMapping("/public/interactions/novels/{slug}/ratings")
    public List<RatingResponse> getRatings(@PathVariable String slug) {
        return interactionService.getNovelRatings(slug);
    }

    @PostMapping("/interactions/novels/{slug}/favorite")
    public Map<String, Boolean> toggleFavorite(@PathVariable String slug, Authentication authentication) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        boolean isFav = interactionService.toggleFavorite(principal.id(), slug);
        return Map.of("isFavorite", isFav);
    }

    @PostMapping("/interactions/novels/{slug}/rate")
    public RatingResponse rateNovel(@PathVariable String slug,
                                    @Valid @RequestBody RatingRequest request,
                                    Authentication authentication) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        return interactionService.rateNovel(principal.id(), slug, request);
    }

    @GetMapping("/interactions/my-favorites")
    public List<NovelSummaryResponse> getMyFavorites(Authentication authentication) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        return interactionService.getUserFavoriteNovels(principal.id());
    }
}
