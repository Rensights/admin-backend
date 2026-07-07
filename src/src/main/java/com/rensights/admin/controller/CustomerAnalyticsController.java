package com.rensights.admin.controller;

import com.rensights.admin.dto.CustomerAnalyticsSummaryDTO;
import com.rensights.admin.dto.CustomerLoginStatDTO;
import com.rensights.admin.dto.DailyActiveUsersPointDTO;
import com.rensights.admin.dto.LoginEventDTO;
import com.rensights.admin.dto.UserLoginSummaryDTO;
import com.rensights.admin.service.CustomerAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/customer-analytics")
@RequiredArgsConstructor
public class CustomerAnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerAnalyticsController.class);

    private final CustomerAnalyticsService customerAnalyticsService;

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        try {
            CustomerAnalyticsSummaryDTO summary = customerAnalyticsService.getSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error fetching customer analytics summary: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/trend")
    public ResponseEntity<?> getTrend(@RequestParam(defaultValue = "30") int days) {
        try {
            List<DailyActiveUsersPointDTO> trend = customerAnalyticsService.getDailyTrend(days);
            return ResponseEntity.ok(trend);
        } catch (Exception e) {
            logger.error("Error fetching DAU trend: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getCustomerLoginStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<CustomerLoginStatDTO> stats = customerAnalyticsService.getCustomerLoginStats(page, size);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error fetching customer login stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/customers/{userId}/summary")
    public ResponseEntity<?> getUserLoginSummary(@PathVariable UUID userId) {
        try {
            UserLoginSummaryDTO summary = customerAnalyticsService.getUserLoginSummary(userId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error fetching login summary for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/customers/{userId}/history")
    public ResponseEntity<?> getUserLoginHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<LoginEventDTO> history = customerAnalyticsService.getUserLoginHistory(userId, page, size);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error fetching login history for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
