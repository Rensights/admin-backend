package com.rensights.admin.dto;

import com.rensights.admin.model.Deal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealDTO {
    private UUID id;
    private String name;
    private String location;
    private String city;
    private String area;
    private String bedrooms;
    private String bedroomCount;
    private String size;
    private String listedPrice;
    private BigDecimal priceValue;
    private BigDecimal estimateMin;
    private BigDecimal estimateMax;
    private String estimateRange;
    private String discount;
    private String rentalYield;
    private String grossRentalYield;
    private Deal.BuildingStatus buildingStatus;
    private String propertyType;
    private String priceVsEstimations;
    private BigDecimal pricePerSqft;
    private BigDecimal pricePerSqftVsMarket;
    private String propertyDescription;
    private String buildingFeatures;
    private String serviceCharge;
    private String developer;
    private String propertyLink;
    private String propertyId;
    private Deal.DealStatus status;
    private Boolean active;
    private LocalDateTime batchDate;
    private LocalDateTime approvedAt;
    private UUID approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related deals - listed deals and recent sales
    private List<DealDTO> listedDeals;
    private List<DealDTO> recentSales;
}

