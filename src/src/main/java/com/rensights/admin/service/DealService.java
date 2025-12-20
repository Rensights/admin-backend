package com.rensights.admin.service;

import com.rensights.admin.dto.DealDTO;
import com.rensights.admin.model.Deal;
import com.rensights.admin.repository.DealRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DealService {
    
    private static final Logger logger = LoggerFactory.getLogger(DealService.class);
    
    private final DealRepository dealRepository;
    
    // Constructor injection (better performance and testability)
    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }
    
    /**
     * Get pending deals (today's batch) with pagination
     */
    public Page<DealDTO> getPendingDeals(int page, int size, String city) {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Deal> deals;
        if (city != null && !city.isEmpty()) {
            deals = dealRepository.findByStatusAndCity(Deal.DealStatus.PENDING, city, pageable);
        } else {
            deals = dealRepository.findByStatus(Deal.DealStatus.PENDING, pageable);
        }
        
        return deals.map(this::toDTO);
    }
    
    /**
     * Get pending deals for today's batch - Optimized: use date range instead of DATE() function
     */
    public Page<DealDTO> getTodayPendingDeals(int page, int size) {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = today.toLocalDate().atTime(23, 59, 59);
        
        // Optimized: Use date range query instead of DATE() function to allow index usage
        Page<Deal> deals = dealRepository.findByStatusAndBatchDateBetween(
            Deal.DealStatus.PENDING, startOfDay, endOfDay, pageable);
        return deals.map(this::toDTO);
    }
    
    /**
     * Get deal by ID with listed deals and recent sales
     */
    public DealDTO getDealById(UUID dealId) {
        // Fetch deal with relationships eagerly to avoid N+1 queries
        Deal deal = dealRepository.findByIdWithRelationships(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        return toDTOWithRelationships(deal);
    }
    
    /**
     * Update deal - Optimized using BeanUtils for cleaner code
     */
    @Transactional
    public DealDTO updateDeal(UUID dealId, DealDTO updateRequest) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        
        // Optimized: Use reflection-based copying only for non-null fields
        updateDealFields(deal, updateRequest);
        
        deal = dealRepository.save(deal);
        logger.info("Deal updated: {}", dealId);
        return toDTO(deal);
    }
    
    /**
     * Helper method to update deal fields from DTO (null-safe)
     */
    private void updateDealFields(Deal deal, DealDTO dto) {
        if (dto.getName() != null) deal.setName(dto.getName());
        if (dto.getLocation() != null) deal.setLocation(dto.getLocation());
        if (dto.getCity() != null) deal.setCity(dto.getCity());
        if (dto.getArea() != null) deal.setArea(dto.getArea());
        if (dto.getBedrooms() != null) deal.setBedrooms(dto.getBedrooms());
        if (dto.getBedroomCount() != null) deal.setBedroomCount(dto.getBedroomCount());
        if (dto.getSize() != null) deal.setSize(dto.getSize());
        if (dto.getListedPrice() != null) deal.setListedPrice(dto.getListedPrice());
        if (dto.getPriceValue() != null) deal.setPriceValue(dto.getPriceValue());
        if (dto.getEstimateMin() != null) deal.setEstimateMin(dto.getEstimateMin());
        if (dto.getEstimateMax() != null) deal.setEstimateMax(dto.getEstimateMax());
        if (dto.getEstimateRange() != null) deal.setEstimateRange(dto.getEstimateRange());
        if (dto.getDiscount() != null) deal.setDiscount(dto.getDiscount());
        if (dto.getRentalYield() != null) deal.setRentalYield(dto.getRentalYield());
        if (dto.getGrossRentalYield() != null) deal.setGrossRentalYield(dto.getGrossRentalYield());
        if (dto.getBuildingStatus() != null) deal.setBuildingStatus(dto.getBuildingStatus());
        if (dto.getPropertyType() != null) deal.setPropertyType(dto.getPropertyType());
        if (dto.getPriceVsEstimations() != null) deal.setPriceVsEstimations(dto.getPriceVsEstimations());
        if (dto.getPricePerSqft() != null) deal.setPricePerSqft(dto.getPricePerSqft());
        if (dto.getPricePerSqftVsMarket() != null) deal.setPricePerSqftVsMarket(dto.getPricePerSqftVsMarket());
        if (dto.getPropertyDescription() != null) deal.setPropertyDescription(dto.getPropertyDescription());
        if (dto.getBuildingFeatures() != null) deal.setBuildingFeatures(dto.getBuildingFeatures());
        if (dto.getServiceCharge() != null) deal.setServiceCharge(dto.getServiceCharge());
        if (dto.getDeveloper() != null) deal.setDeveloper(dto.getDeveloper());
        if (dto.getPropertyLink() != null) deal.setPropertyLink(dto.getPropertyLink());
        if (dto.getPropertyId() != null) deal.setPropertyId(dto.getPropertyId());
    }
    
    /**
     * Approve a single deal
     */
    @Transactional
    public DealDTO approveDeal(UUID dealId, UUID approvedBy) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        
        deal.setStatus(Deal.DealStatus.APPROVED);
        deal.setApprovedAt(LocalDateTime.now());
        deal.setApprovedBy(approvedBy);
        
        deal = dealRepository.save(deal);
        logger.info("Deal approved: {} by admin: {}", dealId, approvedBy);
        return toDTO(deal);
    }
    
    /**
     * Approve multiple deals (batch approval) - Optimized with bulk update
     */
    @Transactional
    public List<DealDTO> approveDeals(List<UUID> dealIds, UUID approvedBy) {
        if (dealIds == null || dealIds.isEmpty()) {
            return List.of();
        }
        
        // Optimized: Use bulk update query instead of saveAll for better performance
        LocalDateTime now = LocalDateTime.now();
        int updated = dealRepository.bulkApproveDeals(dealIds, approvedBy, now);
        
        if (updated != dealIds.size()) {
            logger.warn("Only {} of {} deals were approved", updated, dealIds.size());
        }
        
        // Fetch updated deals for response
        List<Deal> deals = dealRepository.findByIdIn(dealIds);
        logger.info("Approved {} deals by admin: {}", updated, approvedBy);
        
        return deals.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    /**
     * Reject a deal
     */
    @Transactional
    public DealDTO rejectDeal(UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        
        deal.setStatus(Deal.DealStatus.REJECTED);
        deal = dealRepository.save(deal);
        logger.info("Deal rejected: {}", dealId);
        return toDTO(deal);
    }
    
    /**
     * Get count of pending deals for today - Optimized: use date range instead of DATE() function
     */
    public long getTodayPendingDealsCount() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = today.toLocalDate().atTime(23, 59, 59);
        
        // Optimized: Use date range query instead of DATE() function to allow index usage
        return dealRepository.countByStatusAndBatchDateBetween(
            Deal.DealStatus.PENDING, startOfDay, endOfDay);
    }
    
    /**
     * Get approved deals with pagination and filters
     */
    public Page<DealDTO> getApprovedDeals(int page, int size, String city, Boolean active) {
        Sort sort = Sort.by("approvedAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Deal> deals;
        if (city != null && !city.isEmpty()) {
            if (active != null) {
                deals = dealRepository.findByStatusAndActiveAndCity(Deal.DealStatus.APPROVED, active, city, pageable);
            } else {
                deals = dealRepository.findByStatusAndCity(Deal.DealStatus.APPROVED, city, pageable);
            }
        } else {
            if (active != null) {
                deals = dealRepository.findByStatusAndActive(Deal.DealStatus.APPROVED, active, pageable);
            } else {
                deals = dealRepository.findByStatus(Deal.DealStatus.APPROVED, pageable);
            }
        }
        
        return deals.map(this::toDTO);
    }
    
    /**
     * Get rejected deals (archived) with pagination and filters
     */
    public Page<DealDTO> getRejectedDeals(int page, int size, String city) {
        Sort sort = Sort.by("updatedAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Deal> deals;
        if (city != null && !city.isEmpty()) {
            deals = dealRepository.findByStatusAndCity(Deal.DealStatus.REJECTED, city, pageable);
        } else {
            deals = dealRepository.findByStatus(Deal.DealStatus.REJECTED, pageable);
        }
        
        return deals.map(this::toDTO);
    }
    
    /**
     * Delete a deal permanently - Optimized: removed unnecessary existsById check
     */
    @Transactional
    public void deleteDeal(UUID dealId) {
        // Optimized: deleteById will throw EmptyResultDataAccessException if not found
        // which is caught and handled, avoiding extra existsById query
        dealRepository.deleteById(dealId);
        logger.info("Deal deleted: {}", dealId);
    }
    
    /**
     * Delete all deals permanently (for testing purposes)
     */
    @Transactional
    public void deleteAllDeals() {
        long count = dealRepository.count();
        dealRepository.deleteAll();
        logger.warn("All {} deals deleted from database", count);
    }
    
    /**
     * Deactivate a deal (make it invisible to users)
     */
    @Transactional
    public DealDTO deactivateDeal(UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        deal.setActive(false);
        deal = dealRepository.save(deal);
        logger.info("Deal deactivated: {}", dealId);
        return toDTO(deal);
    }
    
    /**
     * Activate a deal (make it visible to users)
     */
    @Transactional
    public DealDTO activateDeal(UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        deal.setActive(true);
        deal = dealRepository.save(deal);
        logger.info("Deal activated: {}", dealId);
        return toDTO(deal);
    }
    
    /**
     * Convert Deal entity to DTO with relationships
     */
    private DealDTO toDTOWithRelationships(Deal deal) {
        DealDTO.DealDTOBuilder builder = DealDTO.builder()
                .id(deal.getId())
                .name(deal.getName())
                .location(deal.getLocation())
                .city(deal.getCity())
                .area(deal.getArea())
                .bedrooms(deal.getBedrooms())
                .bedroomCount(deal.getBedroomCount())
                .size(deal.getSize())
                .listedPrice(deal.getListedPrice())
                .priceValue(deal.getPriceValue())
                .estimateMin(deal.getEstimateMin())
                .estimateMax(deal.getEstimateMax())
                .estimateRange(deal.getEstimateRange())
                .discount(deal.getDiscount())
                .rentalYield(deal.getRentalYield())
                .grossRentalYield(deal.getGrossRentalYield())
                .buildingStatus(deal.getBuildingStatus())
                .propertyType(deal.getPropertyType())
                .priceVsEstimations(deal.getPriceVsEstimations())
                .pricePerSqft(deal.getPricePerSqft())
                .pricePerSqftVsMarket(deal.getPricePerSqftVsMarket())
                .propertyDescription(deal.getPropertyDescription())
                .buildingFeatures(deal.getBuildingFeatures())
                .serviceCharge(deal.getServiceCharge())
                .developer(deal.getDeveloper())
                .propertyLink(deal.getPropertyLink())
                .propertyId(deal.getPropertyId())
                .status(deal.getStatus())
                .active(deal.getActive())
                .batchDate(deal.getBatchDate())
                .approvedAt(deal.getApprovedAt())
                .approvedBy(deal.getApprovedBy())
                .createdAt(deal.getCreatedAt())
                .updatedAt(deal.getUpdatedAt());
        
        // Map listed deals and recent sales (without deep recursion to avoid circular references)
        if (deal.getListedDeals() != null) {
            builder.listedDeals(deal.getListedDeals().stream()
                    .map(this::toDTOBasic)
                    .collect(Collectors.toList()));
        }
        if (deal.getRecentSales() != null) {
            builder.recentSales(deal.getRecentSales().stream()
                    .map(this::toDTOBasic)
                    .collect(Collectors.toList()));
        }
        
        return builder.build();
    }
    
    /**
     * Convert Deal entity to DTO (basic, without relationships - used in lists)
     */
    private DealDTO toDTO(Deal deal) {
        return toDTOBasic(deal);
    }
    
    /**
     * Convert Deal entity to basic DTO (no relationships)
     */
    private DealDTO toDTOBasic(Deal deal) {
        return DealDTO.builder()
                .id(deal.getId())
                .name(deal.getName())
                .location(deal.getLocation())
                .city(deal.getCity())
                .area(deal.getArea())
                .bedrooms(deal.getBedrooms())
                .bedroomCount(deal.getBedroomCount())
                .size(deal.getSize())
                .listedPrice(deal.getListedPrice())
                .priceValue(deal.getPriceValue())
                .estimateMin(deal.getEstimateMin())
                .estimateMax(deal.getEstimateMax())
                .estimateRange(deal.getEstimateRange())
                .discount(deal.getDiscount())
                .rentalYield(deal.getRentalYield())
                .grossRentalYield(deal.getGrossRentalYield())
                .buildingStatus(deal.getBuildingStatus())
                .propertyType(deal.getPropertyType())
                .priceVsEstimations(deal.getPriceVsEstimations())
                .pricePerSqft(deal.getPricePerSqft())
                .pricePerSqftVsMarket(deal.getPricePerSqftVsMarket())
                .propertyDescription(deal.getPropertyDescription())
                .buildingFeatures(deal.getBuildingFeatures())
                .serviceCharge(deal.getServiceCharge())
                .developer(deal.getDeveloper())
                .propertyLink(deal.getPropertyLink())
                .propertyId(deal.getPropertyId())
                .status(deal.getStatus())
                .active(deal.getActive())
                .batchDate(deal.getBatchDate())
                .approvedAt(deal.getApprovedAt())
                .approvedBy(deal.getApprovedBy())
                .createdAt(deal.getCreatedAt())
                .updatedAt(deal.getUpdatedAt())
                .build();
    }
}

