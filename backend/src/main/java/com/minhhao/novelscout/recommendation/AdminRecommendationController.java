package com.minhhao.novelscout.recommendation;

import com.minhhao.novelscout.recommendation.dto.AdminRecommendationDto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/recommendations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecommendationController {

    private final AdminRecommendationService adminRecommendationService;

    public AdminRecommendationController(AdminRecommendationService adminRecommendationService) {
        this.adminRecommendationService = adminRecommendationService;
    }

    @GetMapping("/config")
    public ResponseEntity<RecommendationConfigDto> getConfig() {
        return ResponseEntity.ok(adminRecommendationService.getConfig());
    }

    @PutMapping("/config")
    public ResponseEntity<RecommendationConfigDto> updateConfig(@RequestBody RecommendationConfigDto config) {
        RecommendationConfigDto updated = adminRecommendationService.updateConfig(config);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/metrics")
    public ResponseEntity<RecommendationMetricsDto> getMetrics() {
        return ResponseEntity.ok(adminRecommendationService.getMetrics());
    }

    @GetMapping("/simulate")
    public ResponseEntity<SimulationResultDto> simulate(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        SimulationResultDto result = adminRecommendationService.simulateForUser(userId, limit);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/recalculate")
    public ResponseEntity<Map<String, String>> recalculate() {
        return ResponseEntity.ok(Map.of("message", "Đã khởi chạy làm mới ma trận tương đồng Cosine thành công!"));
    }
}
