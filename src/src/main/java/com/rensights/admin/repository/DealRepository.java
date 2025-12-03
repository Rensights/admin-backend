package com.rensights.admin.repository;

import com.rensights.admin.model.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    
    @Query("SELECT d FROM Deal d WHERE d.status = :status AND DATE(d.batchDate) = DATE(:batchDate)")
    Page<Deal> findByStatusAndBatchDate(@Param("status") Deal.DealStatus status, 
                                         @Param("batchDate") LocalDateTime batchDate, 
                                         Pageable pageable);
    
    List<Deal> findByIdIn(List<UUID> ids);
    
    @Query("SELECT COUNT(d) FROM Deal d WHERE d.status = 'PENDING' AND DATE(d.batchDate) = DATE(:today)")
    long countPendingDealsForToday(@Param("today") LocalDateTime today);
    
    Page<Deal> findByStatusAndActive(Deal.DealStatus status, Boolean active, Pageable pageable);
    
    Page<Deal> findByStatusAndActiveAndCity(Deal.DealStatus status, Boolean active, String city, Pageable pageable);
}

