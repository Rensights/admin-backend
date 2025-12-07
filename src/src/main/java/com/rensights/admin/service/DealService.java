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
    
    @Autowired
    private DealRepository dealRepository;
    
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
     * Get pending deals for today's batch
     */
    public Page<DealDTO> getTodayPendingDeals(int page, int size) {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        LocalDateTime today = LocalDateTime.now();
        
        Page<Deal> deals = dealRepository.findByStatusAndBatchDate(Deal.DealStatus.PENDING, today, pageable);
        return deals.map(this::toDTO);
    }
    
    /**
     * Get deal by ID
     */
    public DealDTO getDealById(UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        return toDTO(deal);
    }
    
    /**
     * Update deal
     */
    @Transactional(transactionManager = "publicTransactionManager")
    public DealDTO updateDeal(UUID dealId, DealDTO updateRequest) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        
        if (updateRequest.getName() != null) {
            deal.setName(updateRequest.getName());
        }
        if (updateRequest.getLocation() != null) {
            deal.setLocation(updateRequest.getLocation());
        }
        if (updateRequest.getCity() != null) {
            deal.setCity(updateRequest.getCity());
        }
        if (updateRequest.getArea() != null) {
            deal.setArea(updateRequest.getArea());
        }
        if (updateRequest.getBedrooms() != null) {
            deal.setBedrooms(updateRequest.getBedrooms());
        }
        if (updateRequest.getBedroomCount() != null) {
            deal.setBedroomCount(updateRequest.getBedroomCount());
        }
        if (updateRequest.getSize() != null) {
            deal.setSize(updateRequest.getSize());
        }
        if (updateRequest.getListedPrice() != null) {
            deal.setListedPrice(updateRequest.getListedPrice());
        }
        if (updateRequest.getPriceValue() != null) {
            deal.setPriceValue(updateRequest.getPriceValue());
        }
        if (updateRequest.getEstimateMin() != null) {
            deal.setEstimateMin(updateRequest.getEstimateMin());
        }
        if (updateRequest.getEstimateMax() != null) {
            deal.setEstimateMax(updateRequest.getEstimateMax());
        }
        if (updateRequest.getEstimateRange() != null) {
            deal.setEstimateRange(updateRequest.getEstimateRange());
        }
        if (updateRequest.getDiscount() != null) {
            deal.setDiscount(updateRequest.getDiscount());
        }
        if (updateRequest.getRentalYield() != null) {
            deal.setRentalYield(updateRequest.getRentalYield());
        }
        if (updateRequest.getBuildingStatus() != null) {
            deal.setBuildingStatus(updateRequest.getBuildingStatus());
        }
        
        deal = dealRepository.save(deal);
        logger.info("Deal updated: {}", dealId);
        return toDTO(deal);
    }
    
    /**
     * Approve a single deal
     */
    @Transactional(transactionManager = "publicTransactionManager")
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
     * Approve multiple deals (batch approval)
     */
    @Transactional(transactionManager = "publicTransactionManager")
    public List<DealDTO> approveDeals(List<UUID> dealIds, UUID approvedBy) {
        List<Deal> deals = dealRepository.findByIdIn(dealIds);
        
        if (deals.size() != dealIds.size()) {
            throw new RuntimeException("Some deals were not found");
        }
        
        LocalDateTime now = LocalDateTime.now();
        deals.forEach(deal -> {
            deal.setStatus(Deal.DealStatus.APPROVED);
            deal.setApprovedAt(now);
            deal.setApprovedBy(approvedBy);
        });
        
        deals = dealRepository.saveAll(deals);
        logger.info("Approved {} deals by admin: {}", deals.size(), approvedBy);
        
        return deals.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    /**
     * Reject a deal
     */
    @Transactional(transactionManager = "publicTransactionManager")
    public DealDTO rejectDeal(UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        
        deal.setStatus(Deal.DealStatus.REJECTED);
        deal = dealRepository.save(deal);
        logger.info("Deal rejected: {}", dealId);
        return toDTO(deal);
    }
    
    /**
     * Get count of pending deals for today
     */
    public long getTodayPendingDealsCount() {
        return dealRepository.countPendingDealsForToday(LocalDateTime.now());
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
     * Delete a deal permanently
     */
    @Transactional(transactionManager = "publicTransactionManager")
    public void deleteDeal(UUID dealId) {
        if (!dealRepository.existsById(dealId)) {
            throw new RuntimeException("Deal not found");
        }
        dealRepository.deleteById(dealId);
        logger.info("Deal deleted: {}", dealId);
    }
    
    /**
     * Deactivate a deal (make it invisible to users)
     */
    @Transactional(transactionManager = "publicTransactionManager")
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
    @Transactional(transactionManager = "publicTransactionManager")
    public DealDTO activateDeal(UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        deal.setActive(true);
        deal = dealRepository.save(deal);
        logger.info("Deal activated: {}", dealId);
        return toDTO(deal);
    }
    
    /**
     * Convert Deal entity to DTO
     */
    private DealDTO toDTO(Deal deal) {
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
                .buildingStatus(deal.getBuildingStatus())
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

