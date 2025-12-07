package com.rensights.admin.controller;

import com.rensights.admin.dto.*;
import com.rensights.admin.service.AdminService;
import com.rensights.admin.service.DealService;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private DealService dealService;
    
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
    
    // Deal management endpoints
    @GetMapping("/deals/pending")
    public ResponseEntity<?> getPendingDeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String city,
            Authentication authentication) {
        try {
            Page<DealDTO> deals = dealService.getPendingDeals(page, size, city);
            return ResponseEntity.ok(deals);
        } catch (Exception e) {
            logger.error("Error fetching pending deals: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/deals/pending/today")
    public ResponseEntity<?> getTodayPendingDeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            Page<DealDTO> deals = dealService.getTodayPendingDeals(page, size);
            return ResponseEntity.ok(deals);
        } catch (Exception e) {
            logger.error("Error fetching today's pending deals: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/deals/{dealId}")
    public ResponseEntity<?> getDealById(@PathVariable UUID dealId, Authentication authentication) {
        try {
            DealDTO deal = dealService.getDealById(dealId);
            return ResponseEntity.ok(deal);
        } catch (Exception e) {
            logger.error("Error fetching deal: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/deals/{dealId}")
    public ResponseEntity<?> updateDeal(
            @PathVariable UUID dealId,
            @RequestBody DealDTO updateRequest,
            Authentication authentication) {
        try {
            DealDTO updatedDeal = dealService.updateDeal(dealId, updateRequest);
            return ResponseEntity.ok(updatedDeal);
        } catch (Exception e) {
            logger.error("Error updating deal: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/deals/{dealId}/approve")
    public ResponseEntity<?> approveDeal(
            @PathVariable UUID dealId,
            Authentication authentication) {
        try {
            // Get admin user ID from authentication
            UUID adminId = UUID.fromString(authentication.getName());
            DealDTO approvedDeal = dealService.approveDeal(dealId, adminId);
            return ResponseEntity.ok(approvedDeal);
        } catch (Exception e) {
            logger.error("Error approving deal: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/deals/batch-approve")
    public ResponseEntity<?> approveDeals(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            @SuppressWarnings("unchecked")
            List<String> dealIds = (List<String>) request.get("dealIds");
            if (dealIds == null || dealIds.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "dealIds is required"));
            }
            
            List<UUID> uuids = dealIds.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            
            UUID adminId = UUID.fromString(authentication.getName());
            List<DealDTO> approvedDeals = dealService.approveDeals(uuids, adminId);
            return ResponseEntity.ok(Map.of("approvedCount", approvedDeals.size(), "deals", approvedDeals));
        } catch (Exception e) {
            logger.error("Error batch approving deals: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/deals/{dealId}/reject")
    public ResponseEntity<?> rejectDeal(
            @PathVariable UUID dealId,
            Authentication authentication) {
        try {
            DealDTO rejectedDeal = dealService.rejectDeal(dealId);
            return ResponseEntity.ok(rejectedDeal);
        } catch (Exception e) {
            logger.error("Error rejecting deal: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/deals/approved")
    public ResponseEntity<?> getApprovedDeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean active) {
        try {
            Page<DealDTO> deals = dealService.getApprovedDeals(page, size, city, active);
            return ResponseEntity.ok(deals);
        } catch (Exception e) {
            logger.error("Error fetching approved deals: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/deals/rejected")
    public ResponseEntity<?> getRejectedDeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String city,
            Authentication authentication) {
        try {
            Page<DealDTO> deals = dealService.getRejectedDeals(page, size, city);
            return ResponseEntity.ok(deals);
        } catch (Exception e) {
            logger.error("Error fetching rejected deals: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/deals/{dealId}")
    public ResponseEntity<?> deleteDeal(
            @PathVariable UUID dealId,
            Authentication authentication) {
        try {
            dealService.deleteDeal(dealId);
            return ResponseEntity.ok(Map.of("message", "Deal deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting deal: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/deals/{dealId}/deactivate")
    public ResponseEntity<?> deactivateDeal(
            @PathVariable UUID dealId,
            Authentication authentication) {
        try {
            DealDTO deal = dealService.deactivateDeal(dealId);
            return ResponseEntity.ok(deal);
        } catch (Exception e) {
            logger.error("Error deactivating deal: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/deals/{dealId}/activate")
    public ResponseEntity<?> activateDeal(
            @PathVariable UUID dealId,
            Authentication authentication) {
        try {
            DealDTO deal = dealService.activateDeal(dealId);
            return ResponseEntity.ok(deal);
        } catch (Exception e) {
            logger.error("Error activating deal: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

