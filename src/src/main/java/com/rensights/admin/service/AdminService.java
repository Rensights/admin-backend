package com.rensights.admin.service;

import com.rensights.admin.dto.*;
import com.rensights.admin.repository.*;
import com.rensights.admin.model.AnalysisRequest;
import com.rensights.admin.model.Device;
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
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private static final long PREMIUM_MONTHLY_PRICE = 20;
    private static final long ENTERPRISE_YEARLY_PRICE = 2000;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private AnalysisRequestRepository analysisRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${analysis.api.url:http://10.42.0.1:8000}")
    private String analysisApiUrl;
    
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
    @Transactional(readOnly = true)
    public Page<SubscriptionDTO> getAllSubscriptions(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // The @EntityGraph in repository will eagerly load the user
        return subscriptionRepository.findAll(pageable)
                .map(this::toSubscriptionDTO);
    }
    
    /**
     * Get subscription by ID
     */
    @Transactional(readOnly = true)
    public SubscriptionDTO getSubscriptionById(UUID subscriptionId) {
        // The @EntityGraph in repository will eagerly load the user
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

        List<User> users = userRepository.findAll();
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        List<Device> devices = deviceRepository.findAll();

        Map<YearMonth, long[]> monthlyUserCounts = new HashMap<>();
        Map<LocalDate, long[]> dailyUserCounts = new HashMap<>();

        for (User user : users) {
            if (user.getCreatedAt() == null || user.getUserTier() == null) {
                continue;
            }
            YearMonth month = YearMonth.from(user.getCreatedAt());
            LocalDate day = user.getCreatedAt().toLocalDate();
            int tierIndex = getTierIndex(user.getUserTier());

            monthlyUserCounts.computeIfAbsent(month, key -> new long[3])[tierIndex]++;
            dailyUserCounts.computeIfAbsent(day, key -> new long[3])[tierIndex]++;
        }

        Map<YearMonth, Long> monthlyIncomeTotals = new HashMap<>();
        Map<LocalDate, Long> dailyIncomeTotals = new HashMap<>();
        Map<Subscription.SubscriptionStatus, Long> subscriptionStatusTotals = new EnumMap<>(Subscription.SubscriptionStatus.class);

        long totalRevenue = 0;
        for (Subscription subscription : subscriptions) {
            long amount = getPlanAmount(subscription.getPlanType());
            totalRevenue += amount;

            if (subscription.getStartDate() != null) {
                YearMonth month = YearMonth.from(subscription.getStartDate());
                LocalDate day = subscription.getStartDate().toLocalDate();
                monthlyIncomeTotals.merge(month, amount, Long::sum);
                dailyIncomeTotals.merge(day, amount, Long::sum);
            }

            if (subscription.getStatus() != null) {
                subscriptionStatusTotals.merge(subscription.getStatus(), 1L, Long::sum);
            }
        }

        Map<String, Long> deviceTypeCounts = new HashMap<>();
        for (Device device : devices) {
            String type = resolveDeviceType(device.getUserAgent());
            deviceTypeCounts.merge(type, 1L, Long::sum);
        }

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        List<MonthlyIncomeDTO> monthlyIncome = new ArrayList<>();
        List<MonthlyUserRegistrationsDTO> monthlyUserRegistrations = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            long income = monthlyIncomeTotals.getOrDefault(month, 0L);
            long[] counts = monthlyUserCounts.getOrDefault(month, new long[3]);
            monthlyIncome.add(new MonthlyIncomeDTO(monthFormatter.format(month.atDay(1)), income));
            monthlyUserRegistrations.add(new MonthlyUserRegistrationsDTO(
                    monthFormatter.format(month.atDay(1)),
                    counts[0],
                    counts[1],
                    counts[2]
            ));
        }

        List<DailyIncomeDTO> dailyIncome = new ArrayList<>();
        List<DailyUserRegistrationsDTO> dailyUserRegistrations = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long income = dailyIncomeTotals.getOrDefault(date, 0L);
            long[] counts = dailyUserCounts.getOrDefault(date, new long[3]);
            dailyIncome.add(new DailyIncomeDTO(date.toString(), income));
            dailyUserRegistrations.add(new DailyUserRegistrationsDTO(
                    date.toString(),
                    counts[0],
                    counts[1],
                    counts[2]
            ));
        }

        List<DeviceTypeStatDTO> deviceTypeStats = List.of(
                new DeviceTypeStatDTO("Desktop", deviceTypeCounts.getOrDefault("Desktop", 0L)),
                new DeviceTypeStatDTO("Mobile", deviceTypeCounts.getOrDefault("Mobile", 0L)),
                new DeviceTypeStatDTO("Tablet", deviceTypeCounts.getOrDefault("Tablet", 0L))
        );

        List<SubscriptionStatusStatDTO> subscriptionStatusStats = List.of(
                new SubscriptionStatusStatDTO("ACTIVE", subscriptionStatusTotals.getOrDefault(Subscription.SubscriptionStatus.ACTIVE, 0L)),
                new SubscriptionStatusStatDTO("CANCELLED", subscriptionStatusTotals.getOrDefault(Subscription.SubscriptionStatus.CANCELLED, 0L)),
                new SubscriptionStatusStatDTO("EXPIRED", subscriptionStatusTotals.getOrDefault(Subscription.SubscriptionStatus.EXPIRED, 0L))
        );

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
                .monthlyIncome(monthlyIncome)
                .dailyIncome(dailyIncome)
                .deviceTypeStats(deviceTypeStats)
                .monthlyUserRegistrations(monthlyUserRegistrations)
                .dailyUserRegistrations(dailyUserRegistrations)
                .subscriptionStatusStats(subscriptionStatusStats)
                .build();
    }

    private int getTierIndex(User.UserTier tier) {
        if (tier == User.UserTier.PREMIUM) {
            return 1;
        }
        if (tier == User.UserTier.ENTERPRISE) {
            return 2;
        }
        return 0;
    }

    private long getPlanAmount(User.UserTier tier) {
        if (tier == User.UserTier.PREMIUM) {
            return PREMIUM_MONTHLY_PRICE;
        }
        if (tier == User.UserTier.ENTERPRISE) {
            return ENTERPRISE_YEARLY_PRICE;
        }
        return 0;
    }

    private String resolveDeviceType(String userAgent) {
        if (userAgent == null) {
            return "Desktop";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("ipad") || ua.contains("tablet")) {
            return "Tablet";
        }
        if (ua.contains("mobi") || ua.contains("iphone") || ua.contains("android")) {
            return "Mobile";
        }
        return "Desktop";
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

    @Transactional
    public AnalysisRequestDTO refreshAnalysisResult(UUID requestId) {
        AnalysisRequest request = analysisRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Analysis request not found"));

        RestTemplate restTemplate = new RestTemplate();
        String analysisId = request.getAnalysisId();
        if (analysisId == null || analysisId.isBlank()) {
            logger.warn("Analysis ID missing for request {} - falling back to request ID", requestId);
            analysisId = request.getId().toString();
        }
        String url = analysisApiUrl + "/analysis_request/" + analysisId;
        logger.info("Fetching analysis result from external service. requestId={}, analysisId={}, url={}", requestId, analysisId, url);
        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        if (response == null || response.isNull()) {
            logger.warn("No analysis result returned. requestId={}, analysisId={}", requestId, analysisId);
            throw new RuntimeException("No analysis result returned from external service");
        }

        logger.info("Analysis result fetched successfully. requestId={}, analysisId={}", requestId, analysisId);
        request.setAnalysisResult(response);
        request = analysisRequestRepository.save(request);
        logger.info("Analysis result saved. requestId={}", requestId);
        return toAnalysisRequestDTO(request);
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
                .analysisId(request.getAnalysisId())
                .analysisResult(request.getAnalysisResult())
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
                .phone(user.getPhone())
                .budget(user.getBudget())
                .portfolio(user.getPortfolio())
                .goals(readJsonList(user.getGoalsJson()))
                .registrationPlan(user.getRegistrationPlan())
                .userTier(user.getUserTier() != null ? user.getUserTier().name() : "FREE")
                .customerId(user.getCustomerId())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "")
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    private List<String> readJsonList(String json) {
        if (json == null || json.isBlank()) {
            return java.util.Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return java.util.Collections.emptyList();
        }
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
