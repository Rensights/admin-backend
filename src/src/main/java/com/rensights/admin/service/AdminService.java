package com.rensights.admin.service;

import com.rensights.admin.dto.*;
import com.rensights.admin.repository.*;
import com.rensights.admin.model.AnalysisRequest;
import com.rensights.admin.model.User;
import com.rensights.admin.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private AnalysisRequestRepository analysisRequestRepository;
    
    /**
     * Get all users with pagination
     */
    public Page<UserDTO> getAllUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return userRepository.findAll(pageable)
                .map(this::toUserDTO);
    }
    
    /**
     * Get user by ID
     */
    public UserDTO getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(this::toUserDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Update user
     */
    @Transactional
    public UserDTO updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        if (request.getUserTier() != null) {
            user.setUserTier(request.getUserTier());
        }
        
        user = userRepository.save(user);
        logger.info("Admin updated user: {}", userId);
        return toUserDTO(user);
    }
    
    /**
     * Delete user (soft delete - deactivate)
     */
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
        logger.info("Admin deactivated user: {}", userId);
    }
    
    /**
     * Get all subscriptions with pagination
     */
    public Page<SubscriptionDTO> getAllSubscriptions(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return subscriptionRepository.findAll(pageable)
                .map(this::toSubscriptionDTO);
    }
    
    /**
     * Get subscription by ID
     */
    public SubscriptionDTO getSubscriptionById(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .map(this::toSubscriptionDTO)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
    }
    
    /**
     * Cancel subscription
     */
    @Transactional
    public SubscriptionDTO cancelSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subscription.setEndDate(LocalDateTime.now());
        subscription = subscriptionRepository.save(subscription);
        
        // Update user tier to FREE
        User user = subscription.getUser();
        user.setUserTier(User.UserTier.FREE);
        userRepository.save(user);
        
        logger.info("Admin cancelled subscription: {}", subscriptionId);
        return toSubscriptionDTO(subscription);
    }
    
    /**
     * Get dashboard statistics
     */
    public DashboardStatsDTO getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeSubscriptions = subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.ACTIVE);
        long freeUsers = userRepository.countByUserTier(User.UserTier.FREE);
        long premiumUsers = userRepository.countByUserTier(User.UserTier.PREMIUM);
        long enterpriseUsers = userRepository.countByUserTier(User.UserTier.ENTERPRISE);
        long activeUsers = userRepository.countByIsActive(true);
        long verifiedUsers = userRepository.countByEmailVerified(true);
        
        // Calculate revenue (simplified - you'd get this from Stripe or payment records)
        long totalRevenue = activeSubscriptions * 50; // Placeholder calculation
        
        long pendingRequests = analysisRequestRepository.countByStatus(AnalysisRequest.AnalysisRequestStatus.PENDING);
        
        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .activeSubscriptions(activeSubscriptions)
                .totalRevenue(totalRevenue)
                .freeUsers(freeUsers)
                .premiumUsers(premiumUsers)
                .enterpriseUsers(enterpriseUsers)
                .activeUsers(activeUsers)
                .verifiedUsers(verifiedUsers)
                .pendingAnalysisRequests(pendingRequests)
                .build();
    }
    
    /**
     * Get all analysis requests with pagination
     */
    public Page<AnalysisRequestDTO> getAllAnalysisRequests(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return analysisRequestRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toAnalysisRequestDTO);
    }
    
    /**
     * Get analysis request by ID
     */
    public AnalysisRequestDTO getAnalysisRequestById(UUID requestId) {
        return analysisRequestRepository.findById(requestId)
                .map(this::toAnalysisRequestDTO)
                .orElseThrow(() -> new RuntimeException("Analysis request not found"));
    }
    
    /**
     * Update analysis request status
     */
    @Transactional
    public AnalysisRequestDTO updateAnalysisRequestStatus(UUID requestId, String status) {
        AnalysisRequest request = analysisRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Analysis request not found"));
        
        try {
            AnalysisRequest.AnalysisRequestStatus newStatus = AnalysisRequest.AnalysisRequestStatus.valueOf(status.toUpperCase());
            request.setStatus(newStatus);
            request = analysisRequestRepository.save(request);
            logger.info("Admin updated analysis request status: {} -> {}", requestId, status);
            return toAnalysisRequestDTO(request);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }
    
    private AnalysisRequestDTO toAnalysisRequestDTO(AnalysisRequest request) {
        return AnalysisRequestDTO.builder()
                .id(request.getId().toString())
                .userId(request.getUser() != null ? request.getUser().getId().toString() : null)
                .email(request.getEmail())
                .city(request.getCity())
                .area(request.getArea())
                .buildingName(request.getBuildingName())
                .listingUrl(request.getListingUrl())
                .propertyType(request.getPropertyType())
                .bedrooms(request.getBedrooms())
                .size(request.getSize())
                .plotSize(request.getPlotSize())
                .floor(request.getFloor())
                .totalFloors(request.getTotalFloors())
                .buildingStatus(request.getBuildingStatus())
                .condition(request.getCondition())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .askingPrice(request.getAskingPrice())
                .serviceCharge(request.getServiceCharge())
                .handoverDate(request.getHandoverDate())
                .developer(request.getDeveloper())
                .paymentPlan(request.getPaymentPlan())
                .features(request.getFeatures())
                .view(request.getView())
                .furnishing(request.getFurnishing())
                .additionalNotes(request.getAdditionalNotes())
                .filePaths(request.getFilePaths())
                .status(request.getStatus() != null ? request.getStatus().name() : "PENDING")
                .createdAt(request.getCreatedAt() != null ? request.getCreatedAt().toString() : "")
                .updatedAt(request.getUpdatedAt() != null ? request.getUpdatedAt().toString() : "")
                .build();
    }
    
    // Helper methods
    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userTier(user.getUserTier() != null ? user.getUserTier().name() : "FREE")
                .customerId(user.getCustomerId())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "")
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .build();
    }
    
    private SubscriptionDTO toSubscriptionDTO(Subscription subscription) {
        return SubscriptionDTO.builder()
                .id(subscription.getId().toString())
                .userId(subscription.getUser().getId().toString())
                .userEmail(subscription.getUser().getEmail())
                .planType(subscription.getPlanType() != null ? subscription.getPlanType().name() : "FREE")
                .status(subscription.getStatus() != null ? subscription.getStatus().name() : "ACTIVE")
                .startDate(subscription.getStartDate() != null ? subscription.getStartDate().toString() : "")
                .endDate(subscription.getEndDate() != null ? subscription.getEndDate().toString() : null)
                .createdAt(subscription.getCreatedAt() != null ? subscription.getCreatedAt().toString() : "")
                .stripeSubscriptionId(subscription.getStripeSubscriptionId())
                .build();
    }
}


