package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListedDealDTO {
    private UUID id;
    private String buildingName;
    private String propertyType;
    private String sizeSqft;
    private String view;
    private BigDecimal listedPriceAed;
    private BigDecimal pricePerSqft;
    private String propertyId;
    private Set<UUID> dealIds; // IDs of related deals
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

