package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequestDTO {
    private String id;
    private String userId;
    private String email;
    private String city;
    private String area;
    private String buildingName;
    private String listingUrl;
    private String propertyType;
    private String bedrooms;
    private String size;
    private String plotSize;
    private String floor;
    private String totalFloors;
    private String buildingStatus;
    private String condition;
    private String latitude;
    private String longitude;
    private String askingPrice;
    private String serviceCharge;
    private String handoverDate;
    private String developer;
    private String paymentPlan;
    private List<String> features;
    private String view;
    private String furnishing;
    private String additionalNotes;
    private List<String> filePaths;
    private String analysisId;
    private Object analysisResult;
    private String status;
    private String createdAt;
    private String updatedAt;
}


