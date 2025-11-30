package com.rensights.admin.controller;

import com.rensights.admin.dto.*;
import com.rensights.admin.service.AdminService;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private AdminService adminService;
    
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        try {
            Page<UserDTO> users = adminService.getAllUsers(page, size, sortBy, sortDir);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId, Authentication authentication) {
        try {
            UserDTO user = adminService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error fetching user: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable UUID userId,
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        try {
            UserDTO updatedUser = adminService.updateUser(userId, request);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId, Authentication authentication) {
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/subscriptions")
    public ResponseEntity<?> getAllSubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        try {
            Page<SubscriptionDTO> subscriptions = adminService.getAllSubscriptions(page, size, sortBy, sortDir);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            logger.error("Error fetching subscriptions: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<?> getSubscriptionById(@PathVariable UUID subscriptionId, Authentication authentication) {
        try {
            SubscriptionDTO subscription = adminService.getSubscriptionById(subscriptionId);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            logger.error("Error fetching subscription: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/subscriptions/{subscriptionId}/cancel")
    public ResponseEntity<?> cancelSubscription(@PathVariable UUID subscriptionId, Authentication authentication) {
        try {
            SubscriptionDTO subscription = adminService.cancelSubscription(subscriptionId);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            logger.error("Error cancelling subscription: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(Authentication authentication) {
        try {
            DashboardStatsDTO stats = adminService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error fetching stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/analysis-requests")
    public ResponseEntity<?> getAllAnalysisRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        try {
            Page<AnalysisRequestDTO> requests = adminService.getAllAnalysisRequests(page, size, sortBy, sortDir);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            logger.error("Error fetching analysis requests: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/analysis-requests/{requestId}")
    public ResponseEntity<?> getAnalysisRequestById(@PathVariable UUID requestId, Authentication authentication) {
        try {
            AnalysisRequestDTO request = adminService.getAnalysisRequestById(requestId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            logger.error("Error fetching analysis request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/analysis-requests/{requestId}/status")
    public ResponseEntity<?> updateAnalysisRequestStatus(
            @PathVariable UUID requestId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String status = request.get("status");
            if (status == null || status.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Status is required"));
            }
            AnalysisRequestDTO updatedRequest = adminService.updateAnalysisRequestStatus(requestId, status);
            return ResponseEntity.ok(updatedRequest);
        } catch (Exception e) {
            logger.error("Error updating analysis request status: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

