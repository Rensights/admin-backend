package com.rensights.admin.repository;

import com.rensights.admin.model.RecentSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecentSaleRepository extends JpaRepository<RecentSale, UUID> {
}

