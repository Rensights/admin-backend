package com.rensights.admin.repository;

import com.rensights.admin.model.Area;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AreaRepository extends JpaRepository<Area, UUID> {

    /** Existing row for a name, so importing the same file twice does not duplicate it. */
    @Query("SELECT a FROM Area a WHERE LOWER(a.name) = :name")
    Optional<Area> findByNameIgnoringCase(@Param("name") String name);

    @Query("SELECT a FROM Area a WHERE :query = '' OR LOWER(a.name) LIKE CONCAT('%', :query, '%')")
    Page<Area> search(@Param("query") String query, Pageable pageable);
}
