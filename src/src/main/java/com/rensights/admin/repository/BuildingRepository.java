package com.rensights.admin.repository;

import com.rensights.admin.model.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<Building, UUID> {

    /**
     * Look up an existing row for an import line, so re-running an import updates rather than
     * duplicates. Matched case-insensitively on name plus area, because the same tower name can
     * legitimately appear in two districts.
     */
    @Query("SELECT b FROM Building b WHERE LOWER(b.name) = :name "
        + "AND (LOWER(COALESCE(b.area, '')) = COALESCE(:area, ''))")
    Optional<Building> findByNameAndArea(@Param("name") String name, @Param("area") String area);

    @Query("SELECT b FROM Building b WHERE :query = '' "
        + "OR LOWER(b.name) LIKE CONCAT('%', :query, '%') "
        + "OR LOWER(COALESCE(b.area, '')) LIKE CONCAT('%', :query, '%')")
    Page<Building> search(@Param("query") String query, Pageable pageable);
}
