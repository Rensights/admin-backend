package com.rensights.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "analysis_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "city", nullable = false)
    private String city;
    
    @Column(name = "area", nullable = false)
    private String area;
    
    @Column(name = "building_name", nullable = false)
    private String buildingName;
    
    @Column(name = "listing_url", length = 500)
    private String listingUrl;
    
    @Column(name = "property_type", nullable = false)
    private String propertyType;
    
    @Column(name = "bedrooms", nullable = false)
    private String bedrooms;
    
    @Column(name = "size")
    private String size;
    
    @Column(name = "plot_size")
    private String plotSize;
    
    @Column(name = "floor")
    private String floor;
    
    @Column(name = "total_floors")
    private String totalFloors;
    
    @Column(name = "building_status", nullable = false)
    private String buildingStatus;
    
    @Column(name = "condition", nullable = false)
    private String condition;
    
    @Column(name = "latitude", length = 20)
    private String latitude;
    
    @Column(name = "longitude", length = 20)
    private String longitude;
    
    @Column(name = "asking_price", nullable = false)
    private String askingPrice;
    
    @Column(name = "service_charge")
    private String serviceCharge;
    
    @Column(name = "handover_date")
    private String handoverDate;
    
    @Column(name = "developer")
    private String developer;
    
    @Column(name = "payment_plan")
    private String paymentPlan;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "jsonb")
    private List<String> features;
    
    @Column(name = "view_type")
    private String view;
    
    @Column(name = "furnishing")
    private String furnishing;
    
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "file_paths", columnDefinition = "jsonb")
    private List<String> filePaths;

    @Column(name = "analysis_id", length = 120)
    private String analysisId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "analysis_result", columnDefinition = "jsonb")
    private JsonNode analysisResult;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AnalysisRequestStatus status = AnalysisRequestStatus.PENDING;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum AnalysisRequestStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}


