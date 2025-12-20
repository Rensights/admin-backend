package com.rensights.admin.repository;

import com.rensights.admin.model.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DealRepository extends JpaRepository<Deal, UUID> {
    
    Page<Deal> findByStatus(Deal.DealStatus status, Pageable pageable);
    
    Page<Deal> findByStatusAndCity(Deal.DealStatus status, String city, Pageable pageable);
    
    // Optimized: Use date range instead of DATE() function to allow index usage
    Page<Deal> findByStatusAndBatchDateBetween(Deal.DealStatus status, 
                                                LocalDateTime startDate, 
                                                LocalDateTime endDate, 
                                                Pageable pageable);
    
    List<Deal> findByIdIn(List<UUID> ids);
    
    // Optimized: Use date range instead of DATE() function to allow index usage
    long countByStatusAndBatchDateBetween(Deal.DealStatus status, 
                                          LocalDateTime startDate, 
                                          LocalDateTime endDate);
    
    // Optimized: Bulk update for batch approval (better performance than saveAll)
    @Modifying
    @Query("UPDATE Deal d SET d.status = 'APPROVED', d.approvedAt = :approvedAt, d.approvedBy = :approvedBy WHERE d.id IN :ids")
    int bulkApproveDeals(@Param("ids") List<UUID> ids, 
                         @Param("approvedBy") UUID approvedBy, 
                         @Param("approvedAt") LocalDateTime approvedAt);
    
    Page<Deal> findByStatusAndActive(Deal.DealStatus status, Boolean active, Pageable pageable);
    
    Page<Deal> findByStatusAndActiveAndCity(Deal.DealStatus status, Boolean active, String city, Pageable pageable);
    
    // Fetch deal with listed deals and recent sales relationships
    @Query("SELECT DISTINCT d FROM Deal d " +
           "LEFT JOIN FETCH d.listedDeals " +
           "LEFT JOIN FETCH d.recentSales " +
           "WHERE d.id = :id")
    java.util.Optional<Deal> findByIdWithRelationships(@Param("id") UUID id);
}

