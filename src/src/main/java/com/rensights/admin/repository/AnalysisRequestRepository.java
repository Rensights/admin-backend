package com.rensights.admin.repository;

import com.rensights.admin.model.AnalysisRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, UUID> {
    Page<AnalysisRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    long countByStatus(AnalysisRequest.AnalysisRequestStatus status);
}

